import java.util.*;

/**
 * Entry point demonstrating {@link OrgChart}.
 * Compile: {@code javac OrgTree.java}  Run: {@code java OrgTree}
 */
public class OrgTree {

    /**
     * A single employee node in the org-tree.
     *
     * <p>Maintains a bidirectional link to its manager and an ordered list of direct reports.
     */
    static class Employee {
        /** Unique employee identifier. */
        final String id;

        /** Direct manager; {@code null} only for the CEO root. */
        Employee manager;

        /** Ordered list of direct reports. */
        final List<Employee> reports = new ArrayList<>();

        /** @param id unique identifier */
        Employee(String id) { this.id = id; }

        /** Links {@code e} as a direct report and sets its manager pointer. */
        void addReport(Employee e) {
            reports.add(e);
            e.manager = this;
        }

        /** Removes {@code e} from the direct-report list. */
        void removeReport(Employee e) { reports.remove(e); }

        @Override public String toString() { return id; }
    }

    /**
     * Org-tree with a height-cap operation that minimises new CEO direct reports.
     *
     * <p>The core operation {@link #flattenToHeight(int)} ensures the tree height
     * is at most {@code h} by reparenting any node at depth {@code > h} directly
     * under the CEO. Moving the shallowest violator (depth {@code h+1}) is greedy:
     * it drags the entire sub-tree upward, minimising how many descendants still
     * violate the cap after the move.
     *
     * <p>Algorithm — single DFS, O(n):
     * <pre>
     *   flattenDFS(node, depth):
     *     if depth > h: reparent node → CEO, depth = 1
     *     for each child: flattenDFS(child, depth + 1)
     * </pre>
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class OrgChart {
        /** The root (CEO) of the hierarchy. */
        private final Employee ceo;

        /** All employees by ID for O(1) lookup. */
        private final Map<String, Employee> roster = new HashMap<>();

        /** Employees moved to CEO during the most recent {@link #flattenToHeight} call. */
        private final Set<Employee> movedToCeo = new HashSet<>();

        /**
         * Creates an org-chart with the given CEO as the root.
         *
         * @param ceoId unique identifier for the CEO
         */
        OrgChart(String ceoId) {
            ceo = new Employee(ceoId);
            roster.put(ceoId, ceo);
        }

        /**
         * Adds an employee under the given manager (or re-parents an existing one).
         *
         * @param id        the employee's unique ID
         * @param managerId the manager's ID (must already exist in the roster)
         * @throws IllegalArgumentException if {@code managerId} is not in the roster
         */
        public void addEmployee(String id, String managerId) {
            Employee mgr = get(managerId);
            Employee emp = roster.computeIfAbsent(id, Employee::new);
            if (emp.manager != null) emp.manager.removeReport(emp);
            mgr.addReport(emp);
        }

        // ── Core operation ────────────────────────────────────────────────────

        /**
         * Restructures the tree so that its height is at most {@code h}.
         * Any employee deeper than {@code h} levels below the CEO is moved to
         * report directly to the CEO. Their sub-trees move with them.
         *
         * @param h the maximum allowed tree height (CEO = depth 0; direct reports = depth 1)
         */
        public void flattenToHeight(int h) {
            movedToCeo.clear();
            for (Employee child : new ArrayList<>(ceo.reports)) {
                flattenDFS(child, 1, h);
            }
        }

        /** DFS worker — reparents {@code node} to CEO if {@code depth > h}, then recurses. */
        private void flattenDFS(Employee node, int depth, int h) {
            if (depth > h) {
                // Reparent: detach from current manager, attach to CEO
                node.manager.removeReport(node);
                ceo.addReport(node);
                movedToCeo.add(node);
                depth = 1;          // node is now a direct CEO report
            }
            // Visit children with updated depth
            for (Employee child : new ArrayList<>(node.reports)) {
                flattenDFS(child, depth + 1, h);
            }
        }

        // ── Helpers ───────────────────────────────────────────────────────────

        /**
         * Returns the height of the tree (max depth of any employee below CEO).
         * An org with only the CEO has height 0.
         *
         * @return tree height
         */
        public int height() { return subtreeHeight(ceo) - 1; }

        /** Recursive height of the subtree rooted at {@code e}. */
        private int subtreeHeight(Employee e) {
            if (e.reports.isEmpty()) return 1;
            int max = 0;
            for (Employee c : e.reports) max = Math.max(max, subtreeHeight(c));
            return 1 + max;
        }

        /** Returns the number of direct reports the CEO currently has. */
        public int ceoDegree() { return ceo.reports.size(); }

        /** Looks up an employee by ID, throwing if not found. */
        private Employee get(String id) {
            Employee e = roster.get(id);
            if (e == null) throw new IllegalArgumentException("Unknown: " + id);
            return e;
        }

        // ── Visualisation ─────────────────────────────────────────────────────

        /** Prints the full tree with height, CEO degree, and move annotations. */
        public void printTree() {
            System.out.printf("Org tree  height=%d  CEO direct reports=%d%n",
                              height(), ceoDegree());
            System.out.println(ceo.id + " [CEO]");
            printChildren(ceo, "");
        }

        /** Recursive ASCII-tree printer with move markers. */
        private void printChildren(Employee node, String prefix) {
            List<Employee> children = node.reports;
            for (int i = 0; i < children.size(); i++) {
                Employee child = children.get(i);
                boolean last   = (i == children.size() - 1);
                String  marker = movedToCeo.contains(child) ? " (↑ moved)" : "";
                System.out.println(prefix + (last ? "└── " : "├── ") + child.id + marker);
                printChildren(child, prefix + (last ? "    " : "│   "));
            }
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        // ── 1. Linear chain ──────────────────────────────────────────────────
        System.out.println("══════════════════════════════════════════");
        System.out.println(" Example 1: linear chain  CEO→A→B→C→D→E ");
        System.out.println("══════════════════════════════════════════");
        OrgChart o1 = new OrgChart("CEO");
        for (String[] p : new String[][]{{"A","CEO"},{"B","A"},{"C","B"},{"D","C"},{"E","D"}})
            o1.addEmployee(p[0], p[1]);

        System.out.println("Before:");
        o1.printTree();

        o1.flattenToHeight(2);
        System.out.println("\nAfter flattenToHeight(2):");
        o1.printTree();
        // Moved: C (was depth 3→1), E (depth 3 after C's move → 1)
        // CEO reports: A, C, E  — minimum possible

        // ── 2. Wide-and-deep tree ────────────────────────────────────────────
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 2: bushy tree, h=2             ");
        System.out.println("══════════════════════════════════════════");
        //        CEO
        //       / | \
        //      A  B  C
        //     /|   \
        //    D  E   F
        //   /       \
        //  G          H
        OrgChart o2 = new OrgChart("CEO");
        for (String[] p : new String[][]{
                {"A","CEO"},{"B","CEO"},{"C","CEO"},
                {"D","A"},{"E","A"},{"F","B"},
                {"G","D"},{"H","F"}})
            o2.addEmployee(p[0], p[1]);

        System.out.println("Before:");
        o2.printTree();

        o2.flattenToHeight(2);
        System.out.println("\nAfter flattenToHeight(2):");
        o2.printTree();
        // G (depth 3) → CEO; H (depth 3) → CEO
        // Only G and H are new CEO reports — subtrees stay intact

        // ── 3. h=1 (fully flat) ──────────────────────────────────────────────
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 3: h=1 (fully flat)             ");
        System.out.println("══════════════════════════════════════════");
        OrgChart o3 = new OrgChart("CEO");
        for (String[] p : new String[][]{{"A","CEO"},{"B","A"},{"C","B"}})
            o3.addEmployee(p[0], p[1]);

        System.out.println("Before:");
        o3.printTree();

        o3.flattenToHeight(1);
        System.out.println("\nAfter flattenToHeight(1):");
        o3.printTree();
        // B (depth 2→1), C (still depth 2 after B's move → 1). All 3 under CEO.

        // ── 4. Subtree deep enough to need two rounds of moving ───────────────
        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 4: double-violation, h=2        ");
        System.out.println("══════════════════════════════════════════");
        // CEO → A → B → C → D → E → F
        OrgChart o4 = new OrgChart("CEO");
        for (String[] p : new String[][]{{"A","CEO"},{"B","A"},{"C","B"},
                                          {"D","C"},{"E","D"},{"F","E"}})
            o4.addEmployee(p[0], p[1]);

        System.out.println("Before:");
        o4.printTree();

        o4.flattenToHeight(2);
        System.out.println("\nAfter flattenToHeight(2):");
        o4.printTree();
        // C (depth 3→1), E (depth 3 after C's move → 1), F still at depth 2. OK.
        // CEO reports: A, C, E  (F is under E, depth 2 ≤ h) — minimum!
    }
}
