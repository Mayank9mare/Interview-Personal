#include <bits/stdc++.h>
using namespace std;

// ─────────────────────────────────────────────────────────────────────────
// Dynamic Programming Reference — C++
// Patterns sourced from: classic problems + Coding-Journey-1.0/DP repo
// ─────────────────────────────────────────────────────────────────────────
//
// ═════════════════════════════════════════════════════════════════════════
// MEMORY PALACE — A Movie Theater (15 stations, outside → inside → back)
// ═════════════════════════════════════════════════════════════════════════
//
//   Station  1  PARKING LOT       → FIBONACCI / CLIMBING STAIRS
//   Station  2  TICKET BOOTH      → 0/1 KNAPSACK
//   Station  3  CONCESSION STAND  → UNBOUNDED KNAPSACK / COIN CHANGE MIN
//   Station  4  SODA FOUNTAIN     → COIN CHANGE COUNT WAYS
//   Station  5  HALLWAY CARPET    → LCS (Longest Common Subsequence)
//   Station  6  SEAT ROW A        → LIS (O(n log n))
//   Station  7  MOVIE SCREEN      → EDIT DISTANCE
//   Station  8  PROJECTOR ROOM    → GRID PATHS
//   Station  9  FIRE EXIT         → LONGEST PATH IN DAG
//   Station 10  BATHROOM TILES    → INTERVAL DP (Merge Stones)
//   Station 11  COAT CHECK        → SUBSET SUM / PARTITION EQUAL
//   Station 12  VALET STATION     → WEIGHTED JOB SCHEDULING
//   Station 13  SECURITY BOOTH    → BITMASK DP / TSP
//   Station 14  MANAGER'S OFFICE  → TREE DP (Max Independent Set)
//   Station 15  MARQUEE SIGN      → DIGIT DP
//
// VIVID SCENES:
//   1  A FROG hops across PARKING LOT stripes — 1 or 2 stripes at a time.
//      How many ways to cross n stripes? dp[n] = dp[n-1] + dp[n-2].
//   2  TICKET BOOTH: for each ticket (item), TAKE IT or LEAVE IT.
//      Suitcase capacity = weight. Inner loop HIGH→LOW prevents reuse.
//   3  CONCESSION STAND vending machine RESTOCKS the coin after each use.
//      Can reuse — inner loop LOW→HIGH.
//   4  SODA FOUNTAIN: 4 syrup flavors. Count combos that fill exactly 12oz.
//      Outer=flavors, inner=volumes → combinations (not permutations).
//   5  HALLWAY has TWO carpet runners side by side. Step DIAGONALLY when colors match.
//   6  SEAT ROW A: seat numbers must only INCREASE left to right.
//      Patience sort: binary-search "tail" array for each new seat.
//   7  MOVIE SCREEN shows TYPOS. Each fix (replace/insert/delete) costs 1.
//      Fill a cost grid top-left to bottom-right.
//   8  PROJECTOR ROOM floor is a GRID — only RIGHT or DOWN. Blocked tiles = 0 paths.
//   9  FIRE EXIT corridors form a one-way DAG. DFS + memo finds the LONGEST escape route.
//  10  JANITOR SNAPS tiles in half. Each snap costs the combined length of the pieces.
//      Fill by gap length so shorter subproblems are solved first.
//  11  COAT CHECK: can a subset of coat weights sum to exactly HALF the total?
//      dp[j] = can we achieve sum j? Inner loop HIGH→LOW.
//  12  VALET picks the most PROFITABLE set of non-overlapping car arrivals.
//      Sort by end time; binary search for the last non-conflicting job.
//  13  SECURITY GUARD tours EVERY parking spot exactly once.
//      State = (current spot, bitmask of visited). O(2^n * n^2).
//  14  MANAGER's org chart on the wall. Never pick a manager AND direct report both.
//      dp[v][0] = best if excluded; dp[v][1] = best if included.
//  15  MARQUEE SIGN: count digit by digit. Track tight bound and running total.
//
// ═════════════════════════════════════════════════════════════════════════
// LINKING CHAIN
// ═════════════════════════════════════════════════════════════════════════
//
//  A FROG hops to the TICKET BOOTH (1→2) → cashier flips a MAGIC COIN
//  that restocks itself at the CONCESSION STAND (2→3) → coin drops in the
//  SODA FOUNTAIN and counts flavor combos (3→4) → two flavor runners match
//  patterns on the HALLWAY CARPET (4→5) → carpet seats are numbered in
//  increasing order on ROW A (5→6) → misspelled seat names show on the
//  MOVIE SCREEN (6→7) → projector casts a GRID on the floor (7→8) → grid
//  hallways lead to the FIRE EXIT DAG (8→9) → exit corridors are tiled by
//  the JANITOR in the BATHROOM (9→10) → janitor hangs coats by weight at
//  the COAT CHECK (10→11) → valet collects coats and parks non-overlapping
//  cars (11→12) → valet reports to the SECURITY GUARD who tours every spot
//  (12→13) → guard clocks in at the MANAGER'S office with the org chart
//  (13→14) → manager's name is spelled out digit by digit on the MARQUEE
//  (14→15).
//
// ═════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────
// 1. Fibonacci / Climbing Stairs  [1D DP, O(1) space]
//    LOCI : PARKING LOT — frog hops 1 or 2 stripes; total = dp[n-1] + dp[n-2]
//    LINK : frog bounces to the TICKET BOOTH (0/1 knapsack: take or leave the ticket)
//    MENTAL MODEL: "only last two values matter → rolling variables, O(1) space"
// ─────────────────────────────────────────────────────────────────────────
int climbStairs(int n) {
    if (n <= 1) return 1;
    int a = 1, b = 1;
    for (int i = 2; i <= n; i++) { int c = a + b; a = b; b = c; }
    return b;
}

// Frog jump: min cost to reach end, can jump 1..k steps
int frogJump(vector<int>& h, int k) {
    int n = h.size();
    vector<int> dp(n, INT_MAX);
    dp[0] = 0;
    for (int i = 1; i < n; i++)
        for (int j = 1; j <= k && i - j >= 0; j++)
            dp[i] = min(dp[i], dp[i-j] + abs(h[i] - h[i-j]));
    return dp[n-1];
}


// ─────────────────────────────────────────────────────────────────────────
// 2. 0/1 Knapsack  [2D → 1D rolling array]
//    LOCI : TICKET BOOTH — TAKE the ticket or LEAVE it; inner loop HIGH→LOW (no reuse)
//    LINK : booth has a MAGIC VENDING MACHINE that restocks coins (unbounded next)
//    MENTAL MODEL: "outer=items, inner=weight HIGH→LOW to prevent using same item twice"
// ─────────────────────────────────────────────────────────────────────────
int knapsack01(int W, vector<int>& wt, vector<int>& val) {
    vector<int> dp(W + 1, 0);
    for (int i = 0; i < (int)wt.size(); i++)
        for (int j = W; j >= wt[i]; j--)        // HIGH→LOW = 0/1 (each item once)
            dp[j] = max(dp[j], dp[j - wt[i]] + val[i]);
    return dp[W];
}


// ─────────────────────────────────────────────────────────────────────────
// 3. Unbounded Knapsack / Coin Change — Minimum Coins  [1D, inner LOW→HIGH]
//    LOCI : CONCESSION STAND — machine RESTOCKS every coin; inner loop LOW→HIGH
//    LINK : minimum coins in hand → SODA FOUNTAIN counts HOW MANY WAYS to make the amount
//    MENTAL MODEL: "outer=items, inner=weight LOW→HIGH allows reusing same item"
// ─────────────────────────────────────────────────────────────────────────
int coinChangeMin(vector<int>& coins, int amount) {
    vector<int> dp(amount + 1, INT_MAX);
    dp[0] = 0;
    for (int i = 1; i <= amount; i++)
        for (int c : coins)
            if (c <= i && dp[i - c] != INT_MAX)
                dp[i] = min(dp[i], dp[i - c] + 1);
    return dp[amount] == INT_MAX ? -1 : dp[amount];
}

int unboundedKnapsack(int W, vector<int>& wt, vector<int>& val) {
    vector<int> dp(W + 1, 0);
    for (int j = 1; j <= W; j++)                // LOW→HIGH = unbounded (reuse allowed)
        for (int i = 0; i < (int)wt.size(); i++)
            if (wt[i] <= j)
                dp[j] = max(dp[j], dp[j - wt[i]] + val[i]);
    return dp[W];
}


// ─────────────────────────────────────────────────────────────────────────
// 4. Coin Change — Count Ways (Combinations)  [outer=coins, inner=amounts]
//    LOCI : SODA FOUNTAIN — outer=syrup flavor, inner=cup volume; counts combos not perms
//    LINK : two flavor runners side by side on the HALLWAY CARPET (LCS next)
//    MENTAL MODEL: "outer=coins → each coin processed once → no duplicate combinations"
//    Swap loops (outer=amounts) → count permutations (order matters).
// ─────────────────────────────────────────────────────────────────────────
long long coinChangeWays(vector<int>& coins, int amount) {
    vector<long long> dp(amount + 1, 0);
    dp[0] = 1;
    for (int c : coins)                          // outer=coins → combinations
        for (int j = c; j <= amount; j++)
            dp[j] += dp[j - c];
    return dp[amount];
}

long long coinChangePerms(vector<int>& coins, int amount) {
    vector<long long> dp(amount + 1, 0);
    dp[0] = 1;
    for (int j = 1; j <= amount; j++)            // outer=amounts → permutations
        for (int c : coins)
            if (c <= j) dp[j] += dp[j - c];
    return dp[amount];
}


// ─────────────────────────────────────────────────────────────────────────
// 5. LCS — Longest Common Subsequence  [2D DP, O(m*n)]
//    LOCI : HALLWAY CARPET — two runners; step DIAGONALLY when tile colors match
//    LINK : both runners merge into one growing chain (LIS next)
//    MENTAL MODEL: "match → dp[i-1][j-1]+1; else → max(skip one string from each side)"
// ─────────────────────────────────────────────────────────────────────────
int lcs(const string& s, const string& t) {
    int m = s.size(), n = t.size();
    vector<vector<int>> dp(m+1, vector<int>(n+1, 0));
    for (int i = 1; i <= m; i++)
        for (int j = 1; j <= n; j++)
            dp[i][j] = (s[i-1] == t[j-1]) ? dp[i-1][j-1] + 1
                                            : max(dp[i-1][j], dp[i][j-1]);
    // reconstruct
    // int i=m,j=n; string res; while(i>0&&j>0){if(s[i-1]==t[j-1]){res+=s[i-1];i--;j--;}
    //              else if(dp[i-1][j]>dp[i][j-1])i--;else j--;}reverse(res.begin(),res.end());
    return dp[m][n];
}


// ─────────────────────────────────────────────────────────────────────────
// 6. LIS — Longest Increasing Subsequence  [O(n log n) patience sort]
//    LOCI : SEAT ROW A — seat numbers always increase; binary-search the "tails" deck
//    LINK : increasing seats lead to misspelled names on the SCREEN (edit distance next)
//    MENTAL MODEL: "tails[i] = smallest ending value for LIS of length i+1"
//    lower_bound(x): replace first tail >= x; extend tails if x is new max.
//    O(n^2) version: dp[i] = max(dp[j]+1) for all j < i with a[j] < a[i]
// ─────────────────────────────────────────────────────────────────────────
int lisNlogN(vector<int>& a) {
    vector<int> tails;
    for (int x : a) {
        auto it = lower_bound(tails.begin(), tails.end(), x);
        if (it == tails.end()) tails.push_back(x);
        else *it = x;
    }
    return tails.size();
}

int lisN2(vector<int>& a) {
    int n = a.size();
    vector<int> dp(n, 1);
    for (int i = 1; i < n; i++)
        for (int j = 0; j < i; j++)
            if (a[j] < a[i]) dp[i] = max(dp[i], dp[j] + 1);
    return *max_element(dp.begin(), dp.end());
}


// ─────────────────────────────────────────────────────────────────────────
// 7. Edit Distance — Levenshtein  [2D DP]
//    LOCI : MOVIE SCREEN — subtitle typos; each fix costs 1 (replace/insert/delete)
//    LINK : fixing the grid of subtitles → crossing the PROJECTOR ROOM floor (grid next)
//    MENTAL MODEL: "dp[i][j] = min edits to turn s[0..i-1] into t[0..j-1]"
//      s[i]==t[j]: dp[i-1][j-1]          (free — characters match)
//      replace:    dp[i-1][j-1] + 1
//      delete s[i]: dp[i-1][j] + 1
//      insert t[j]: dp[i][j-1] + 1
// ─────────────────────────────────────────────────────────────────────────
int editDistance(const string& s, const string& t) {
    int m = s.size(), n = t.size();
    vector<vector<int>> dp(m+1, vector<int>(n+1));
    for (int i = 0; i <= m; i++) dp[i][0] = i;
    for (int j = 0; j <= n; j++) dp[0][j] = j;
    for (int i = 1; i <= m; i++)
        for (int j = 1; j <= n; j++)
            dp[i][j] = (s[i-1] == t[j-1]) ? dp[i-1][j-1]
                       : 1 + min({dp[i-1][j-1], dp[i-1][j], dp[i][j-1]});
    return dp[m][n];
}


// ─────────────────────────────────────────────────────────────────────────
// 8. Grid Paths / Minimum Path Sum  [2D DP]
//    LOCI : PROJECTOR ROOM — floor grid; only move RIGHT or DOWN; obstacles = broken tiles
//    LINK : grid hallways lead to the FIRE EXIT DAG corridors (longest path next)
//    MENTAL MODEL: "dp[i][j] = dp[i-1][j] + dp[i][j-1]; blocked cells → 0"
// ─────────────────────────────────────────────────────────────────────────
long long gridPaths(vector<vector<int>>& grid) {
    int m = grid.size(), n = grid[0].size();
    vector<vector<long long>> dp(m, vector<long long>(n, 0));
    for (int i = 0; i < m && !grid[i][0]; i++) dp[i][0] = 1;
    for (int j = 0; j < n && !grid[0][j]; j++) dp[0][j] = 1;
    for (int i = 1; i < m; i++)
        for (int j = 1; j < n; j++)
            if (!grid[i][j]) dp[i][j] = dp[i-1][j] + dp[i][j-1];
    return dp[m-1][n-1];
}

int minPathSum(vector<vector<int>>& g) {
    int m = g.size(), n = g[0].size();
    vector<vector<int>> dp(m, vector<int>(n));
    dp[0][0] = g[0][0];
    for (int i = 1; i < m; i++) dp[i][0] = dp[i-1][0] + g[i][0];
    for (int j = 1; j < n; j++) dp[0][j] = dp[0][j-1] + g[0][j];
    for (int i = 1; i < m; i++)
        for (int j = 1; j < n; j++)
            dp[i][j] = g[i][j] + min(dp[i-1][j], dp[i][j-1]);
    return dp[m-1][n-1];
}


// ─────────────────────────────────────────────────────────────────────────
// 9. Longest Path in DAG  [DFS + memoization]
//    LOCI : FIRE EXIT — one-way corridors form a DAG; DFS+memo finds longest escape route
//    LINK : longest corridor ends at BATHROOM TILES the janitor must snap (interval DP next)
//    MENTAL MODEL: "DFS from each node; memo[v] = longest path starting at v"
// ─────────────────────────────────────────────────────────────────────────
int dfsLong(int v, vector<vector<int>>& adj, vector<int>& memo) {
    if (memo[v] != -1) return memo[v];
    memo[v] = 0;
    for (int u : adj[v]) memo[v] = max(memo[v], 1 + dfsLong(u, adj, memo));
    return memo[v];
}

int longestPathDAG(int n, vector<vector<int>>& adj) {
    vector<int> memo(n, -1);
    int ans = 0;
    for (int v = 0; v < n; v++) ans = max(ans, dfsLong(v, adj, memo));
    return ans;
}


// ─────────────────────────────────────────────────────────────────────────
// 10. Interval DP — Merge Stones / Matrix Chain  [O(n^3)]
//     LOCI : BATHROOM TILES — janitor snaps tiles; each snap costs combined length
//     LINK : snapped tile pieces weighed and tagged at COAT CHECK (subset sum next)
//     MENTAL MODEL: "iterate by GAP SIZE (1 to n-1); try all split points k in [l,r)"
//     Fill dp[l][r] = min cost to reduce [l,r] to one pile.
//     Use prefix sums for range sum in O(1).
// ─────────────────────────────────────────────────────────────────────────
int mergeStones(vector<int>& stones) {
    int n = stones.size();
    vector<int> pre(n+1, 0);
    for (int i = 0; i < n; i++) pre[i+1] = pre[i] + stones[i];
    vector<vector<int>> dp(n, vector<int>(n, 0));
    for (int gap = 1; gap < n; gap++)
        for (int l = 0; l + gap < n; l++) {
            int r = l + gap;
            dp[l][r] = INT_MAX;
            for (int k = l; k < r; k++)
                dp[l][r] = min(dp[l][r], dp[l][k] + dp[k+1][r] + pre[r+1] - pre[l]);
        }
    return dp[0][n-1];
}

// Burst balloons: dp[l][r] = max coins bursting all balloons in open interval (l,r)
int maxCoins(vector<int>& nums) {
    int n = nums.size();
    vector<int> a(n+2, 1);
    for (int i = 0; i < n; i++) a[i+1] = nums[i];
    vector<vector<int>> dp(n+2, vector<int>(n+2, 0));
    for (int gap = 2; gap <= n+1; gap++)
        for (int l = 0; l + gap <= n+1; l++) {
            int r = l + gap;
            for (int k = l+1; k < r; k++)
                dp[l][r] = max(dp[l][r], dp[l][k] + dp[k][r] + a[l]*a[k]*a[r]);
        }
    return dp[0][n+1];
}


// ─────────────────────────────────────────────────────────────────────────
// 11. Subset Sum / Partition Equal Subset  [1D bool DP]
//     LOCI : COAT CHECK — can subset of coat weights sum to exactly HALF of total?
//     LINK : coats loaded, VALET parks non-overlapping cars (job scheduling next)
//     MENTAL MODEL: "dp[j]=can we reach sum j? Inner HIGH→LOW (0/1 — each coat once)"
//     Partition equal: if total is odd → impossible; target = total/2.
// ─────────────────────────────────────────────────────────────────────────
bool subsetSum(vector<int>& nums, int target) {
    vector<bool> dp(target + 1, false);
    dp[0] = true;
    for (int x : nums)
        for (int j = target; j >= x; j--)
            dp[j] = dp[j] || dp[j - x];
    return dp[target];
}

bool partitionEqual(vector<int>& nums) {
    int sum = accumulate(nums.begin(), nums.end(), 0);
    if (sum % 2) return false;
    return subsetSum(nums, sum / 2);
}

int countSubsets(vector<int>& nums, int target) {
    vector<int> dp(target + 1, 0);
    dp[0] = 1;
    for (int x : nums)
        for (int j = target; j >= x; j--)
            dp[j] += dp[j - x];
    return dp[target];
}


// ─────────────────────────────────────────────────────────────────────────
// 12. Weighted Job Scheduling  [O(n log n) DP + binary search]
//     LOCI : VALET STATION — pick most profitable set of non-overlapping car arrivals
//     LINK : valet covers the lot → SECURITY GUARD must visit every spot (bitmask next)
//     MENTAL MODEL: "sort by end time; dp[i]=max profit using jobs[1..i]"
//       Skip job i: dp[i-1]
//       Take job i: dp[last_non_conflict] + profit[i]
// ─────────────────────────────────────────────────────────────────────────
struct Job { int start, end, profit; };

int weightedJobSched(vector<Job>& jobs) {
    sort(jobs.begin(), jobs.end(), [](auto& a, auto& b){ return a.end < b.end; });
    int n = jobs.size();
    vector<int> dp(n + 1, 0);
    for (int i = 1; i <= n; i++) {
        // binary search: last job ending <= start of job i
        int lo = 0, hi = i - 1;
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (jobs[mid-1].end <= jobs[i-1].start) lo = mid; else hi = mid - 1;
        }
        dp[i] = max(dp[i-1], dp[lo] + jobs[i-1].profit);
    }
    return dp[n];
}


// ─────────────────────────────────────────────────────────────────────────
// 13. Bitmask DP — TSP / Hamiltonian Path  [O(2^n * n^2)]
//     LOCI : SECURITY BOOTH — guard tours EVERY parking spot exactly once (TSP)
//     LINK : guard files full report with the MANAGER (tree DP next)
//     MENTAL MODEL: "dp[mask][v] = min cost to have visited exactly 'mask', ending at v"
//     Transition: dp[mask|(1<<u)][u] = min(..., dp[mask][v] + dist[v][u])
// ─────────────────────────────────────────────────────────────────────────
int tsp(vector<vector<int>>& dist) {
    int n = dist.size();
    int FULL = (1 << n) - 1;
    const int INF = INT_MAX / 2;
    vector<vector<int>> dp(1 << n, vector<int>(n, INF));
    dp[1][0] = 0;
    for (int mask = 1; mask <= FULL; mask++)
        for (int v = 0; v < n; v++) {
            if (!(mask & (1 << v)) || dp[mask][v] == INF) continue;
            for (int u = 0; u < n; u++) {
                if (mask & (1 << u)) continue;
                int nmask = mask | (1 << u);
                dp[nmask][u] = min(dp[nmask][u], dp[mask][v] + dist[v][u]);
            }
        }
    int ans = INF;
    for (int v = 1; v < n; v++)
        if (dp[FULL][v] != INF) ans = min(ans, dp[FULL][v] + dist[v][0]);
    return ans;
}

// Count Hamiltonian paths (matching / assignment problems)
long long hamiltonianPaths(int n, vector<vector<int>>& adj) {
    vector<vector<long long>> dp(1 << n, vector<long long>(n, 0));
    for (int v = 0; v < n; v++) dp[1 << v][v] = 1;
    for (int mask = 1; mask < (1 << n); mask++)
        for (int v = 0; v < n; v++) {
            if (!(mask & (1 << v)) || !dp[mask][v]) continue;
            for (int u = 0; u < n; u++)
                if (!(mask & (1 << u)) && adj[v][u])
                    dp[mask | (1 << u)][u] += dp[mask][v];
        }
    long long ans = 0;
    for (int v = 0; v < n; v++) ans += dp[(1<<n)-1][v];
    return ans;
}


// ─────────────────────────────────────────────────────────────────────────
// 14. Tree DP — Max Independent Set  [DFS post-order, O(n)]
//     LOCI : MANAGER'S OFFICE — org chart; can't pick boss AND direct report both
//     LINK : manager marks special nodes on the MARQUEE sign (digit DP next)
//     MENTAL MODEL: "dp[v][0]=max if v excluded; dp[v][1]=max if v included"
//       v excluded: each child can be either → dp[child][0] or dp[child][1]
//       v included: each child must be excluded → dp[child][0]
// ─────────────────────────────────────────────────────────────────────────
pair<int,int> treeDFS(int v, int par, vector<vector<int>>& adj, vector<int>& val) {
    pair<int,int> res = {0, val[v]};   // {excluded, included}
    for (int u : adj[v]) {
        if (u == par) continue;
        auto sub = treeDFS(u, v, adj, val);
        res.first  += max(sub.first, sub.second);  // child can be either when v is excluded
        res.second += sub.first;                   // child must be excluded when v is included
    }
    return res;
}

int maxIndepSet(int n, vector<pair<int,int>>& edges, vector<int>& val) {
    vector<vector<int>> adj(n);
    for (auto& e : edges) { adj[e.first].push_back(e.second); adj[e.second].push_back(e.first); }
    auto root = treeDFS(0, -1, adj, val);
    return max(root.first, root.second);
}


// ─────────────────────────────────────────────────────────────────────────
// 15. Digit DP — Count integers in [0,N] with digit sum == k
//     LOCI : MARQUEE SIGN — count digit by digit; track tight bound and running total
//     LINK : (end of theater — you've walked all 15 stations!)
//     MENTAL MODEL: "recurse position by position; tight=can this digit exceed N[pos]?"
//     memo only caches (pos, remaining) when NOT tight AND NOT leading zero.
//     Template adapts to: digit sum, count of specific digits, divisibility rules.
// ─────────────────────────────────────────────────────────────────────────
long long digitDFS(int pos, int rem, bool tight, bool leading,
                   const string& num, vector<vector<long long>>& memo) {
    if (pos == (int)num.size()) return (!leading && rem == 0) ? 1 : 0;
    if (!tight && !leading && memo[pos][rem] != -1) return memo[pos][rem];
    int limit = tight ? (num[pos] - '0') : 9;
    long long res = 0;
    for (int d = 0; d <= limit; d++) {
        bool newTight   = tight && (d == limit);
        bool newLeading = leading && (d == 0);
        int  newRem     = newLeading ? rem : rem - d;
        if (newRem >= 0)
            res += digitDFS(pos+1, newRem, newTight, newLeading, num, memo);
    }
    if (!tight && !leading) memo[pos][rem] = res;
    return res;
}

long long countDigitSum(long long N, int k) {
    string num = to_string(N);
    vector<vector<long long>> memo(num.size(), vector<long long>(k+1, -1));
    return digitDFS(0, k, true, true, num, memo);
}


// ─────────────────────────────────────────────────────────────────────────
// Quick Reference Table
// ─────────────────────────────────────────────────────────────────────────
//
//  Pattern             | Inner loop    | Reuse | Key recurrence
//  ────────────────────┼───────────────┼───────┼──────────────────────────
//  0/1 Knapsack        | HIGH → LOW    | No    | dp[j] = max(dp[j], dp[j-w]+v)
//  Unbounded Knapsack  | LOW → HIGH    | Yes   | dp[j] = max(dp[j], dp[j-w]+v)
//  Coin Change Min     | LOW → HIGH    | Yes   | dp[j] = min(dp[j], dp[j-c]+1)
//  Coin Change Ways    | outer=coins   | Yes   | dp[j] += dp[j-c]   (combinations)
//  Coin Permutations   | outer=amounts | Yes   | dp[j] += dp[j-c]   (permutations)
//  Subset Sum          | HIGH → LOW    | No    | dp[j] |= dp[j-x]
//  LCS                 | 2D            | -     | match:+1diag else max(up,left)
//  LIS (fast)          | binary search | -     | replace tail for O(n log n)
//  Edit Distance       | 2D            | -     | min(replace, del, ins)
//  Grid Paths          | 2D            | -     | dp[i][j]=dp[i-1][j]+dp[i][j-1]
//  Interval DP         | gap=1..n-1    | -     | try all split points k in [l,r)
//  Bitmask DP (TSP)    | all masks     | -     | dp[mask|(1<<u)][u]=min(...)
//  Tree DP             | DFS post-order| -     | dp[v][0/1], multiply over children
//  Digit DP            | pos by pos    | -     | tight/leading flags, memoize free states
//
// ─────────────────────────────────────────────────────────────────────────

int main() {
    cout << "=== Fibonacci / Climbing Stairs ===" << endl;
    cout << climbStairs(5) << endl;  // 8
    vector<int> h{10,30,40,20};
    cout << "Frog jump: " << frogJump(h, 2) << endl;  // 30

    cout << "\n=== 0/1 Knapsack ===" << endl;
    vector<int> wt{1,3,4,5}, val{1,4,5,7};
    cout << knapsack01(7, wt, val) << endl;  // 9

    cout << "\n=== Coin Change Min ===" << endl;
    vector<int> coins{1,5,6,9};
    cout << coinChangeMin(coins, 11) << endl;  // 2 (5+6)

    cout << "\n=== Coin Change Ways ===" << endl;
    vector<int> c2{1,2,5};
    cout << "Combos for 5: " << coinChangeWays(c2, 5) << endl;   // 4
    cout << "Perms for 3: "  << coinChangePerms(c2, 3) << endl;  // 4

    cout << "\n=== LCS ===" << endl;
    cout << lcs("ABCBDAB", "BDCAB") << endl;  // 4

    cout << "\n=== LIS ===" << endl;
    vector<int> a{3,1,4,1,5,9,2,6};
    cout << "O(n log n): " << lisNlogN(a) << endl;  // 4
    cout << "O(n^2):     " << lisN2(a)    << endl;  // 4

    cout << "\n=== Edit Distance ===" << endl;
    cout << editDistance("horse", "ros") << endl;  // 3

    cout << "\n=== Grid Paths ===" << endl;
    vector<vector<int>> grid(3, vector<int>(3, 0));
    grid[1][1] = 1;
    cout << gridPaths(grid) << endl;  // 2

    cout << "\n=== Merge Stones ===" << endl;
    vector<int> stones{6,4,7,2};
    cout << mergeStones(stones) << endl;  // 64

    cout << "\n=== Burst Balloons ===" << endl;
    vector<int> balloons{3,1,5,8};
    cout << maxCoins(balloons) << endl;  // 167

    cout << "\n=== Subset Sum / Partition ===" << endl;
    vector<int> nums{1,5,11,5};
    cout << boolalpha << partitionEqual(nums) << endl;  // true
    cout << "Count subsets summing to 11: " << countSubsets(nums, 11) << endl;  // 2

    cout << "\n=== Weighted Job Scheduling ===" << endl;
    vector<Job> jobs{{1,3,50},{2,5,10},{4,6,70},{6,7,60}};
    cout << weightedJobSched(jobs) << endl;  // 180

    cout << "\n=== TSP (4 nodes) ===" << endl;
    vector<vector<int>> dist{
        {0,10,15,20},{10,0,35,25},{15,35,0,30},{20,25,30,0}
    };
    cout << tsp(dist) << endl;  // 80

    cout << "\n=== Tree DP (Max Independent Set) ===" << endl;
    // Star: 0 is center, leaves are 1,2,3
    vector<pair<int,int>> edges{{0,1},{0,2},{0,3}};
    vector<int> v{1,1,1,1};
    cout << maxIndepSet(4, edges, v) << endl;  // 3 (pick leaves 1,2,3)

    cout << "\n=== Digit DP (count in [0,100] with digit sum=5) ===" << endl;
    cout << countDigitSum(100, 5) << endl;  // 6: {5,14,23,32,41,50}

    return 0;
}
