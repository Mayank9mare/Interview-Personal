import java.util.*;

public class GraphAlgorithms {

    // Graph representations used:
    //   Weighted:   List<int[]>[]  where int[] = {neighbor, weight}
    //   Unweighted: List<Integer>[]

    @SuppressWarnings("unchecked")
    static List<int[]>[] weightedGraph(int n, int[][] edges, boolean directed) {
        List<int[]>[] g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (int[] e : edges) {
            g[e[0]].add(new int[]{e[1], e[2]});
            if (!directed) g[e[1]].add(new int[]{e[0], e[2]});
        }
        return g;
    }

    @SuppressWarnings("unchecked")
    static List<Integer>[] unweightedGraph(int n, int[][] edges, boolean directed) {
        List<Integer>[] g = new List[n];
        for (int i = 0; i < n; i++) g[i] = new ArrayList<>();
        for (int[] e : edges) {
            g[e[0]].add(e[1]);
            if (!directed) g[e[1]].add(e[0]);
        }
        return g;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Dijkstra — single-source shortest path (non-negative weights)
    //    O((V + E) log V) with min-heap
    //    Use when: GPS routing, cheapest flight, network latency
    //    DOES NOT work with negative edge weights (use Bellman-Ford instead)
    // ─────────────────────────────────────────────────────────────────────────
    static int[] dijkstra(List<int[]>[] g, int src) {
        int n = g.length;
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]); // {dist, node}
        pq.offer(new int[]{0, src});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int d = cur[0], u = cur[1];
            if (d > dist[u]) continue; // stale entry — skip
            for (int[] e : g[u]) {
                int v = e[0], w = e[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    pq.offer(new int[]{dist[v], v});
                }
            }
        }
        return dist; // Integer.MAX_VALUE = unreachable
    }

    // Dijkstra with path reconstruction
    static List<Integer> dijkstraPath(List<int[]>[] g, int src, int dst) {
        int n = g.length;
        int[] dist = new int[n], prev = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        pq.offer(new int[]{0, src});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int d = cur[0], u = cur[1];
            if (d > dist[u]) continue;
            for (int[] e : g[u]) {
                int v = e[0], w = e[1];
                if (dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w; prev[v] = u;
                    pq.offer(new int[]{dist[v], v});
                }
            }
        }
        List<Integer> path = new ArrayList<>();
        for (int v = dst; v != -1; v = prev[v]) path.add(v);
        Collections.reverse(path);
        return path;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Bellman-Ford — single-source, handles negative weights
    //    O(V * E) — slower than Dijkstra but handles negative edges
    //    Detect negative cycle: if any edge still relaxes after V-1 passes
    // ─────────────────────────────────────────────────────────────────────────
    // edges[i] = {u, v, weight}
    static int[] bellmanFord(int n, int[][] edges, int src) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        for (int i = 0; i < n - 1; i++) // relax all edges n-1 times
            for (int[] e : edges)
                if (dist[e[0]] != Integer.MAX_VALUE && dist[e[0]] + e[2] < dist[e[1]])
                    dist[e[1]] = dist[e[0]] + e[2];
        // nth pass — if any edge still relaxes, negative cycle exists
        for (int[] e : edges)
            if (dist[e[0]] != Integer.MAX_VALUE && dist[e[0]] + e[2] < dist[e[1]])
                throw new IllegalStateException("Negative cycle detected");
        return dist;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Floyd-Warshall — all-pairs shortest path
    //    O(V³) time, O(V²) space
    //    Use when: need ALL pairs of distances, V is small (≤ 500)
    //    Works with negative weights (not negative cycles)
    // ─────────────────────────────────────────────────────────────────────────
    // edges[i] = {u, v, weight}
    static int[][] floydWarshall(int n, int[][] edges) {
        int INF = Integer.MAX_VALUE / 2;
        int[][] dist = new int[n][n];
        for (int[] row : dist) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) dist[i][i] = 0;
        for (int[] e : edges) dist[e[0]][e[1]] = Math.min(dist[e[0]][e[1]], e[2]);
        for (int k = 0; k < n; k++) // try each node as intermediate
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (dist[i][k] + dist[k][j] < dist[i][j])
                        dist[i][j] = dist[i][k] + dist[k][j];
        return dist; // dist[i][j] = shortest i→j
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Union-Find (Disjoint Set Union)
    //    Path compression + union by rank → amortized O(α(n)) ≈ O(1)
    //    Use when: connected components, cycle detection in undirected graph
    // ─────────────────────────────────────────────────────────────────────────
    static class UnionFind {
        int[] parent, rank;
        int components;

        UnionFind(int n) {
            parent = new int[n]; rank = new int[n]; components = n;
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]); // path compression
            return parent[x];
        }

        boolean union(int x, int y) {
            int px = find(x), py = find(y);
            if (px == py) return false; // already in same set (adding edge = cycle)
            if (rank[px] < rank[py]) { int t = px; px = py; py = t; }
            parent[py] = px;
            if (rank[px] == rank[py]) rank[px]++;
            components--;
            return true;
        }

        boolean connected(int x, int y) { return find(x) == find(y); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Kruskal's MST — minimum spanning tree
    //    Sort edges by weight, add greedily if it doesn't form a cycle
    //    O(E log E) — dominated by sorting
    //    Better for sparse graphs (E ≪ V²)
    // ─────────────────────────────────────────────────────────────────────────
    static int kruskalMST(int n, int[][] edges) {
        Arrays.sort(edges, (a, b) -> a[2] - b[2]); // sort by weight ascending
        UnionFind uf = new UnionFind(n);
        int weight = 0, added = 0;
        for (int[] e : edges) {
            if (uf.union(e[0], e[1])) { // no cycle → include edge
                weight += e[2]; added++;
                if (added == n - 1) break; // MST has exactly n-1 edges
            }
        }
        return weight;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Prim's MST — grow tree greedily using min-heap
    //    O((V + E) log V)
    //    Better for dense graphs (E ≈ V²)
    // ─────────────────────────────────────────────────────────────────────────
    static int primMST(List<int[]>[] g) {
        int n = g.length;
        boolean[] inMST = new boolean[n];
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]); // {weight, node}
        pq.offer(new int[]{0, 0}); // start from node 0
        int totalWeight = 0;
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int w = cur[0], u = cur[1];
            if (inMST[u]) continue;
            inMST[u] = true;
            totalWeight += w;
            for (int[] e : g[u])
                if (!inMST[e[0]]) pq.offer(new int[]{e[1], e[0]});
        }
        return totalWeight;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Bipartite Check — BFS 2-coloring
    //    Graph is bipartite iff no odd-length cycle
    //    Use when: conflict detection, task scheduling, matching
    // ─────────────────────────────────────────────────────────────────────────
    static boolean isBipartite(List<Integer>[] g) {
        int n = g.length;
        int[] color = new int[n]; // 0=unvisited, 1=red, -1=blue
        for (int start = 0; start < n; start++) {
            if (color[start] != 0) continue;
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(start); color[start] = 1;
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (int v : g[u]) {
                    if (color[v] == 0) { color[v] = -color[u]; queue.offer(v); }
                    else if (color[v] == color[u]) return false;
                }
            }
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Number of Islands — DFS on grid
    //    O(m*n) — classic 2D graph traversal
    // ─────────────────────────────────────────────────────────────────────────
    static int numIslands(char[][] grid) {
        int count = 0;
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[0].length; j++)
                if (grid[i][j] == '1') { sinkIsland(grid, i, j); count++; }
        return count;
    }
    static void sinkIsland(char[][] grid, int i, int j) {
        if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length || grid[i][j] != '1') return;
        grid[i][j] = '0'; // mark visited by sinking
        sinkIsland(grid, i+1, j); sinkIsland(grid, i-1, j);
        sinkIsland(grid, i, j+1); sinkIsland(grid, i, j-1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. Shortest Path in Grid (BFS) — unweighted
    //    BFS always finds shortest path in unweighted graphs
    // ─────────────────────────────────────────────────────────────────────────
    static int shortestPathGrid(int[][] grid, int[] start, int[] end) {
        int m = grid.length, n = grid[0].length;
        if (grid[start[0]][start[1]] == 1 || grid[end[0]][end[1]] == 1) return -1;
        boolean[][] visited = new boolean[m][n];
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{start[0], start[1], 0});
        visited[start[0]][start[1]] = true;
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1], dist = cur[2];
            if (r == end[0] && c == end[1]) return dist;
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nr < m && nc >= 0 && nc < n && !visited[nr][nc] && grid[nr][nc] == 0) {
                    visited[nr][nc] = true;
                    q.offer(new int[]{nr, nc, dist + 1});
                }
            }
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. Tarjan's Bridge Detection — find critical edges
    //     Removal of a bridge disconnects the graph
    //     O(V + E)
    //     Use when: network resilience, critical connection problems
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> findBridges(int n, List<Integer>[] g) {
        int[] disc = new int[n], low = new int[n];
        boolean[] visited = new boolean[n];
        List<List<Integer>> bridges = new ArrayList<>();
        int[] timer = {0};
        for (int i = 0; i < n; i++)
            if (!visited[i]) dfsBridge(g, i, -1, disc, low, visited, timer, bridges);
        return bridges;
    }
    static void dfsBridge(List<Integer>[] g, int u, int parent, int[] disc, int[] low,
                          boolean[] visited, int[] timer, List<List<Integer>> bridges) {
        visited[u] = true;
        disc[u] = low[u] = timer[0]++;
        for (int v : g[u]) {
            if (!visited[v]) {
                dfsBridge(g, v, u, disc, low, visited, timer, bridges);
                low[u] = Math.min(low[u], low[v]);
                if (low[v] > disc[u]) bridges.add(List.of(u, v)); // bridge: no back-edge skips u
            } else if (v != parent) {
                low[u] = Math.min(low[u], disc[v]); // back edge
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 11. Topological Sort — Kahn's (BFS-based)
    //     O(V + E) — only for DAGs
    //     If output size < n → cycle exists
    // ─────────────────────────────────────────────────────────────────────────
    static int[] topologicalSort(int n, List<Integer>[] g) {
        int[] indegree = new int[n];
        for (int u = 0; u < n; u++) for (int v : g[u]) indegree[v]++;
        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < n; i++) if (indegree[i] == 0) q.offer(i);
        int[] order = new int[n]; int idx = 0;
        while (!q.isEmpty()) {
            int u = q.poll(); order[idx++] = u;
            for (int v : g[u]) if (--indegree[v] == 0) q.offer(v);
        }
        if (idx < n) throw new IllegalStateException("Graph has a cycle");
        return order;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 12. Strongly Connected Components — Kosaraju's
    //     Two-pass DFS: pass 1 on original, pass 2 on reversed graph
    //     O(V + E)
    //     SCC: maximal set of nodes where every node reaches every other
    // ─────────────────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    static int countSCCs(int n, List<Integer>[] g) {
        // Pass 1: fill finish order
        boolean[] visited = new boolean[n];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < n; i++)
            if (!visited[i]) dfsFinish(g, i, visited, stack);
        // Build reversed graph
        List<Integer>[] rev = new List[n];
        for (int i = 0; i < n; i++) rev[i] = new ArrayList<>();
        for (int u = 0; u < n; u++) for (int v : g[u]) rev[v].add(u);
        // Pass 2: DFS on reversed graph in finish order
        Arrays.fill(visited, false);
        int sccs = 0;
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (!visited[u]) { dfsVisit(rev, u, visited); sccs++; }
        }
        return sccs;
    }
    static void dfsFinish(List<Integer>[] g, int u, boolean[] visited, Deque<Integer> stack) {
        visited[u] = true;
        for (int v : g[u]) if (!visited[v]) dfsFinish(g, v, visited, stack);
        stack.push(u);
    }
    static void dfsVisit(List<Integer>[] g, int u, boolean[] visited) {
        visited[u] = true;
        for (int v : g[u]) if (!visited[v]) dfsVisit(g, v, visited);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Demo
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("=== Dijkstra ===");
        // 0→1:4, 0→2:1, 2→1:2, 1→3:1, 2→3:5
        var wg = weightedGraph(4, new int[][]{{0,1,4},{0,2,1},{2,1,2},{1,3,1},{2,3,5}}, true);
        System.out.println("Distances from 0: " + Arrays.toString(dijkstra(wg, 0))); // [0,3,1,4]
        System.out.println("Path 0→3: " + dijkstraPath(wg, 0, 3)); // [0, 2, 1, 3]

        System.out.println("\n=== Bellman-Ford ===");
        int[] bf = bellmanFord(4, new int[][]{{0,1,4},{0,2,1},{2,1,2},{1,3,1}}, 0);
        System.out.println("Distances: " + Arrays.toString(bf)); // [0,3,1,4]

        System.out.println("\n=== Floyd-Warshall ===");
        int[][] fw = floydWarshall(3, new int[][]{{0,1,3},{1,2,1},{0,2,10}});
        System.out.println("0→2: " + fw[0][2]); // 4 (via node 1: 3+1)

        System.out.println("\n=== Union-Find ===");
        UnionFind uf = new UnionFind(5);
        uf.union(0,1); uf.union(1,2); uf.union(3,4);
        System.out.println("0-2 connected: " + uf.connected(0,2)); // true
        System.out.println("0-3 connected: " + uf.connected(0,3)); // false
        System.out.println("Components: " + uf.components); // 2

        System.out.println("\n=== Kruskal MST ===");
        int kw = kruskalMST(4, new int[][]{{0,1,4},{0,2,3},{1,2,1},{1,3,2},{2,3,4}});
        System.out.println("MST weight: " + kw); // 6

        System.out.println("\n=== Prim MST ===");
        var pg = weightedGraph(4, new int[][]{{0,1,4},{0,2,3},{1,2,1},{1,3,2},{2,3,4}}, false);
        System.out.println("MST weight: " + primMST(pg)); // 6

        System.out.println("\n=== Bipartite ===");
        var bg = unweightedGraph(4, new int[][]{{0,1},{1,2},{2,3},{3,0}}, false); // square
        System.out.println("Even cycle bipartite: " + isBipartite(bg)); // true
        var tg = unweightedGraph(3, new int[][]{{0,1},{1,2},{2,0}}, false); // triangle
        System.out.println("Triangle bipartite: " + isBipartite(tg)); // false

        System.out.println("\n=== Number of Islands ===");
        char[][] grid = {
            {'1','1','0','0'},
            {'1','0','0','1'},
            {'0','0','1','1'}
        };
        System.out.println("Islands: " + numIslands(grid)); // 3

        System.out.println("\n=== Shortest Path Grid ===");
        int[][] maze = {{0,0,0},{0,1,0},{0,0,0}};
        System.out.println("Shortest (0,0)→(2,2): " + shortestPathGrid(maze, new int[]{0,0}, new int[]{2,2})); // 4

        System.out.println("\n=== Bridges ===");
        var bridgeG = unweightedGraph(4, new int[][]{{0,1},{1,2},{2,0},{1,3}}, false);
        System.out.println("Bridges: " + findBridges(4, bridgeG)); // [[1,3]]

        System.out.println("\n=== Topological Sort ===");
        var dag = unweightedGraph(6, new int[][]{{5,2},{5,0},{4,0},{4,1},{2,3},{3,1}}, true);
        System.out.println("Topo order: " + Arrays.toString(topologicalSort(6, dag)));

        System.out.println("\n=== Strongly Connected Components ===");
        var sccG = unweightedGraph(5, new int[][]{{0,1},{1,2},{2,0},{1,3},{3,4}}, true);
        System.out.println("SCC count: " + countSCCs(5, sccG)); // 3: {0,1,2}, {3}, {4}
    }
}
