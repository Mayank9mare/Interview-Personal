import java.util.*;
import java.util.concurrent.*;

public class TaskDependencyExecutor {

    // ═══════════════════════════════════════════════════════════════════════════
    // Task Dependency Executor
    //
    // Problem: Given a set of tasks with dependencies (a DAG), execute them
    // concurrently such that a task only runs after ALL its dependencies finish.
    //
    // API:
    //   addTask(id, action, deps...)  — register a task and its dependencies
    //   execute()                     — run all tasks; blocks until all done
    //
    // Design:
    //   • Build forward edges (dep → dependents) after all tasks are registered.
    //   • Each TaskNode tracks pendingDeps (volatile int).
    //   • Tasks with pendingDeps == 0 are immediately runnable.
    //   • On task completion: decrement pendingDeps of each dependent;
    //     if it hits 0, submit that dependent to the thread pool.
    //   • A shared CountDownLatch(total tasks) signals overall completion.
    //   • Cycle detection via DFS before execution begins.
    //
    // Thread safety: pendingDeps update + pool.submit under synchronized(node).
    // Complexity: O(V + E) time, O(V + E) space (V=tasks, E=dependency edges).
    // ═══════════════════════════════════════════════════════════════════════════

    static class TaskNode {
        final String         id;
        final Runnable       action;
        final List<String>   depIds;
        final List<TaskNode> dependents = new ArrayList<>(); // tasks that need THIS
        volatile int         pendingDeps;                    // decremented as deps finish

        TaskNode(String id, Runnable action, List<String> depIds) {
            this.id          = id;
            this.action      = action;
            this.depIds      = depIds;
            this.pendingDeps = depIds.size();
        }
    }

    private final Map<String, TaskNode> tasks = new LinkedHashMap<>();

    public void addTask(String id, Runnable action, String... deps) {
        if (tasks.containsKey(id)) throw new IllegalArgumentException("Duplicate task: " + id);
        tasks.put(id, new TaskNode(id, action, Arrays.asList(deps)));
    }

    // Execute all tasks respecting dependencies; blocks until all complete.
    public void execute() {
        // Wire up dependents (reverse of dep edges)
        for (TaskNode node : tasks.values()) {
            for (String depId : node.depIds) {
                TaskNode dep = tasks.get(depId);
                if (dep == null) throw new IllegalStateException("Unknown dependency: " + depId);
                dep.dependents.add(node);
            }
        }

        if (hasCycle()) throw new IllegalStateException("Dependency graph has a cycle");

        CountDownLatch allDone = new CountDownLatch(tasks.size());
        ExecutorService pool   = Executors.newCachedThreadPool();

        // Seed: tasks with no dependencies run immediately
        for (TaskNode node : tasks.values()) {
            if (node.pendingDeps == 0) pool.submit(() -> run(node, pool, allDone));
        }

        try {
            allDone.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }
    }

    private void run(TaskNode node, ExecutorService pool, CountDownLatch allDone) {
        System.out.printf("[%s] START  %s%n", Thread.currentThread().getName(), node.id);
        try {
            node.action.run();
        } catch (Exception e) {
            System.err.println("Task " + node.id + " threw: " + e.getMessage());
        }
        System.out.printf("[%s] DONE   %s%n", Thread.currentThread().getName(), node.id);
        allDone.countDown();

        // Notify dependents — submit those whose last dependency just finished
        for (TaskNode dep : node.dependents) {
            int remaining;
            synchronized (dep) {
                remaining = --dep.pendingDeps;
            }
            if (remaining == 0) pool.submit(() -> run(dep, pool, allDone));
        }
    }

    // ── Cycle detection (DFS) ─────────────────────────────────────────────────

    private boolean hasCycle() {
        Set<String> visited = new HashSet<>(), onStack = new HashSet<>();
        for (String id : tasks.keySet())
            if (dfs(id, visited, onStack)) return true;
        return false;
    }

    private boolean dfs(String id, Set<String> visited, Set<String> onStack) {
        if (onStack.contains(id))  return true;
        if (visited.contains(id))  return false;
        visited.add(id); onStack.add(id);
        for (String dep : tasks.get(id).depIds)
            if (dfs(dep, visited, onStack)) return true;
        onStack.remove(id);
        return false;
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {

        System.out.println("══════════════════════════════════════");
        System.out.println(" Example 1: Chain  A → B → C         ");
        System.out.println("══════════════════════════════════════");
        TaskDependencyExecutor e1 = new TaskDependencyExecutor();
        e1.addTask("A", () -> work("A", 100));
        e1.addTask("B", () -> work("B",  80), "A");
        e1.addTask("C", () -> work("C",  60), "B");
        e1.execute();

        System.out.println("\n══════════════════════════════════════");
        System.out.println(" Example 2: Diamond  A → B,C → D     ");
        System.out.println("══════════════════════════════════════");
        // B and C run in parallel after A; D waits for both
        TaskDependencyExecutor e2 = new TaskDependencyExecutor();
        e2.addTask("A", () -> work("A", 100));
        e2.addTask("B", () -> work("B", 200), "A");
        e2.addTask("C", () -> work("C",  80), "A");
        e2.addTask("D", () -> work("D",  50), "B", "C");
        e2.execute();

        System.out.println("\n══════════════════════════════════════");
        System.out.println(" Example 3: Fan-out  A → B,C,D       ");
        System.out.println("══════════════════════════════════════");
        TaskDependencyExecutor e3 = new TaskDependencyExecutor();
        e3.addTask("A", () -> work("A",  50));
        e3.addTask("B", () -> work("B", 100), "A");
        e3.addTask("C", () -> work("C", 150), "A");
        e3.addTask("D", () -> work("D",  80), "A");
        e3.addTask("E", () -> work("E",  60), "B", "C", "D");
        e3.execute();

        System.out.println("\n══════════════════════════════════════");
        System.out.println(" Example 4: Cycle detection           ");
        System.out.println("══════════════════════════════════════");
        try {
            TaskDependencyExecutor e4 = new TaskDependencyExecutor();
            e4.addTask("X", () -> {}, "Y");
            e4.addTask("Y", () -> {}, "Z");
            e4.addTask("Z", () -> {}, "X");
            e4.execute();
        } catch (IllegalStateException ex) {
            System.out.println("Caught expected: " + ex.getMessage());
        }
    }

    private static void work(String name, long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
