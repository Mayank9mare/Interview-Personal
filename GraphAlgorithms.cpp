#include <bits/stdc++.h>
using namespace std;

// Graph representations:
//   Weighted:   vector<vector<pair<int,int>>>  where pair = {neighbor, weight}
//   Unweighted: vector<vector<int>>

using WGraph = vector<vector<pair<int,int>>>;
using Graph  = vector<vector<int>>;

WGraph weightedGraph(int n, vector<vector<int>>& edges, bool directed) {
    WGraph g(n);
    for (auto& e : edges) {
        g[e[0]].push_back({e[1], e[2]});
        if (!directed) g[e[1]].push_back({e[0], e[2]});
    }
    return g;
}

Graph unweightedGraph(int n, vector<vector<int>>& edges, bool directed) {
    Graph g(n);
    for (auto& e : edges) {
        g[e[0]].push_back(e[1]);
        if (!directed) g[e[1]].push_back(e[0]);
    }
    return g;
}

// ─────────────────────────────────────────────────────────────────────────
// 1. Dijkstra — single-source shortest path (non-negative weights)
//    O((V + E) log V) with min-heap
//    Use when: GPS routing, cheapest flight, network latency
//    DOES NOT work with negative edge weights (use Bellman-Ford instead)
// ─────────────────────────────────────────────────────────────────────────
vector<int> dijkstra(WGraph& g, int src) {
    int n = g.size();
    vector<int> dist(n, INT_MAX);
    dist[src] = 0;
    priority_queue<pair<int,int>, vector<pair<int,int>>, greater<>> pq; // {dist, node}
    pq.push({0, src});
    while (!pq.empty()) {
        int d = pq.top().first, u = pq.top().second; pq.pop();
        if (d > dist[u]) continue; // stale entry — skip
        for (auto& e : g[u]) {
            int v = e.first, w = e.second;
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
                pq.push({dist[v], v});
            }
        }
    }
    return dist; // INT_MAX = unreachable
}

// Dijkstra with path reconstruction
vector<int> dijkstraPath(WGraph& g, int src, int dst) {
    int n = g.size();
    vector<int> dist(n, INT_MAX), prev(n, -1);
    dist[src] = 0;
    priority_queue<pair<int,int>, vector<pair<int,int>>, greater<>> pq;
    pq.push({0, src});
    while (!pq.empty()) {
        int d = pq.top().first, u = pq.top().second; pq.pop();
        if (d > dist[u]) continue;
        for (auto& e : g[u]) {
            int v = e.first, w = e.second;
            if (dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w; prev[v] = u;
                pq.push({dist[v], v});
            }
        }
    }
    vector<int> path;
    for (int v = dst; v != -1; v = prev[v]) path.push_back(v);
    reverse(path.begin(), path.end());
    return path;
}

// ─────────────────────────────────────────────────────────────────────────
// 2. Bellman-Ford — single-source, handles negative weights
//    O(V * E) — slower than Dijkstra but handles negative edges
//    Detect negative cycle: if any edge still relaxes after V-1 passes
// ─────────────────────────────────────────────────────────────────────────
// edges[i] = {u, v, weight}
vector<int> bellmanFord(int n, vector<vector<int>>& edges, int src) {
    vector<int> dist(n, INT_MAX);
    dist[src] = 0;
    for (int i = 0; i < n - 1; i++) // relax all edges n-1 times
        for (auto& e : edges)
            if (dist[e[0]] != INT_MAX && dist[e[0]] + e[2] < dist[e[1]])
                dist[e[1]] = dist[e[0]] + e[2];
    // nth pass — if any edge still relaxes, negative cycle exists
    for (auto& e : edges)
        if (dist[e[0]] != INT_MAX && dist[e[0]] + e[2] < dist[e[1]])
            throw runtime_error("Negative cycle detected");
    return dist;
}

// ─────────────────────────────────────────────────────────────────────────
// 3. Floyd-Warshall — all-pairs shortest path
//    O(V³) time, O(V²) space
//    Use when: need ALL pairs of distances, V is small (≤ 500)
//    Works with negative weights (not negative cycles)
// ─────────────────────────────────────────────────────────────────────────
// edges[i] = {u, v, weight}
vector<vector<int>> floydWarshall(int n, vector<vector<int>>& edges) {
    const int INF = INT_MAX / 2;
    vector<vector<int>> dist(n, vector<int>(n, INF));
    for (int i = 0; i < n; i++) dist[i][i] = 0;
    for (auto& e : edges) dist[e[0]][e[1]] = min(dist[e[0]][e[1]], e[2]);
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
struct UnionFind {
    vector<int> parent, rank_;
    int components;

    UnionFind(int n) : parent(n), rank_(n, 0), components(n) {
        iota(parent.begin(), parent.end(), 0);
    }

    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]); // path compression
        return parent[x];
    }

    bool unite(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false; // already in same set
        if (rank_[px] < rank_[py]) swap(px, py);
        parent[py] = px;
        if (rank_[px] == rank_[py]) rank_[px]++;
        components--;
        return true;
    }

    bool connected(int x, int y) { return find(x) == find(y); }
};

// ─────────────────────────────────────────────────────────────────────────
// 5. Kruskal's MST — minimum spanning tree
//    Sort edges by weight, add greedily if it doesn't form a cycle
//    O(E log E) — dominated by sorting
//    Better for sparse graphs (E ≪ V²)
// ─────────────────────────────────────────────────────────────────────────
int kruskalMST(int n, vector<vector<int>> edges) {
    sort(edges.begin(), edges.end(), [](auto& a, auto& b){ return a[2] < b[2]; });
    UnionFind uf(n);
    int weight = 0, added = 0;
    for (auto& e : edges) {
        if (uf.unite(e[0], e[1])) {
            weight += e[2]; added++;
            if (added == n - 1) break;
        }
    }
    return weight;
}

// ─────────────────────────────────────────────────────────────────────────
// 6. Prim's MST — grow tree greedily using min-heap
//    O((V + E) log V)
//    Better for dense graphs (E ≈ V²)
// ─────────────────────────────────────────────────────────────────────────
int primMST(WGraph& g) {
    int n = g.size();
    vector<bool> inMST(n, false);
    priority_queue<pair<int,int>, vector<pair<int,int>>, greater<>> pq;
    pq.push({0, 0}); // {weight, node}, start from node 0
    int totalWeight = 0;
    while (!pq.empty()) {
        int w = pq.top().first, u = pq.top().second; pq.pop();
        if (inMST[u]) continue;
        inMST[u] = true;
        totalWeight += w;
        for (auto& e : g[u])
            if (!inMST[e.first]) pq.push({e.second, e.first});
    }
    return totalWeight;
}

// ─────────────────────────────────────────────────────────────────────────
// 7. Bipartite Check — BFS 2-coloring
//    Graph is bipartite iff no odd-length cycle
//    Use when: conflict detection, task scheduling, matching
// ─────────────────────────────────────────────────────────────────────────
bool isBipartite(Graph& g) {
    int n = g.size();
    vector<int> color(n, 0); // 0=unvisited, 1=red, -1=blue
    for (int start = 0; start < n; start++) {
        if (color[start]) continue;
        queue<int> q;
        q.push(start); color[start] = 1;
        while (!q.empty()) {
            int u = q.front(); q.pop();
            for (int v : g[u]) {
                if (!color[v]) { color[v] = -color[u]; q.push(v); }
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
void sinkIsland(vector<vector<char>>& grid, int i, int j) {
    if (i < 0 || i >= (int)grid.size() || j < 0 || j >= (int)grid[0].size() || grid[i][j] != '1') return;
    grid[i][j] = '0';
    sinkIsland(grid, i+1, j); sinkIsland(grid, i-1, j);
    sinkIsland(grid, i, j+1); sinkIsland(grid, i, j-1);
}
int numIslands(vector<vector<char>> grid) {
    int count = 0;
    for (int i = 0; i < (int)grid.size(); i++)
        for (int j = 0; j < (int)grid[0].size(); j++)
            if (grid[i][j] == '1') { sinkIsland(grid, i, j); count++; }
    return count;
}

// ─────────────────────────────────────────────────────────────────────────
// 9. Shortest Path in Grid (BFS) — unweighted
//    BFS always finds shortest path in unweighted graphs
// ─────────────────────────────────────────────────────────────────────────
int shortestPathGrid(vector<vector<int>>& grid, pair<int,int> start, pair<int,int> end) {
    int m = grid.size(), n = grid[0].size();
    if (grid[start.first][start.second] == 1 || grid[end.first][end.second] == 1) return -1;
    vector<vector<bool>> visited(m, vector<bool>(n, false));
    int dirs[4][2] = {{0,1},{0,-1},{1,0},{-1,0}};
    queue<tuple<int,int,int>> q;
    q.push({start.first, start.second, 0});
    visited[start.first][start.second] = true;
    while (!q.empty()) {
        int r = get<0>(q.front()), c = get<1>(q.front()), dist = get<2>(q.front()); q.pop();
        if (r == end.first && c == end.second) return dist;
        for (auto& d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < m && nc >= 0 && nc < n && !visited[nr][nc] && grid[nr][nc] == 0) {
                visited[nr][nc] = true;
                q.push({nr, nc, dist + 1});
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
void dfsBridge(Graph& g, int u, int parent, vector<int>& disc, vector<int>& low,
               vector<bool>& visited, int& timer, vector<vector<int>>& bridges) {
    visited[u] = true;
    disc[u] = low[u] = timer++;
    for (int v : g[u]) {
        if (!visited[v]) {
            dfsBridge(g, v, u, disc, low, visited, timer, bridges);
            low[u] = min(low[u], low[v]);
            if (low[v] > disc[u]) bridges.push_back({u, v});
        } else if (v != parent) {
            low[u] = min(low[u], disc[v]);
        }
    }
}
vector<vector<int>> findBridges(int n, Graph& g) {
    vector<int> disc(n), low(n);
    vector<bool> visited(n, false);
    vector<vector<int>> bridges;
    int timer = 0;
    for (int i = 0; i < n; i++)
        if (!visited[i]) dfsBridge(g, i, -1, disc, low, visited, timer, bridges);
    return bridges;
}

// ─────────────────────────────────────────────────────────────────────────
// 11. Topological Sort — Kahn's (BFS-based)
//     O(V + E) — only for DAGs
//     If output size < n → cycle exists
// ─────────────────────────────────────────────────────────────────────────
vector<int> topologicalSort(int n, Graph& g) {
    vector<int> indegree(n, 0);
    for (int u = 0; u < n; u++) for (int v : g[u]) indegree[v]++;
    queue<int> q;
    for (int i = 0; i < n; i++) if (!indegree[i]) q.push(i);
    vector<int> order;
    while (!q.empty()) {
        int u = q.front(); q.pop(); order.push_back(u);
        for (int v : g[u]) if (--indegree[v] == 0) q.push(v);
    }
    if ((int)order.size() < n) throw runtime_error("Graph has a cycle");
    return order;
}

// ─────────────────────────────────────────────────────────────────────────
// 12. Strongly Connected Components — Kosaraju's
//     Two-pass DFS: pass 1 on original, pass 2 on reversed graph
//     O(V + E)
//     SCC: maximal set of nodes where every node reaches every other
// ─────────────────────────────────────────────────────────────────────────
void dfsFinish(Graph& g, int u, vector<bool>& visited, stack<int>& st) {
    visited[u] = true;
    for (int v : g[u]) if (!visited[v]) dfsFinish(g, v, visited, st);
    st.push(u);
}
void dfsVisit(Graph& g, int u, vector<bool>& visited) {
    visited[u] = true;
    for (int v : g[u]) if (!visited[v]) dfsVisit(g, v, visited);
}
int countSCCs(int n, Graph& g) {
    vector<bool> visited(n, false);
    stack<int> st;
    for (int i = 0; i < n; i++)
        if (!visited[i]) dfsFinish(g, i, visited, st);
    // Build reversed graph
    Graph rev(n);
    for (int u = 0; u < n; u++) for (int v : g[u]) rev[v].push_back(u);
    fill(visited.begin(), visited.end(), false);
    int sccs = 0;
    while (!st.empty()) {
        int u = st.top(); st.pop();
        if (!visited[u]) { dfsVisit(rev, u, visited); sccs++; }
    }
    return sccs;
}

// ─────────────────────────────────────────────────────────────────────────
// Demo
// ─────────────────────────────────────────────────────────────────────────
int main() {
    cout << "=== Dijkstra ===\n";
    // 0→1:4, 0→2:1, 2→1:2, 1→3:1, 2→3:5
    vector<vector<int>> wedges = {{0,1,4},{0,2,1},{2,1,2},{1,3,1},{2,3,5}};
    auto wg = weightedGraph(4, wedges, true);
    auto dists = dijkstra(wg, 0);
    cout << "Distances from 0: "; for (int d : dists) cout << d << " "; cout << "\n"; // 0 3 1 4
    auto path = dijkstraPath(wg, 0, 3);
    cout << "Path 0→3: "; for (int v : path) cout << v << " "; cout << "\n"; // 0 2 1 3

    cout << "\n=== Bellman-Ford ===\n";
    vector<vector<int>> bfedges = {{0,1,4},{0,2,1},{2,1,2},{1,3,1}};
    auto bf = bellmanFord(4, bfedges, 0);
    cout << "Distances: "; for (int d : bf) cout << d << " "; cout << "\n"; // 0 3 1 4

    cout << "\n=== Floyd-Warshall ===\n";
    vector<vector<int>> fwedges = {{0,1,3},{1,2,1},{0,2,10}};
    auto fw = floydWarshall(3, fwedges);
    cout << "0→2: " << fw[0][2] << "\n"; // 4 (via node 1: 3+1)

    cout << "\n=== Union-Find ===\n";
    UnionFind uf(5);
    uf.unite(0,1); uf.unite(1,2); uf.unite(3,4);
    cout << "0-2 connected: " << uf.connected(0,2) << "\n"; // 1
    cout << "0-3 connected: " << uf.connected(0,3) << "\n"; // 0
    cout << "Components: " << uf.components << "\n"; // 2

    cout << "\n=== Kruskal MST ===\n";
    vector<vector<int>> kedges = {{0,1,4},{0,2,3},{1,2,1},{1,3,2},{2,3,4}};
    cout << "MST weight: " << kruskalMST(4, kedges) << "\n"; // 6

    cout << "\n=== Prim MST ===\n";
    vector<vector<int>> pedges = {{0,1,4},{0,2,3},{1,2,1},{1,3,2},{2,3,4}};
    auto pg = weightedGraph(4, pedges, false);
    cout << "MST weight: " << primMST(pg) << "\n"; // 6

    cout << "\n=== Bipartite ===\n";
    vector<vector<int>> bpedges = {{0,1},{1,2},{2,3},{3,0}};
    auto bg = unweightedGraph(4, bpedges, false);
    cout << "Even cycle bipartite: " << isBipartite(bg) << "\n"; // 1
    vector<vector<int>> tredges = {{0,1},{1,2},{2,0}};
    auto tg = unweightedGraph(3, tredges, false);
    cout << "Triangle bipartite: " << isBipartite(tg) << "\n"; // 0

    cout << "\n=== Number of Islands ===\n";
    vector<vector<char>> grid = {{'1','1','0','0'},{'1','0','0','1'},{'0','0','1','1'}};
    cout << "Islands: " << numIslands(grid) << "\n"; // 3

    cout << "\n=== Shortest Path Grid ===\n";
    vector<vector<int>> maze = {{0,0,0},{0,1,0},{0,0,0}};
    cout << "Shortest (0,0)→(2,2): " << shortestPathGrid(maze, {0,0}, {2,2}) << "\n"; // 4

    cout << "\n=== Bridges ===\n";
    vector<vector<int>> bridgeEdges = {{0,1},{1,2},{2,0},{1,3}};
    auto bridgeG = unweightedGraph(4, bridgeEdges, false);
    auto br = findBridges(4, bridgeG);
    cout << "Bridges: "; for (auto& b : br) cout << "[" << b[0] << "," << b[1] << "] "; cout << "\n"; // [1,3]

    cout << "\n=== Topological Sort ===\n";
    vector<vector<int>> dagedges = {{5,2},{5,0},{4,0},{4,1},{2,3},{3,1}};
    auto dag = unweightedGraph(6, dagedges, true);
    auto topo = topologicalSort(6, dag);
    cout << "Topo order: "; for (int v : topo) cout << v << " "; cout << "\n";

    cout << "\n=== Strongly Connected Components ===\n";
    vector<vector<int>> sccedges = {{0,1},{1,2},{2,0},{1,3},{3,4}};
    auto sccG = unweightedGraph(5, sccedges, true);
    cout << "SCC count: " << countSCCs(5, sccG) << "\n"; // 3: {0,1,2}, {3}, {4}

    return 0;
}
