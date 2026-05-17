import java.util.*;
import java.util.concurrent.*;

/**
 * Concurrent task executor that respects a dependency DAG — each task runs only after
 * all of its declared dependencies have completed.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>After all tasks are registered, build reverse edges (dependency → dependents).</li>
 *   <li>Seed the thread pool with tasks whose {@code pendingDeps == 0}.</li>
 *   <li>When a task finishes, atomically decrement {@code pendingDeps} for each dependent;
 *       submit those that reach 0 to the pool.</li>
 *   <li>A {@code CountDownLatch(total)} blocks {@link #execute()} until every task is done.</li>
 * </ol>
 *
 * <p>Cycle detection runs via DFS before execution begins and throws
 * {@link IllegalStateException} if a cycle is found.
 *
 * <p>Thread safety: {@code pendingDeps} decrements are {@code synchronized} on the node
 * to prevent races when multiple dependencies finish concurrently.
 */
public class TaskDependencyExecutor {

    /**
     * A single task node in the dependency graph.
     */
    static class TaskNode {
        /** Unique task identifier. */
        final String         id;

        /** The work to execute. */
        final Runnable       action;

        /** IDs of tasks that must complete before this one can start. */
        final List<String>   depIds;

        /** Tasks that are waiting for this task to finish (reverse edges). */
        final List<TaskNode> dependents = new ArrayList<>();

        /** Number of declared dependencies that have not yet finished; starts at {@code depIds.size()}. */
        volatile int         pendingDeps;

        /**
         * @param id     unique task identifier
         * @param action the work to perform
         * @param depIds IDs of prerequisite tasks
         */
        TaskNode(String id, Runnable action, List<String> depIds) {
            this.id          = id;
            this.action      = action;
            this.depIds      = depIds;
            this.pendingDeps = depIds.size();
        }
    }

    /** All registered tasks keyed by ID; insertion-ordered for deterministic seeding. */
    private final Map<String, TaskNode> tasks = new LinkedHashMap<>();

    /**
     * Registers a task with its declared dependencies.
     *
     * @param id     unique task identifier
     * @param action the work to perform
     * @param deps   zero or more IDs of tasks that must finish before this one
     * @throws IllegalArgumentException if a task with this ID is already registered
     */
    public void addTask(String id, Runnable action, String... deps) {
        if (tasks.containsKey(id)) throw new IllegalArgumentException("Duplicate task: " + id);
        tasks.put(id, new TaskNode(id, action, Arrays.asList(deps)));
    }

    /**
     * Executes all registered tasks concurrently, respecting the declared dependency order.
     * Blocks until every task has completed.
     *
     * @throws IllegalStateException if any declared dependency ID is unknown, or if the
     *                               dependency graph contains a cycle
     */
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

    /** Executes a task and, on completion, checks whether any dependent is now unblocked. */
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

    /** Returns {@code true} if the dependency graph contains a cycle. */
    private boolean hasCycle() {
        Set<String> visited = new HashSet<>(), onStack = new HashSet<>();
        for (String id : tasks.keySet())
            if (dfs(id, visited, onStack)) return true;
        return false;
    }

    /** DFS cycle-detection helper; {@code onStack} tracks the current recursion path. */
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
