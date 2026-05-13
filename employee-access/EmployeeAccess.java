import java.util.*;

public class EmployeeAccess {

    // ═══════════════════════════════════════════════════════════════════════════
    // Employee Access Management System
    //
    //   grant(employeeId, resource)          — give employee direct access
    //   revoke(employeeId, resource)         — remove direct access
    //   get(employeeId)                      — all resources this employee can access
    //   hasAccess(employeeId, resource)      — boolean check; supports wildcard ("data/*")
    //
    // Extended with Role-Based Access Control (RBAC):
    //   createRole(roleId)
    //   grantToRole(roleId, resource)        — add resource to a role
    //   revokeFromRole(roleId, resource)
    //   assignRole(employeeId, roleId)       — add employee to a role
    //   unassignRole(employeeId, roleId)
    //   extendRole(childRole, parentRole)    — child inherits all parent resources
    //
    // Design:
    //   directGrants   : employeeId → Set<resource>
    //   employeeRoles  : employeeId → Set<roleId>
    //   roleResources  : roleId     → Set<resource>
    //   roleParents    : roleId     → Set<parentRoleId>   (DAG — cycles guarded via visited set)
    //
    //   get(employeeId) = directGrants(employee)
    //                   ∪ BFS/DFS over role hierarchy collecting roleResources at each node
    //
    //   Why BFS over the role graph?
    //     Roles can form a hierarchy (superadmin → admin → viewer).
    //     BFS with a visited-set handles diamonds and cycles correctly.
    //
    // Complexity:
    //   grant / revoke   O(1)
    //   get              O(E + R·P)  E=direct grants, R=reachable roles, P=perms per role
    //   hasAccess        O(get) — linear scan of effective permissions for wildcard check
    // ═══════════════════════════════════════════════════════════════════════════
    static class AccessManager {

        private final Map<String, Set<String>> directGrants   = new HashMap<>();
        private final Map<String, Set<String>> employeeRoles  = new HashMap<>();
        private final Map<String, Set<String>> roleResources  = new HashMap<>();
        private final Map<String, Set<String>> roleParents    = new HashMap<>(); // child → parents

        // ── Employee-level grant / revoke ─────────────────────────────────────

        public void grant(String employeeId, String resource) {
            directGrants.computeIfAbsent(employeeId, k -> new HashSet<>()).add(resource);
        }

        public void revoke(String employeeId, String resource) {
            Set<String> g = directGrants.get(employeeId);
            if (g != null) g.remove(resource);
        }

        // ── Effective permission lookup ───────────────────────────────────────

        // Returns every resource this employee can access (direct + all inherited roles)
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

        // Check access to a specific resource.
        // A granted "prefix/*" covers any resource starting with "prefix/".
        // Example: grant("data/*") → hasAccess("data/users") == true
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

        public void createRole(String roleId) {
            roleResources.putIfAbsent(roleId, new HashSet<>());
        }

        public void grantToRole(String roleId, String resource) {
            roleResources.computeIfAbsent(roleId, k -> new HashSet<>()).add(resource);
        }

        public void revokeFromRole(String roleId, String resource) {
            Set<String> r = roleResources.get(roleId);
            if (r != null) r.remove(resource);
        }

        public void assignRole(String employeeId, String roleId) {
            employeeRoles.computeIfAbsent(employeeId, k -> new HashSet<>()).add(roleId);
        }

        public void unassignRole(String employeeId, String roleId) {
            Set<String> r = employeeRoles.get(employeeId);
            if (r != null) r.remove(roleId);
        }

        // childRole inherits every resource that parentRole has (and all of parentRole's ancestors)
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
