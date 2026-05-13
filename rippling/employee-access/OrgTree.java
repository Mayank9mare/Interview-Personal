import java.util.*;

public class OrgTree {

    // ═══════════════════════════════════════════════════════════════════════════
    // Org-Tree Height Cap
    //
    // Goal: ensure tree height ≤ h while minimizing new CEO direct reportees.
    //
    // Rule: any employee at depth > h reports directly to the CEO.
    //       Subordinates of a moved employee stay in the same relative structure.
    //
    // Why greedily move the SHALLOWEST violator (depth h+1)?
    //   Moving a node at depth h+1 to CEO (depth 1) pulls its entire subtree
    //   up by h levels. Every descendant at depth h+1+k is now at depth 1+k.
    //   Only descendants where 1+k > h (i.e. k > h-1) will still violate —
    //   far fewer than if we moved deeper nodes individually.
    //   Moving a deeper node instead leaves shallower violators untouched.
    //
    // Algorithm (single DFS, O(n) time):
    //   flattenDFS(node, depth):
    //     if depth > h:
    //       reparent node → CEO, set depth = 1
    //     for each child:
    //       flattenDFS(child, depth + 1)      ← uses the (possibly updated) depth
    //
    // Complexity:
    //   Time  O(n)  — every node visited exactly once
    //   Space O(n)  — tree storage + O(h) recursion stack in the balanced case,
    //                 O(n) worst case (linear chain)
    // ═══════════════════════════════════════════════════════════════════════════

    static class Employee {
        final String id;
        Employee manager;
        final List<Employee> reports = new ArrayList<>();

        Employee(String id) { this.id = id; }

        void addReport(Employee e) {
            reports.add(e);
            e.manager = this;
        }

        void removeReport(Employee e) { reports.remove(e); }

        @Override public String toString() { return id; }
    }

    static class OrgChart {
        private final Employee ceo;
        private final Map<String, Employee> roster = new HashMap<>();
        // Tracks who was moved to CEO during the last flatten call
        private final Set<Employee> movedToCeo = new HashSet<>();

        OrgChart(String ceoId) {
            ceo = new Employee(ceoId);
            roster.put(ceoId, ceo);
        }

        public void addEmployee(String id, String managerId) {
            Employee mgr = get(managerId);
            Employee emp = roster.computeIfAbsent(id, Employee::new);
            if (emp.manager != null) emp.manager.removeReport(emp);
            mgr.addReport(emp);
        }

        // ── Core operation ────────────────────────────────────────────────────

        public void flattenToHeight(int h) {
            movedToCeo.clear();
            for (Employee child : new ArrayList<>(ceo.reports)) {
                flattenDFS(child, 1, h);
            }
        }

        // node is at `depth` edges below CEO (CEO = 0; direct reports = 1)
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

        public int height() { return subtreeHeight(ceo) - 1; } // exclude CEO level

        private int subtreeHeight(Employee e) {
            if (e.reports.isEmpty()) return 1;
            int max = 0;
            for (Employee c : e.reports) max = Math.max(max, subtreeHeight(c));
            return 1 + max;
        }

        public int ceoDegree() { return ceo.reports.size(); }

        private Employee get(String id) {
            Employee e = roster.get(id);
            if (e == null) throw new IllegalArgumentException("Unknown: " + id);
            return e;
        }

        // ── Visualisation ─────────────────────────────────────────────────────

        public void printTree() {
            System.out.printf("Org tree  height=%d  CEO direct reports=%d%n",
                              height(), ceoDegree());
            System.out.println(ceo.id + " [CEO]");
            printChildren(ceo, "");
        }

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
