// Companies: Slice
// Extended task scheduler — schedule tasks at future times with TICK and UNDO support.

import java.util.*;

/**
 * Extended task scheduler supporting three operations: scheduling a task at a
 * future time, advancing time (TICK), and undoing the most recent schedule command.
 *
 * <p>Models the "Extended Task Scheduler" machine coding problem asked in Slice's
 * managerial round. Inputs arrive as method calls corresponding to commands:
 * {@code TASK [id] AT [t]}, {@code TICK}, and {@code UNDO}.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>{@code schedule}: {@code TreeMap<time, List<taskId>>} — keeps pending tasks
 *       sorted by time for O(log n) scheduling and O(1) tick execution (just remove
 *       the smallest key when time reaches it).</li>
 *   <li>{@code history}: deque used as a stack of {@code [taskId, scheduledTime]}
 *       pairs — supports UNDO in O(log n) (must find and remove from the TreeMap).</li>
 * </ul>
 *
 * <p>Core invariants:
 * <ul>
 *   <li>Tasks whose scheduled time ≤ {@code currentTime} are either executed or were
 *       past when scheduled (they run on the next tick that reaches their time).</li>
 *   <li>UNDO only affects the pending schedule; already-executed tasks cannot be undone.</li>
 * </ul>
 *
 * <p>Thread safety: Not thread-safe.
 */
public class TaskScheduler {

    // ── Fields ────────────────────────────────────────────────────────────

    /**
     * Pending task schedule: time → ordered list of taskIds due at that time.
     * TreeMap keeps times sorted so the next due time is always {@code firstKey()}.
     */
    private final TreeMap<Integer, List<String>> schedule = new TreeMap<>();

    /**
     * Undo history stack. Each entry is a two-element String array: [taskId, scheduledTime].
     * Only schedule commands are pushed; tick does not affect undo history.
     */
    private final Deque<String[]> history = new ArrayDeque<>();

    /** Logical clock advanced by each {@link #tick()} call. */
    private int currentTime = 0;

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Schedules a task to execute at the given future time.
     * If {@code time <= currentTime}, the task is queued but will execute on the
     * next tick that reaches or passes that time.
     *
     * @param taskId unique identifier for the task
     * @param time   the time at which the task should execute
     */
    public void addTask(String taskId, int time) {
        schedule.computeIfAbsent(time, k -> new ArrayList<>()).add(taskId);
        history.push(new String[]{taskId, String.valueOf(time)});
        System.out.printf("Scheduled task '%s' at t=%d%n", taskId, time);
    }

    /**
     * Advances the logical clock by 1 and executes all tasks whose scheduled time
     * equals the new current time. Returns the list of executed task IDs.
     *
     * <p>Tasks scheduled in the past (time &lt; currentTime) are not re-executed;
     * they are simply absent from the schedule.
     *
     * @return list of task IDs executed on this tick (empty if none due)
     */
    public List<String> tick() {
        currentTime++;
        System.out.printf("[TICK] t=%d%n", currentTime);
        List<String> executed = new ArrayList<>();
        if (schedule.containsKey(currentTime)) {
            executed = schedule.remove(currentTime);
            for (String id : executed) {
                System.out.printf("  -> Executed task '%s'%n", id);
            }
        }
        return executed;
    }

    /**
     * Undoes the most recently scheduled task that is still pending.
     * Has no effect (prints a message) if there is nothing to undo or if
     * the last scheduled task has already been executed.
     */
    public void undo() {
        // skip past already-executed tasks at the top of the history
        while (!history.isEmpty()) {
            String[] last = history.peek();
            int scheduledTime = Integer.parseInt(last[1]);
            if (scheduledTime <= currentTime) {
                // already executed (or at current time which was just ticked)
                history.pop();
                System.out.printf("UNDO: task '%s' was already executed at t=%d%n",
                        last[0], scheduledTime);
                return;
            }
            history.pop();
            String taskId = last[0];
            List<String> tasks = schedule.get(scheduledTime);
            if (tasks != null) {
                tasks.remove(taskId);  // removes first occurrence
                if (tasks.isEmpty()) schedule.remove(scheduledTime);
                System.out.printf("UNDO: removed task '%s' (was scheduled at t=%d)%n",
                        taskId, scheduledTime);
            }
            return;
        }
        System.out.println("UNDO: nothing to undo");
    }

    /**
     * Prints all pending tasks in ascending time order.
     * Tasks already executed are not shown.
     */
    public void printSchedule() {
        if (schedule.isEmpty()) {
            System.out.printf("Schedule at t=%d: (empty)%n", currentTime);
            return;
        }
        System.out.printf("Schedule at t=%d:%n", currentTime);
        for (Map.Entry<Integer, List<String>> e : schedule.entrySet()) {
            System.out.printf("  t=%-3d -> %s%n", e.getKey(), e.getValue());
        }
    }

    /** Returns the current logical time. */
    public int getCurrentTime() { return currentTime; }

    // ── Main ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        TaskScheduler ts = new TaskScheduler();

        // Schedule tasks
        ts.addTask("T1", 3);
        ts.addTask("T2", 5);
        ts.addTask("T3", 3);   // same time as T1
        ts.addTask("T4", 7);

        ts.printSchedule();
        // Expected: t=3 -> [T1, T3], t=5 -> [T2], t=7 -> [T4]

        System.out.println();

        // Undo last scheduled task (T4)
        ts.undo();
        ts.printSchedule();
        // Expected: t=3 -> [T1, T3], t=5 -> [T2]

        System.out.println();

        // Tick to t=1 — nothing due
        List<String> r1 = ts.tick();
        System.out.println("executed: " + r1);
        // Expected: []

        // Tick to t=2 — nothing due
        List<String> r2 = ts.tick();
        System.out.println("executed: " + r2);
        // Expected: []

        // Tick to t=3 — T1 and T3 execute
        List<String> r3 = ts.tick();
        System.out.println("executed: " + r3);
        // Expected: [T1, T3]

        System.out.println();

        // Try to undo T3 (already executed at t=3)
        ts.undo();
        // Expected: "UNDO: task 'T3' was already executed at t=3"

        System.out.println();

        // Add a new task, then undo it
        ts.addTask("T5", 10);
        ts.printSchedule();
        // Expected: t=5 -> [T2], t=10 -> [T5]

        ts.undo();
        ts.printSchedule();
        // Expected: t=5 -> [T2]

        System.out.println();

        // Tick through to t=5 — T2 executes
        ts.tick(); // t=4
        List<String> r5 = ts.tick(); // t=5
        System.out.println("executed at t=5: " + r5);
        // Expected: [T2]

        System.out.println();
        ts.printSchedule();
        // Expected: (empty)
    }
}
