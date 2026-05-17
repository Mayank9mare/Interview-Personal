import java.util.*;

/**
 * Entry point demonstrating {@link AccessManager}.
 * Compile: {@code javac EmployeeAccess.java}  Run: {@code java EmployeeAccess}
 */
public class EmployeeAccess {

    /**
     * Role-Based Access Control (RBAC) system with role inheritance.
     *
     * <p>Core data structures:
     * <ul>
     *   <li>{@code directGrants}: employeeId → {@code Set<resource>} — resources granted
     *       directly to an employee, independent of any role.</li>
     *   <li>{@code employeeRoles}: employeeId → {@code Set<roleId>} — roles assigned to
     *       an employee.</li>
     *   <li>{@code roleResources}: roleId → {@code Set<resource>} — resources attached to
     *       a role.</li>
     *   <li>{@code roleParents}: roleId → {@code Set<parentRoleId>} — directed acyclic graph
     *       of role inheritance; a child role inherits all ancestor resources.</li>
     * </ul>
     *
     * <p>Effective permissions for an employee = directGrants ∪ BFS over the role DAG
     * collecting {@code roleResources} at every reachable node. The visited-set guards
     * against diamond dependencies and cycles.
     *
     * <p>Wildcard resources: a granted {@code "prefix/*"} covers any resource whose path
     * starts with {@code "prefix/"}.
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class AccessManager {

        /** Direct resource grants per employee, independent of roles. */
        private final Map<String, Set<String>> directGrants   = new HashMap<>();

        /** Role memberships per employee. */
        private final Map<String, Set<String>> employeeRoles  = new HashMap<>();

        /** Resources attached to each role. */
        private final Map<String, Set<String>> roleResources  = new HashMap<>();

        /** Role inheritance DAG: child → set of direct parent role IDs. */
        private final Map<String, Set<String>> roleParents    = new HashMap<>();

        // ── Employee-level grant / revoke ─────────────────────────────────────

        /**
         * Grants {@code resource} directly to {@code employeeId}.
         *
         * @param employeeId the employee receiving access
         * @param resource   the resource string (may include wildcard, e.g. {@code "data/*"})
         */
        public void grant(String employeeId, String resource) {
            directGrants.computeIfAbsent(employeeId, k -> new HashSet<>()).add(resource);
        }

        /**
         * Removes a direct resource grant from {@code employeeId}. No-op if not granted.
         *
         * @param employeeId the employee
         * @param resource   the resource to revoke
         */
        public void revoke(String employeeId, String resource) {
            Set<String> g = directGrants.get(employeeId);
            if (g != null) g.remove(resource);
        }

        // ── Effective permission lookup ───────────────────────────────────────

        /**
         * Returns every resource this employee can access, combining direct grants with
         * resources inherited from all reachable roles via BFS.
         *
         * @param employeeId the employee to look up
         * @return mutable set of all effective resource strings; empty if none
         */
        public Set<String> get(String employeeId) {
            Set<String> result = new HashSet<>(
                directGrants.getOrDefault(employeeId, Collections.emptySet()));

            // BFS through the role DAG
            Set<String> visitedRoles = new HashSet<>();
            Deque<String> queue = new ArrayDeque<>(
                employeeRoles.getOrDefault(employeeId, Collections.emptySet()));

            while (!queue.isEmpty()) {
                String role = queue.poll();
                if (!visitedRoles.add(role)) continue;     // already processed (cycle/diamond guard)
                result.addAll(roleResources.getOrDefault(role, Collections.emptySet()));
                queue.addAll(roleParents.getOrDefault(role, Collections.emptySet()));
            }

            return result;
        }

        /**
         * Returns {@code true} if the employee has access to {@code resource}.
         * A granted {@code "prefix/*"} wildcard covers any resource starting with {@code "prefix/"}.
         *
         * @param employeeId the employee to check
         * @param resource   the specific resource to test (e.g. {@code "data/users"})
         * @return true if the employee has a direct or role-inherited grant covering this resource
         */
        public boolean hasAccess(String employeeId, String resource) {
            for (String granted : get(employeeId)) {
                if (granted.equals(resource)) return true;
                if (granted.endsWith("/*")) {
                    String prefix = granted.substring(0, granted.length() - 1); // "data/"
                    if (resource.startsWith(prefix)) return true;
                }
            }
            return false;
        }

        // ── Role management ───────────────────────────────────────────────────

        /**
         * Creates an empty role. No-op if the role already exists.
         *
         * @param roleId unique identifier for the role
         */
        public void createRole(String roleId) {
            roleResources.putIfAbsent(roleId, new HashSet<>());
        }

        /**
         * Adds {@code resource} to the given role's permission set.
         *
         * @param roleId   the role to modify
         * @param resource the resource to grant
         */
        public void grantToRole(String roleId, String resource) {
            roleResources.computeIfAbsent(roleId, k -> new HashSet<>()).add(resource);
        }

        /**
         * Removes {@code resource} from the given role's permission set. No-op if absent.
         *
         * @param roleId   the role to modify
         * @param resource the resource to revoke
         */
        public void revokeFromRole(String roleId, String resource) {
            Set<String> r = roleResources.get(roleId);
            if (r != null) r.remove(resource);
        }

        /**
         * Assigns {@code roleId} to the employee, granting all role resources on next {@link #get}.
         *
         * @param employeeId the employee to update
         * @param roleId     the role to assign
         */
        public void assignRole(String employeeId, String roleId) {
            employeeRoles.computeIfAbsent(employeeId, k -> new HashSet<>()).add(roleId);
        }

        /**
         * Removes {@code roleId} from the employee's role membership. No-op if not assigned.
         *
         * @param employeeId the employee to update
         * @param roleId     the role to remove
         */
        public void unassignRole(String employeeId, String roleId) {
            Set<String> r = employeeRoles.get(employeeId);
            if (r != null) r.remove(roleId);
        }

        /**
         * Makes {@code childRoleId} inherit all resources of {@code parentRoleId} and its
         * ancestors. The inheritance edge is added to the role DAG.
         *
         * @param childRoleId  the role that will inherit
         * @param parentRoleId the role whose resources are inherited
         */
        public void extendRole(String childRoleId, String parentRoleId) {
            roleParents.computeIfAbsent(childRoleId, k -> new HashSet<>()).add(parentRoleId);
        }
    }

    public static void main(String[] args) {
        AccessManager am = new AccessManager();

        // ── 1. Direct grants ─────────────────────────────────────────────────
        System.out.println("=== Direct grants ===");
        am.grant("alice", "file:read");
        am.grant("alice", "file:write");
        am.grant("bob",   "file:read");

        System.out.println("alice: " + am.get("alice"));  // [file:read, file:write]
        System.out.println("bob:   " + am.get("bob"));    // [file:read]

        am.revoke("alice", "file:write");
        System.out.println("alice after revoke: " + am.get("alice")); // [file:read]

        // ── 2. Role-based access ─────────────────────────────────────────────
        System.out.println("\n=== Role-based grants ===");
        am.createRole("viewer");
        am.grantToRole("viewer", "report:read");
        am.grantToRole("viewer", "dashboard:view");

        am.createRole("editor");
        am.grantToRole("editor", "report:write");
        am.extendRole("editor", "viewer");   // editor inherits viewer's permissions

        am.assignRole("alice", "editor");
        System.out.println("alice (editor→viewer): " + am.get("alice"));
        // [file:read, report:write, report:read, dashboard:view]

        am.assignRole("bob", "viewer");
        System.out.println("bob   (viewer):        " + am.get("bob"));
        // [file:read, report:read, dashboard:view]

        // ── 3. Deep role hierarchy ────────────────────────────────────────────
        System.out.println("\n=== Deep role hierarchy ===");
        am.createRole("admin");
        am.grantToRole("admin", "db:read");
        am.grantToRole("admin", "db:write");
        am.extendRole("admin", "editor");    // admin → editor → viewer

        am.createRole("superadmin");
        am.grantToRole("superadmin", "system:shutdown");
        am.extendRole("superadmin", "admin"); // superadmin → admin → editor → viewer

        am.assignRole("charlie", "superadmin");
        System.out.println("charlie (superadmin→admin→editor→viewer): ");
        System.out.println("  " + am.get("charlie"));
        // system:shutdown + db:read/write + report:write + report:read + dashboard:view

        // ── 4. Wildcard access ────────────────────────────────────────────────
        System.out.println("\n=== Wildcard access ===");
        am.grant("dave", "data/*");
        System.out.println("dave get:                      " + am.get("dave")); // [data/*]
        System.out.println("hasAccess(data/users):         " + am.hasAccess("dave", "data/users"));  // true
        System.out.println("hasAccess(data/logs/archive):  " + am.hasAccess("dave", "data/logs/archive")); // true
        System.out.println("hasAccess(logs/error):         " + am.hasAccess("dave", "logs/error"));  // false
        System.out.println("hasAccess(data/*) (exact):     " + am.hasAccess("dave", "data/*"));      // true

        // ── 5. Revoke role ────────────────────────────────────────────────────
        System.out.println("\n=== Unassign role ===");
        am.unassignRole("alice", "editor");
        System.out.println("alice after unassigning editor: " + am.get("alice")); // only [file:read]

        // ── 6. No access ─────────────────────────────────────────────────────
        System.out.println("\n=== Unknown employee ===");
        System.out.println("eve get: " + am.get("eve")); // []
        System.out.println("hasAccess(file:read): " + am.hasAccess("eve", "file:read")); // false
    }
}
