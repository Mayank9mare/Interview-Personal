// GoogleDSA.cpp  — Google-tagged DSA problems (C++14, g++ -std=c++14)
// Compile:  g++ -std=c++14 -O2 -o GoogleDSA GoogleDSA.cpp
//
// Sections (avoiding duplication with Algorithms.cpp / GraphAlgorithms.cpp):
//   1.  Strings
//   2.  Sliding Window
//   3.  Binary Search (advanced)
//   4.  Monotonic Stack
//   5.  Graph (non-standard variants)
//   6.  Topological Sort
//   7.  Dynamic Programming
//   8.  Intervals
//   9.  Matrix
//  10.  Math / Simulation
//  11.  Google-Specific (Codezym / onsite patterns)
//  12.  Two Pointers
//  13.  Tries
//  14.  Heap / Priority Queue
//  15.  Bit Manipulation
//  16.  Trees
//  17.  Backtracking

#include <bits/stdc++.h>
using namespace std;

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 1 — STRINGS
// ═══════════════════════════════════════════════════════════════════════════

// ── 1A. Decode String (LC 394) ────────────────────────────────────────────
// "3[a2[c]]" → "accaccacc"
// Approach: two stacks — one for repeat counts, one for prefix strings.
// When we hit ']', pop and repeat the current buffer count times, then
// append to the prefix that was saved before this '['.
// Complexity: O(output length) time and space.
string decodeString(const string& s) {
    stack<int>    countStack;
    stack<string> strStack;
    string current;
    int k = 0;
    for (char c : s) {
        if (isdigit(c)) {
            k = k * 10 + (c - '0');          // multi-digit numbers
        } else if (c == '[') {
            countStack.push(k);
            strStack.push(current);
            current.clear();
            k = 0;
        } else if (c == ']') {
            int times = countStack.top(); countStack.pop();
            string prev = strStack.top();  strStack.pop();
            for (int i = 0; i < times; i++) prev += current;
            current = prev;
        } else {
            current += c;
        }
    }
    return current;
}

// ── 1B. Expressive Words (LC 809) ─────────────────────────────────────────
// Word "aaa" is a "stretchy" version of "a" (≥3 of same char, or exact match).
// Count how many queries can be the "stretched" source of S.
// Approach: two-pointer run-length comparison on each query word.
// Key: each run in S must either (a) match the query run exactly, or
//      (b) be ≥3 and ≥ query run length.
// Complexity: O(|S| + Σ|word|) overall.
bool isStretchy(const string& S, const string& W) {
    int i = 0, j = 0;
    int n = S.size(), m = W.size();
    while (i < n && j < m) {
        if (S[i] != W[j]) return false;
        int runS = 1, runW = 1;
        while (i + runS < n && S[i + runS] == S[i]) runS++;
        while (j + runW < m && W[j + runW] == W[j]) runW++;
        // Either they match exactly, or S's run is ≥3 and covers W's run
        if (runS < runW || (runS < 3 && runS != runW)) return false;
        i += runS;
        j += runW;
    }
    return i == n && j == m;
}
int expressiveWords(const string& S, vector<string>& words) {
    int count = 0;
    for (auto& w : words) if (isStretchy(S, w)) count++;
    return count;
}

// ── 1C. Permutation Sequence (LC 60) ──────────────────────────────────────
// Return the k-th permutation of n digits (1..n) in lexicographic order.
// Approach: factorial number system — at each position, decide which digit
// to place by computing k / (remaining-1)! and removing that digit.
// Complexity: O(n^2) due to vector erase; O(n) with a Fenwick tree.
string getPermutation(int n, int k) {
    vector<int> digits;
    vector<int> fact(n + 1, 1);
    for (int i = 1; i <= n; i++) {
        digits.push_back(i);
        fact[i] = fact[i - 1] * i;
    }
    k--;  // 0-indexed
    string result;
    for (int i = n; i >= 1; i--) {
        int idx = k / fact[i - 1];
        result += to_string(digits[idx]);
        digits.erase(digits.begin() + idx);
        k %= fact[i - 1];
    }
    return result;
}

// ── 1D. Palindrome Permutations (LC 267) ──────────────────────────────────
// Generate all unique palindromes from an anagram of s.
// Approach: build the left half from frequency counts; generate all its
// permutations with next_permutation; mirror each one (with optional middle).
// Complexity: O((n/2)! × n) in the worst case.
vector<string> generatePalindromes(const string& s) {
    map<char, int> freq;
    for (char c : s) freq[c]++;
    string half, mid = "";
    int odd = 0;
    for (auto& p : freq) {
        if (p.second % 2 == 1) { odd++; mid = p.first; }
        for (int i = 0; i < p.second / 2; i++) half += p.first;
    }
    if (odd > 1) return {};          // more than one odd → impossible
    sort(half.begin(), half.end());  // for next_permutation to see all
    vector<string> res;
    do {
        string rev = half;
        reverse(rev.begin(), rev.end());
        res.push_back(half + mid + rev);
    } while (next_permutation(half.begin(), half.end()));
    return res;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 2 — SLIDING WINDOW
// ═══════════════════════════════════════════════════════════════════════════

// ── 2A. Minimum Window Substring (LC 76) ──────────────────────────────────
// Smallest window in s that contains all chars of t (with multiplicity).
// Approach: two pointers + frequency arrays.
// Maintain `have` (satisfied chars) vs `need` (total distinct chars to satisfy).
// Expand right; when window is valid, shrink from left.
// Complexity: O(|s| + |t|).
string minWindow(const string& s, const string& t) {
    if (s.empty() || t.empty()) return "";
    int freq[128] = {};
    for (char c : t) freq[(int)c]++;
    int have = 0, need = 0;
    for (int i = 0; i < 128; i++) if (freq[i] > 0) need++;
    int best = INT_MAX, bL = 0;
    int window[128] = {};
    int l = 0;
    for (int r = 0; r < (int)s.size(); r++) {
        window[(int)s[r]]++;
        if (freq[(int)s[r]] > 0 && window[(int)s[r]] == freq[(int)s[r]])
            have++;
        while (have == need) {
            if (r - l + 1 < best) { best = r - l + 1; bL = l; }
            window[(int)s[l]]--;
            if (freq[(int)s[l]] > 0 && window[(int)s[l]] < freq[(int)s[l]])
                have--;
            l++;
        }
    }
    return best == INT_MAX ? "" : s.substr(bL, best);
}

// ── 2B. Longest Substring with At Most K Distinct Characters (LC 340) ─────
// Approach: sliding window; shrink from left when distinct count > k.
// Use a frequency map — when a char's count drops to 0, it leaves the window.
// Complexity: O(n).
int lengthOfLongestSubstringKDistinct(const string& s, int k) {
    if (k == 0) return 0;
    unordered_map<char, int> freq;
    int l = 0, best = 0;
    for (int r = 0; r < (int)s.size(); r++) {
        freq[s[r]]++;
        while ((int)freq.size() > k) {
            freq[s[l]]--;
            if (freq[s[l]] == 0) freq.erase(s[l]);
            l++;
        }
        best = max(best, r - l + 1);
    }
    return best;
}

// ── 2C. Fruit Into Baskets (LC 904) ──────────────────────────────────────
// Each basket holds only one type; pick from at most 2 consecutive types.
// Equivalent to "longest subarray with at most 2 distinct values".
// Approach: sliding window + frequency map; shrink left when distinct > 2.
// Complexity: O(n).
int totalFruit(vector<int>& fruits) {
    unordered_map<int,int> freq;
    int l = 0, best = 0;
    for (int r = 0; r < (int)fruits.size(); r++) {
        freq[fruits[r]]++;
        while ((int)freq.size() > 2) {
            freq[fruits[l]]--;
            if (freq[fruits[l]] == 0) freq.erase(fruits[l]);
            l++;
        }
        best = max(best, r - l + 1);
    }
    return best;
}

// ── 2D. Max Consecutive Ones III (LC 1004) ────────────────────────────────
// Flip at most K zeros to get the longest subarray of 1s.
// Approach: sliding window tracking zero count; shrink left when zeros > K.
// Complexity: O(n).
int longestOnes(vector<int>& nums, int k) {
    int l = 0, zeros = 0, best = 0;
    for (int r = 0; r < (int)nums.size(); r++) {
        if (nums[r] == 0) zeros++;
        while (zeros > k) {
            if (nums[l] == 0) zeros--;
            l++;
        }
        best = max(best, r - l + 1);
    }
    return best;
}

// ── 2E. Sliding Window Maximum (LC 239) ───────────────────────────────────
// Return the maximum of each window of size k as it slides across nums.
// Approach: monotonic decreasing deque of indices.
// Invariant: front of deque = index of current window's max.
// Pop front when out of window; pop back when current element ≥ back
// (smaller elements can never be a future window maximum).
// Complexity: O(n).
vector<int> maxSlidingWindow(vector<int>& nums, int k) {
    deque<int> dq;  // stores indices, values are decreasing
    vector<int> res;
    for (int i = 0; i < (int)nums.size(); i++) {
        while (!dq.empty() && dq.front() < i - k + 1) dq.pop_front();
        while (!dq.empty() && nums[dq.back()] < nums[i])  dq.pop_back();
        dq.push_back(i);
        if (i >= k - 1) res.push_back(nums[dq.front()]);
    }
    return res;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 3 — BINARY SEARCH (ADVANCED)
// ═══════════════════════════════════════════════════════════════════════════

// ── 3A. First Bad Version (LC 278) ────────────────────────────────────────
// Classic binary-search-on-answer.
// Complexity: O(log n).
static int BAD_VERSION_THRESHOLD = 0;  // set before calling
bool isBadVersion(int n) { return n >= BAD_VERSION_THRESHOLD; }
int firstBadVersion(int n) {
    int lo = 1, hi = n;
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (isBadVersion(mid)) hi = mid;
        else                   lo = mid + 1;
    }
    return lo;
}

// ── 3B. Path with Minimum Effort (LC 1631) ────────────────────────────────
// Find a path from top-left to bottom-right minimising the maximum absolute
// difference between consecutive cells.
// Approach 1 (shown here): Binary search on answer + BFS reachability.
// Approach 2: Dijkstra treating effort as edge weight — same O complexity.
// Key insight: if effort E is feasible, any E' > E is also feasible →
//              monotone property → binary search.
// Complexity: O(m*n * log(maxDiff)).
int minimumEffortPath(vector<vector<int>>& heights) {
    int rows = heights.size(), cols = heights[0].size();
    int dx[] = {0,0,1,-1}, dy[] = {1,-1,0,0};

    auto canReach = [&](int limit) -> bool {
        vector<vector<bool>> visited(rows, vector<bool>(cols, false));
        queue<pair<int,int>> q;
        q.push({0, 0});
        visited[0][0] = true;
        while (!q.empty()) {
            int r = q.front().first, c = q.front().second; q.pop();
            if (r == rows-1 && c == cols-1) return true;
            for (int d = 0; d < 4; d++) {
                int nr = r + dx[d], nc = c + dy[d];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (visited[nr][nc]) continue;
                if (abs(heights[nr][nc] - heights[r][c]) <= limit) {
                    visited[nr][nc] = true;
                    q.push({nr, nc});
                }
            }
        }
        return false;
    };

    int lo = 0, hi = 1e6;
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (canReach(mid)) hi = mid;
        else               lo = mid + 1;
    }
    return lo;
}

// ── 3C. Koko Eating Bananas (LC 875) ──────────────────────────────────────
// Find the minimum eating speed k such that all piles can be finished in h hours.
// Binary search on answer: if speed s works, any s' > s also works (monotone).
// Hours at speed s: Σ ceil(pile / s).
// Complexity: O(n log maxPile).
int minEatingSpeed(vector<int>& piles, int h) {
    int lo = 1, hi = *max_element(piles.begin(), piles.end());
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        long long hours = 0;
        for (int p : piles) hours += (p + mid - 1) / mid;
        if (hours <= h) hi = mid;
        else            lo = mid + 1;
    }
    return lo;
}

// ── 3D. Find Peak Element (LC 162) ────────────────────────────────────────
// Return any index i where nums[i] > both neighbours (treat out-of-bounds as -∞).
// Binary search: if nums[mid] < nums[mid+1], a peak lies to the right
// (the upslope must end somewhere). Otherwise it lies at or left of mid.
// Complexity: O(log n).
int findPeakElement(vector<int>& nums) {
    int lo = 0, hi = (int)nums.size() - 1;
    while (lo < hi) {
        int mid = lo + (hi - lo) / 2;
        if (nums[mid] < nums[mid + 1]) lo = mid + 1;
        else                           hi = mid;
    }
    return lo;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 4 — MONOTONIC STACK
// ═══════════════════════════════════════════════════════════════════════════

// ── 4A. Number of Visible People in a Queue (LC 1944) ─────────────────────
// heights[i] = height of person at position i (all distinct).
// Person i can see person j (j > i) if no person k between them has
// height ≥ min(heights[i], heights[j]).
// Equivalently: scan right-to-left with a decreasing monotonic stack.
// For each person i, they see each person on the stack until they hit
// someone taller than themselves (plus that taller person counts too).
// Complexity: O(n) — each index pushed/popped once.
vector<int> canSeePersonsCount(vector<int>& heights) {
    int n = heights.size();
    vector<int> ans(n, 0);
    stack<int> st;  // decreasing stack of heights
    for (int i = n - 1; i >= 0; i--) {
        while (!st.empty() && st.top() < heights[i]) {
            ans[i]++;
            st.pop();
        }
        if (!st.empty()) ans[i]++;  // the first person taller than heights[i]
        st.push(heights[i]);
    }
    return ans;
}

// ── 4B. Daily Temperatures (LC 739) ──────────────────────────────────────
// For each day, how many days until a warmer temperature?
// Approach: monotonic decreasing stack of indices.
// When current temp > stack top, pop and record the gap as the answer.
// Complexity: O(n).
vector<int> dailyTemperatures(vector<int>& temperatures) {
    int n = temperatures.size();
    vector<int> ans(n, 0);
    stack<int> st;
    for (int i = 0; i < n; i++) {
        while (!st.empty() && temperatures[st.top()] < temperatures[i]) {
            ans[st.top()] = i - st.top();
            st.pop();
        }
        st.push(i);
    }
    return ans;
}

// ── 4C. Largest Rectangle in Histogram (LC 84) ────────────────────────────
// Find the area of the largest rectangle in a histogram.
// Approach: monotonic increasing stack of indices.
// When a shorter bar is found, the top bar can no longer extend right —
// compute its area: height = top bar, width = distance to the new left boundary.
// Append a sentinel 0 to flush all remaining bars at the end.
// Complexity: O(n).
int largestRectangleArea(vector<int>& heights) {
    heights.push_back(0);  // sentinel to flush stack
    stack<int> st;
    int maxArea = 0;
    for (int i = 0; i < (int)heights.size(); i++) {
        while (!st.empty() && heights[st.top()] > heights[i]) {
            int h = heights[st.top()]; st.pop();
            int w = st.empty() ? i : i - st.top() - 1;
            maxArea = max(maxArea, h * w);
        }
        st.push(i);
    }
    heights.pop_back();  // restore original
    return maxArea;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 5 — GRAPH (NON-STANDARD VARIANTS)
// ═══════════════════════════════════════════════════════════════════════════

// ── 5A. Find if Path Exists in Graph (LC 1971) ────────────────────────────
// Simple Union-Find reachability check.
// Complexity: O((V+E) α(V)) — nearly linear.
struct UF {
    vector<int> parent, rank_;
    UF(int n) : parent(n), rank_(n, 0) { iota(parent.begin(), parent.end(), 0); }
    int find(int x) { return parent[x] == x ? x : parent[x] = find(parent[x]); }
    void unite(int a, int b) {
        a = find(a); b = find(b);
        if (a == b) return;
        if (rank_[a] < rank_[b]) swap(a, b);
        parent[b] = a;
        if (rank_[a] == rank_[b]) rank_[a]++;
    }
    bool connected(int a, int b) { return find(a) == find(b); }
};
bool validPath(int n, vector<vector<int>>& edges, int src, int dst) {
    UF uf(n);
    for (auto& e : edges) uf.unite(e[0], e[1]);
    return uf.connected(src, dst);
}

// ── 5B. Count Distinct Islands (LC 694) ───────────────────────────────────
// Count islands that are distinct up to translation (not rotation/reflection).
// Approach: DFS from each unvisited land cell; record the path as a sequence
// of relative directions from the starting cell. Use a set of these sequences.
// Key: always record the "backtrack" step too, so the shape is unambiguous
// even if two different shapes produce the same forward steps.
// Complexity: O(m * n).
int numDistinctIslands(vector<vector<int>>& grid) {
    int m = grid.size(), n = grid[0].size();
    set<string> shapes;
    string path;
    // dirs: R L D U B(backtrack)
    function<void(int,int,char)> dfs = [&](int r, int c, char dir) {
        if (r < 0 || r >= m || c < 0 || c >= n || grid[r][c] != 1) return;
        grid[r][c] = 0;
        path += dir;
        dfs(r, c+1, 'R');
        dfs(r, c-1, 'L');
        dfs(r+1, c, 'D');
        dfs(r-1, c, 'U');
        path += 'B';  // backtrack marker distinguishes shapes
    };
    for (int r = 0; r < m; r++) {
        for (int c = 0; c < n; c++) {
            if (grid[r][c] == 1) {
                path.clear();
                dfs(r, c, 'S');
                shapes.insert(path);
            }
        }
    }
    return shapes.size();
}

// ── 5C. Sum of Distances in Tree (LC 834) ─────────────────────────────────
// ans[i] = sum of distances from node i to all other nodes in an unweighted tree.
// Approach (rerooting / two-pass DFS):
//   Pass 1: DFS from root 0.
//     sub[v]  = subtree size of v
//     ans[0]  = sum of all node depths (accumulates during DFS)
//   Pass 2: DFS from root 0 again.
//     When we move from parent p to child c:
//       ans[c] = ans[p] - sub[c] + (n - sub[c])
//     Reason: the sub[c] nodes in c's subtree all get 1 closer (+0 net on those),
//             and the (n-sub[c]) nodes outside get 1 farther.
// Complexity: O(n).
vector<int> sumOfDistancesInTree(int n, vector<vector<int>>& edges) {
    vector<vector<int>> adj(n);
    for (auto& e : edges) {
        adj[e[0]].push_back(e[1]);
        adj[e[1]].push_back(e[0]);
    }
    vector<int> sub(n, 1), ans(n, 0);

    // Pass 1: count subtree sizes and accumulate root's answer
    function<void(int,int,int)> dfs1 = [&](int v, int par, int depth) {
        ans[0] += depth;
        for (int u : adj[v]) {
            if (u == par) continue;
            dfs1(u, v, depth + 1);
            sub[v] += sub[u];
        }
    };
    dfs1(0, -1, 0);

    // Pass 2: reroot — propagate answer from parent to child
    function<void(int,int)> dfs2 = [&](int v, int par) {
        for (int u : adj[v]) {
            if (u == par) continue;
            ans[u] = ans[v] - sub[u] + (n - sub[u]);
            dfs2(u, v);
        }
    };
    dfs2(0, -1);

    return ans;
}

// ── 5D. Number of Islands (LC 200) ────────────────────────────────────────
// Count connected components of '1' cells in a binary grid.
// Approach: DFS from each unvisited '1'; mark visited by overwriting with '0'.
// Complexity: O(m * n).
int numIslands(vector<vector<char>>& grid) {
    int m = grid.size(), n = grid[0].size(), count = 0;
    function<void(int,int)> dfs = [&](int r, int c) {
        if (r < 0 || r >= m || c < 0 || c >= n || grid[r][c] != '1') return;
        grid[r][c] = '0';
        dfs(r+1,c); dfs(r-1,c); dfs(r,c+1); dfs(r,c-1);
    };
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            if (grid[r][c] == '1') { dfs(r, c); count++; }
    return count;
}

// ── 5E. Flood Fill (LC 733) ───────────────────────────────────────────────
// Repaint all cells connected to (sr, sc) with the same color to newColor.
// Approach: DFS; guard against infinite loop when oldColor == newColor.
// Complexity: O(m * n).
vector<vector<int>> floodFill(vector<vector<int>>& image, int sr, int sc, int newColor) {
    int oldColor = image[sr][sc];
    if (oldColor == newColor) return image;
    int m = image.size(), n = image[0].size();
    function<void(int,int)> dfs = [&](int r, int c) {
        if (r < 0 || r >= m || c < 0 || c >= n || image[r][c] != oldColor) return;
        image[r][c] = newColor;
        dfs(r+1,c); dfs(r-1,c); dfs(r,c+1); dfs(r,c-1);
    };
    dfs(sr, sc);
    return image;
}

// ── 5F. Word Ladder (LC 127) ──────────────────────────────────────────────
// Shortest transformation sequence from beginWord to endWord changing one
// letter at a time; each intermediate word must be in wordList.
// Approach: BFS — for each word, try all 26 substitutions at each position
// (O(26*L) per word) rather than checking the entire dictionary.
// Erase visited words from dict to avoid re-expansion.
// Complexity: O(26 * L^2 * |dict|) where L = word length.
int ladderLength(const string& beginWord, const string& endWord,
                 vector<string>& wordList) {
    unordered_set<string> dict(wordList.begin(), wordList.end());
    if (!dict.count(endWord)) return 0;
    queue<string> q;
    q.push(beginWord);
    int steps = 1;
    while (!q.empty()) {
        int sz = q.size();
        while (sz--) {
            string word = q.front(); q.pop();
            for (int i = 0; i < (int)word.size(); i++) {
                char orig = word[i];
                for (char c = 'a'; c <= 'z'; c++) {
                    word[i] = c;
                    if (word == endWord) return steps + 1;
                    if (dict.count(word)) { q.push(word); dict.erase(word); }
                }
                word[i] = orig;
            }
        }
        steps++;
    }
    return 0;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 6 — TOPOLOGICAL SORT
// ═══════════════════════════════════════════════════════════════════════════

// ── 6A. Course Schedule I (LC 207) ────────────────────────────────────────
// Can you finish all courses given prerequisites? → detect cycle in DAG.
// Approach: Kahn's BFS (in-degree queue).
// If topological order includes all nodes → no cycle → return true.
// Complexity: O(V + E).
bool canFinish(int numCourses, vector<vector<int>>& prerequisites) {
    vector<int> indegree(numCourses, 0);
    vector<vector<int>> adj(numCourses);
    for (auto& p : prerequisites) {
        adj[p[1]].push_back(p[0]);
        indegree[p[0]]++;
    }
    queue<int> q;
    for (int i = 0; i < numCourses; i++) if (indegree[i] == 0) q.push(i);
    int processed = 0;
    while (!q.empty()) {
        int v = q.front(); q.pop();
        processed++;
        for (int u : adj[v]) if (--indegree[u] == 0) q.push(u);
    }
    return processed == numCourses;
}

// ── 6B. Course Schedule II (LC 210) ───────────────────────────────────────
// Return one valid ordering (or [] if cycle).
// Same Kahn's BFS — collect nodes in the order they are dequeued.
// Complexity: O(V + E).
vector<int> findOrder(int numCourses, vector<vector<int>>& prerequisites) {
    vector<int> indegree(numCourses, 0);
    vector<vector<int>> adj(numCourses);
    for (auto& p : prerequisites) {
        adj[p[1]].push_back(p[0]);
        indegree[p[0]]++;
    }
    queue<int> q;
    for (int i = 0; i < numCourses; i++) if (indegree[i] == 0) q.push(i);
    vector<int> order;
    while (!q.empty()) {
        int v = q.front(); q.pop();
        order.push_back(v);
        for (int u : adj[v]) if (--indegree[u] == 0) q.push(u);
    }
    return (int)order.size() == numCourses ? order : vector<int>();
}

// ── 6C. Alien Dictionary (LC 269) ─────────────────────────────────────────
// Given words sorted in alien lexicographic order, recover the character ordering.
// Approach: compare adjacent words to extract directed edges (u → v means u
// comes before v), then Kahn's topological BFS. Return "" on cycle.
// Edge case: if words[i] is a prefix of words[i-1], the input is invalid.
// Complexity: O(C) where C = total characters across all words.
string alienOrder(vector<string>& words) {
    unordered_map<char, unordered_set<char>> adj;
    unordered_map<char, int> indegree;
    for (auto& w : words) for (char c : w) if (!indegree.count(c)) indegree[c] = 0;
    for (int i = 0; i + 1 < (int)words.size(); i++) {
        const string& a = words[i], &b = words[i+1];
        int len = min(a.size(), b.size());
        bool found = false;
        for (int j = 0; j < (int)len; j++) {
            if (a[j] != b[j]) {
                if (!adj[a[j]].count(b[j])) {
                    adj[a[j]].insert(b[j]);
                    indegree[b[j]]++;
                }
                found = true;
                break;
            }
        }
        if (!found && a.size() > b.size()) return "";  // invalid: prefix before shorter word
    }
    queue<char> q;
    for (auto& p : indegree) if (p.second == 0) q.push(p.first);
    string result;
    while (!q.empty()) {
        char c = q.front(); q.pop();
        result += c;
        if (adj.count(c))
            for (char nb : adj[c])
                if (--indegree[nb] == 0) q.push(nb);
    }
    return result.size() == indegree.size() ? result : "";  // "" if cycle
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 7 — DYNAMIC PROGRAMMING
// ═══════════════════════════════════════════════════════════════════════════

// ── 7A. House Robber (LC 198) ─────────────────────────────────────────────
// Max sum of non-adjacent elements.
// Approach: dp[i] = max(dp[i-1], dp[i-2] + nums[i]). Optimise to O(1) space.
// Complexity: O(n) time, O(1) space.
int rob(vector<int>& nums) {
    int prev2 = 0, prev1 = 0;
    for (int x : nums) {
        int cur = max(prev1, prev2 + x);
        prev2 = prev1;
        prev1 = cur;
    }
    return prev1;
}

// ── 7B. Partition Equal Subset Sum (LC 416) ───────────────────────────────
// Can we split array into two subsets with equal sum?
// Reduce to: can we pick a subset summing to total/2?
// Approach: 0-1 knapsack DP.
// Complexity: O(n * target).
bool canPartition(vector<int>& nums) {
    int total = 0;
    for (int x : nums) total += x;
    if (total % 2) return false;
    int target = total / 2;
    vector<bool> dp(target + 1, false);
    dp[0] = true;
    for (int x : nums)
        for (int j = target; j >= x; j--)
            dp[j] = dp[j] || dp[j - x];
    return dp[target];
}

// ── 7C. Decode Ways (LC 91) ───────────────────────────────────────────────
// Count number of ways to decode a digit string as A=1..Z=26.
// Approach: DP with two variables — "single digit" and "two digit" steps.
// dp[i] = # ways to decode s[0..i-1].
// dp[i] += dp[i-1]  if s[i-1] is '1'..'9'
// dp[i] += dp[i-2]  if s[i-2..i-1] forms 10..26
// Complexity: O(n) time, O(1) space.
int numDecodings(const string& s) {
    if (s.empty() || s[0] == '0') return 0;
    int n = s.size();
    int dp2 = 1;  // dp[i-2]
    int dp1 = 1;  // dp[i-1]
    for (int i = 2; i <= n; i++) {
        int cur = 0;
        if (s[i-1] != '0')
            cur += dp1;
        int two = (s[i-2] - '0') * 10 + (s[i-1] - '0');
        if (two >= 10 && two <= 26)
            cur += dp2;
        dp2 = dp1;
        dp1 = cur;
    }
    return dp1;
}

// ── 7D. Longest Duplicate Substring (LC 1044) ─────────────────────────────
// Find the longest substring that appears at least twice.
// Approach: Binary search on length L + Rabin-Karp rolling hash.
// For a given L, hash every window of size L; if any hash repeats →
// verify the actual string (to handle hash collisions) → feasible.
// Complexity: O(n log n) average.
string longestDupSubstring(const string& s) {
    int n = s.size();
    auto check = [&](int L) -> string {
        const long long MOD = 1e9 + 7, BASE = 31;
        long long h = 0, power = 1;
        for (int i = 0; i < L; i++) {
            h = (h * BASE + (s[i] - 'a' + 1)) % MOD;
            if (i > 0) power = power * BASE % MOD;
        }
        unordered_map<long long, vector<int>> seen;
        seen[h].push_back(0);
        for (int i = 1; i + L <= n; i++) {
            h = (h - (s[i-1] - 'a' + 1) * power % MOD + MOD) % MOD;
            h = (h * BASE + (s[i + L - 1] - 'a' + 1)) % MOD;
            if (seen.count(h)) {
                for (int j : seen[h])
                    if (s.substr(i, L) == s.substr(j, L))
                        return s.substr(i, L);
            }
            seen[h].push_back(i);
        }
        return "";
    };
    int lo = 1, hi = n - 1;
    string best = "";
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        string res = check(mid);
        if (!res.empty()) { best = res; lo = mid + 1; }
        else               hi = mid - 1;
    }
    return best;
}

// ── 7E. Coin Change (LC 322) ──────────────────────────────────────────────
// Minimum number of coins to reach amount. Classic unbounded knapsack.
// dp[0] = 0; dp[i] = min(dp[i], dp[i-coin]+1) for each coin ≤ i.
// Complexity: O(amount * |coins|).
int coinChange(vector<int>& coins, int amount) {
    vector<int> dp(amount + 1, amount + 1);
    dp[0] = 0;
    for (int i = 1; i <= amount; i++)
        for (int c : coins)
            if (c <= i) dp[i] = min(dp[i], dp[i - c] + 1);
    return dp[amount] > amount ? -1 : dp[amount];
}

// ── 7F. Maximum Product Subarray (LC 152) ─────────────────────────────────
// Find the contiguous subarray with the largest product.
// Key: a negative × negative = positive, so track both the running maximum
// AND running minimum at each position; swap them when multiplying by a negative.
// Complexity: O(n) time, O(1) space.
int maxProduct(vector<int>& nums) {
    int maxP = nums[0], minP = nums[0], res = nums[0];
    for (int i = 1; i < (int)nums.size(); i++) {
        if (nums[i] < 0) swap(maxP, minP);
        maxP = max(nums[i], maxP * nums[i]);
        minP = min(nums[i], minP * nums[i]);
        res  = max(res, maxP);
    }
    return res;
}

// ── 7G. Longest Common Subsequence (LC 1143) ──────────────────────────────
// Classic 2D DP.
// If text1[i-1] == text2[j-1]: dp[i][j] = dp[i-1][j-1] + 1
// Else:                        dp[i][j] = max(dp[i-1][j], dp[i][j-1])
// Complexity: O(m*n) time and space.
int longestCommonSubsequence(const string& text1, const string& text2) {
    int m = text1.size(), n = text2.size();
    vector<vector<int>> dp(m + 1, vector<int>(n + 1, 0));
    for (int i = 1; i <= m; i++)
        for (int j = 1; j <= n; j++)
            dp[i][j] = (text1[i-1] == text2[j-1])
                       ? dp[i-1][j-1] + 1
                       : max(dp[i-1][j], dp[i][j-1]);
    return dp[m][n];
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 8 — INTERVALS
// ═══════════════════════════════════════════════════════════════════════════

// ── 8A. Meeting Rooms III (LC 2402) ───────────────────────────────────────
// n rooms; assign each meeting (start,end) to the room with smallest index
// that is free. If no room is free, delay in the room that becomes free
// earliest (smallest index to break ties).
// Return the room with the most meetings.
// Approach:
//   - `free`: min-heap of room indices (by index) currently available.
//   - `busy`: min-heap of (endTime, roomIndex) for ongoing meetings.
//   Sort meetings by start time. For each meeting:
//     1. Move rooms that finish before current start from busy → free.
//     2. If `free` has a room, use the smallest index.
//     3. Otherwise, pop the earliest-finishing room from `busy`, delay the
//        meeting to start when that room frees.
// Complexity: O(M log n) where M = #meetings.
int mostBooked(int n, vector<vector<int>>& meetings) {
    sort(meetings.begin(), meetings.end());
    // min-heap of room indices
    priority_queue<int, vector<int>, greater<int>> freeRooms;
    for (int i = 0; i < n; i++) freeRooms.push(i);
    // min-heap of (endTime, roomIdx)
    priority_queue<pair<long long,int>,
                   vector<pair<long long,int>>,
                   greater<pair<long long,int>>> busyRooms;
    vector<int> count(n, 0);
    for (auto& m : meetings) {
        long long start = m[0], end = m[1];
        // Release rooms that finished before this meeting starts
        while (!busyRooms.empty() && busyRooms.top().first <= start) {
            freeRooms.push(busyRooms.top().second);
            busyRooms.pop();
        }
        int room;
        if (!freeRooms.empty()) {
            room = freeRooms.top(); freeRooms.pop();
            busyRooms.push({end, room});
        } else {
            auto top = busyRooms.top(); busyRooms.pop();
            long long freeAt = top.first;
            room = top.second;
            busyRooms.push({freeAt + (end - start), room});
        }
        count[room]++;
    }
    return max_element(count.begin(), count.end()) - count.begin();
}

// ── 8B. Employee Free Time (LC 759) ───────────────────────────────────────
// Given sorted intervals per employee, find time slots when ALL are free.
// Approach: flatten all intervals, sort, then find gaps between merged intervals.
// (Same as "merge intervals" then complement.)
// Complexity: O(N log N) where N = total intervals.
// Returns gaps as pairs [start, end].
vector<pair<int,int>> employeeFreeTime(vector<vector<pair<int,int>>>& schedule) {
    vector<pair<int,int>> all;
    for (auto& emp : schedule)
        for (auto& iv : emp)
            all.push_back(iv);
    sort(all.begin(), all.end());
    vector<pair<int,int>> merged;
    for (auto& iv : all) {
        if (merged.empty() || merged.back().second < iv.first)
            merged.push_back(iv);
        else
            merged.back().second = max(merged.back().second, iv.second);
    }
    vector<pair<int,int>> gaps;
    for (int i = 1; i < (int)merged.size(); i++)
        gaps.push_back({merged[i-1].second, merged[i].first});
    return gaps;
}

// ── 8C. Special Array II (LC 3152) ────────────────────────────────────────
// Array is "special" if every adjacent pair has different parities.
// For each query [l, r]: is subarray nums[l..r] special?
// Approach: precompute `bad[i]` = 1 if nums[i] and nums[i+1] have same parity.
// prefix[i] = number of bad pairs in [0, i-1].
// Query [l,r]: count bad pairs in [l, r-1] = prefix[r] - prefix[l].
// If 0 → special.
// Complexity: O(n + q).
vector<bool> isArraySpecial(vector<int>& nums, vector<vector<int>>& queries) {
    int n = nums.size();
    vector<int> prefix(n + 1, 0);
    for (int i = 1; i < n; i++)
        prefix[i] = prefix[i-1] + ((nums[i] % 2) == (nums[i-1] % 2) ? 1 : 0);
    prefix[n] = prefix[n-1];
    vector<bool> res;
    for (auto& q : queries) {
        int l = q[0], r = q[1];
        res.push_back(prefix[r] - prefix[l] == 0);
    }
    return res;
}

// ── 8D. Merge Intervals (LC 56) ───────────────────────────────────────────
// Merge all overlapping intervals.
// Approach: sort by start time; extend the current interval's end if the
// next interval overlaps (next.start <= current.end).
// Complexity: O(n log n).
vector<vector<int>> mergeIntervals(vector<vector<int>>& intervals) {
    sort(intervals.begin(), intervals.end());
    vector<vector<int>> res;
    for (auto& iv : intervals) {
        if (res.empty() || res.back()[1] < iv[0])
            res.push_back(iv);
        else
            res.back()[1] = max(res.back()[1], iv[1]);
    }
    return res;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 9 — MATRIX
// ═══════════════════════════════════════════════════════════════════════════

// ── 9A. Longest Increasing Path in Matrix (LC 329) ────────────────────────
// Find the length of the longest strictly increasing path in a grid.
// Approach: DFS + memoization (implicit topo sort — no cycles possible since
// we only move to strictly larger cells).
// dp[r][c] = longest path starting at (r,c).
// Complexity: O(m*n) — each cell computed once.
int longestIncreasingPath(vector<vector<int>>& matrix) {
    int m = matrix.size(), n = matrix[0].size();
    vector<vector<int>> memo(m, vector<int>(n, 0));
    int dx[] = {0,0,1,-1}, dy[] = {1,-1,0,0};
    function<int(int,int)> dfs = [&](int r, int c) -> int {
        if (memo[r][c]) return memo[r][c];
        int best = 1;
        for (int d = 0; d < 4; d++) {
            int nr = r + dx[d], nc = c + dy[d];
            if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
            if (matrix[nr][nc] > matrix[r][c])
                best = max(best, 1 + dfs(nr, nc));
        }
        return memo[r][c] = best;
    };
    int ans = 0;
    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            ans = max(ans, dfs(r, c));
    return ans;
}

// ── 9B. Spiral Matrix (LC 54) ─────────────────────────────────────────────
// Traverse an m×n matrix in spiral (clockwise) order.
// Approach: shrink four boundaries (top/bottom/left/right) after each pass.
// Complexity: O(m*n).
vector<int> spiralOrder(vector<vector<int>>& matrix) {
    int top = 0, bottom = matrix.size() - 1;
    int left = 0, right = matrix[0].size() - 1;
    vector<int> res;
    while (top <= bottom && left <= right) {
        for (int c = left;   c <= right;  c++) res.push_back(matrix[top][c]);
        top++;
        for (int r = top;    r <= bottom; r++) res.push_back(matrix[r][right]);
        right--;
        if (top <= bottom)
            for (int c = right; c >= left;  c--) res.push_back(matrix[bottom][c]);
        bottom--;
        if (left <= right)
            for (int r = bottom; r >= top;  r--) res.push_back(matrix[r][left]);
        left++;
    }
    return res;
}

// ── 9C. Number of Submatrices That Sum to Target (LC 1074) ────────────────
// Count submatrices summing to target.
// Approach: fix top and bottom row; compress columns into 1D prefix sums,
// then apply the "subarray sum equals k" (LC 560) trick using a HashMap.
// Complexity: O(m^2 * n).
int numSubmatrixSumTarget(vector<vector<int>>& matrix, int target) {
    int m = matrix.size(), n = matrix[0].size(), count = 0;
    // Build row-wise prefix sums in place
    for (int r = 0; r < m; r++)
        for (int c = 1; c < n; c++)
            matrix[r][c] += matrix[r][c-1];
    for (int c1 = 0; c1 < n; c1++) {
        for (int c2 = c1; c2 < n; c2++) {
            unordered_map<int,int> prefCnt;
            prefCnt[0] = 1;
            int sum = 0;
            for (int r = 0; r < m; r++) {
                sum += matrix[r][c2] - (c1 > 0 ? matrix[r][c1-1] : 0);
                count += prefCnt.count(sum - target) ? prefCnt[sum - target] : 0;
                prefCnt[sum]++;
            }
        }
    }
    return count;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 10 — MATH / SIMULATION
// ═══════════════════════════════════════════════════════════════════════════

// ── 10A. Happy Number (LC 202) ────────────────────────────────────────────
// A number is "happy" if repeatedly replacing it with the sum of squares of
// its digits eventually reaches 1. Non-happy numbers cycle.
// Approach: Floyd's cycle detection (fast/slow pointers on the sequence).
// Complexity: O(log n) per step, O(log n) steps before a cycle appears.
int digitSquareSum(int n) {
    int s = 0;
    while (n) { int d = n % 10; s += d * d; n /= 10; }
    return s;
}
bool isHappy(int n) {
    int slow = n, fast = digitSquareSum(n);
    while (fast != 1 && slow != fast) {
        slow = digitSquareSum(slow);
        fast = digitSquareSum(digitSquareSum(fast));
    }
    return fast == 1;
}

// ── 10B. Positions of Large Groups (LC 830) ───────────────────────────────
// A "large group" is a consecutive run of ≥3 identical chars.
// Approach: linear scan tracking run start.
// Complexity: O(n).
vector<vector<int>> largeGroupPositions(const string& s) {
    vector<vector<int>> res;
    int n = s.size(), i = 0;
    while (i < n) {
        int j = i;
        while (j < n && s[j] == s[i]) j++;
        if (j - i >= 3) res.push_back({i, j - 1});
        i = j;
    }
    return res;
}

// ── 10C. Robot Bounded in Circle (LC 1041) ────────────────────────────────
// Given a movement sequence (G/L/R), does the robot stay in a bounded circle?
// Key insight: after one full cycle, if the robot is back at origin OR not
// facing north → it will loop (at most 4 repetitions needed).
// Complexity: O(n).
bool isRobotBounded(const string& instructions) {
    int x = 0, y = 0;
    int dx[] = {0, 1, 0, -1};   // N E S W x-deltas
    int dy[] = {1, 0, -1, 0};   // N E S W y-deltas
    int dir = 0;                 // 0=North, 1=East, 2=South, 3=West
    for (char c : instructions) {
        if      (c == 'G') { x += dx[dir]; y += dy[dir]; }
        else if (c == 'L')   dir = (dir + 3) % 4;
        else                 dir = (dir + 1) % 4;
    }
    return (x == 0 && y == 0) || dir != 0;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 11 — GOOGLE-SPECIFIC (Codezym / known onsite patterns)
// ═══════════════════════════════════════════════════════════════════════════

// ── 11A. Subdomain Visit Count (LC 811) ───────────────────────────────────
// "9001 discuss.leetcode.com" means discuss.leetcode.com, leetcode.com,
// and .com were each visited 9001 times.
// Approach: split each record on '.', accumulate counts for all suffixes.
// Complexity: O(Σ |s| * depth).
vector<string> subdomainVisits(vector<string>& cpdomains) {
    unordered_map<string, int> counts;
    for (auto& s : cpdomains) {
        int sp = s.find(' ');
        int n = stoi(s.substr(0, sp));
        string domain = s.substr(sp + 1);
        // Count domain itself and all parent domains
        while (true) {
            counts[domain] += n;
            auto dot = domain.find('.');
            if (dot == string::npos) break;
            domain = domain.substr(dot + 1);
        }
    }
    vector<string> res;
    for (auto& p : counts)
        res.push_back(to_string(p.second) + " " + p.first);
    return res;
}

// ── 11B. Multiply Sparse Matrices (LC 311) ────────────────────────────────
// C = A × B where A is m×k and B is k×n; many zeros → skip zero entries.
// Approach: for each non-zero A[i][l], iterate B[l][j]; add A[i][l]*B[l][j]
// to C[i][j]. This avoids multiplying zeros from A.
// Complexity: O(m*k*n) worst case but much faster on sparse inputs.
vector<vector<int>> multiply(vector<vector<int>>& A, vector<vector<int>>& B) {
    int m = A.size(), k = A[0].size(), n = B[0].size();
    vector<vector<int>> C(m, vector<int>(n, 0));
    for (int i = 0; i < m; i++)
        for (int l = 0; l < k; l++) {
            if (A[i][l] == 0) continue;
            for (int j = 0; j < n; j++)
                C[i][j] += A[i][l] * B[l][j];
        }
    return C;
}

// ── 11C. Array Range Update Queries (Difference Array) ────────────────────
// Given an array and Q queries (l, r, val): add val to all elements [l..r].
// After all queries, return the final array.
// Approach: difference array D where D[i] = A[i] - A[i-1].
//   Update(l,r,val): D[l] += val; D[r+1] -= val.
//   Reconstruct: prefix sum of D gives final A.
// Complexity: O(n + Q) — much better than O(n*Q) naïve.
vector<int> applyRangeUpdates(int n, vector<tuple<int,int,int>>& queries) {
    vector<int> diff(n + 1, 0);
    for (auto& q : queries) {
        int l = get<0>(q), r = get<1>(q), val = get<2>(q);
        diff[l]     += val;
        diff[r + 1] -= val;
    }
    vector<int> result(n);
    int running = 0;
    for (int i = 0; i < n; i++) {
        running += diff[i];
        result[i] = running;
    }
    return result;
}

// ── 11D. Detect First Timed-Out Job (Google onsite variant) ───────────────
// Given a log of (jobId, startTime, endTime) and a timeout T,
// return the jobId of the first job that took longer than T,
// ordered by startTime (then jobId to break ties).
// Approach: sort by startTime, linear scan.
// Complexity: O(n log n).
string detectFirstTimedOutJob(vector<tuple<string,int,int>>& jobs, int timeout) {
    sort(jobs.begin(), jobs.end(), [](const tuple<string,int,int>& a,
                                      const tuple<string,int,int>& b) {
        if (get<1>(a) != get<1>(b)) return get<1>(a) < get<1>(b);
        return get<0>(a) < get<0>(b);
    });
    for (auto& j : jobs) {
        string id = get<0>(j);
        int start = get<1>(j), end = get<2>(j);
        if (end - start > timeout) return id;
    }
    return "";
}

// ── 11E. Most Active Users in Chat Logs (Google onsite variant) ───────────
// Given a list of messages as (userId, timestamp), find the top-K
// most active users (by message count). Ties broken by userId ascending.
// Approach: HashMap + partial sort.
// Complexity: O(n + U log K) where U = distinct users.
vector<string> topKActiveUsers(vector<pair<string,int>>& messages, int k) {
    unordered_map<string, int> cnt;
    for (auto& m : messages) cnt[m.first]++;
    vector<pair<int,string>> users;
    users.reserve(cnt.size());
    for (auto& p : cnt) users.push_back({p.second, p.first});
    sort(users.begin(), users.end(), [](const pair<int,string>& a,
                                        const pair<int,string>& b) {
        if (a.first != b.first) return a.first > b.first;
        return a.second < b.second;
    });
    vector<string> res;
    for (int i = 0; i < k && i < (int)users.size(); i++)
        res.push_back(users[i].second);
    return res;
}

// ── 11F. Days When Everyone Is Free (Google onsite variant) ───────────────
// Each person provides their list of busy day intervals.
// Return the number of days in [1..D] when all persons are free.
// Approach: difference array to mark busy-ness; any day with 0 = free.
// Complexity: O(P * I + D) where P=people, I=intervals per person.
int daysEveryoneFree(int D, vector<vector<pair<int,int>>>& busy) {
    vector<int> diff(D + 2, 0);
    for (auto& person : busy) {
        for (auto& iv : person) {
            diff[iv.first]++;
            diff[iv.second + 1]--;
        }
    }
    int freeDays = 0, running = 0;
    for (int d = 1; d <= D; d++) {
        running += diff[d];
        if (running == 0) freeDays++;
    }
    return freeDays;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 12 — TWO POINTERS
// ═══════════════════════════════════════════════════════════════════════════

// ── 12A. 3Sum (LC 15) ────────────────────────────────────────────────────
// Find all unique triplets that sum to zero.
// Approach: sort, then for each nums[i] use two pointers on i+1..n-1.
// Skip duplicates on i, l, and r to avoid repeated results.
// Complexity: O(n^2).
vector<vector<int>> threeSum(vector<int>& nums) {
    sort(nums.begin(), nums.end());
    int n = nums.size();
    vector<vector<int>> res;
    for (int i = 0; i < n - 2; i++) {
        if (i > 0 && nums[i] == nums[i-1]) continue;
        int l = i + 1, r = n - 1;
        while (l < r) {
            int sum = nums[i] + nums[l] + nums[r];
            if (sum == 0) {
                res.push_back({nums[i], nums[l], nums[r]});
                while (l < r && nums[l] == nums[l+1]) l++;
                while (l < r && nums[r] == nums[r-1]) r--;
                l++; r--;
            } else if (sum < 0) l++;
            else                r--;
        }
    }
    return res;
}

// ── 12B. Container With Most Water (LC 11) ───────────────────────────────
// Two vertical lines; find the pair that traps the most water.
// Approach: two pointers from both ends — always advance the shorter line
// (moving the taller one can only shrink the width without gaining height).
// Complexity: O(n).
int maxArea(vector<int>& height) {
    int l = 0, r = (int)height.size() - 1, best = 0;
    while (l < r) {
        best = max(best, min(height[l], height[r]) * (r - l));
        if (height[l] < height[r]) l++;
        else                       r--;
    }
    return best;
}

// ── 12C. Trapping Rain Water (LC 42) ─────────────────────────────────────
// Compute total water trapped between bars after rainfall.
// Approach: two pointers + running max from each side.
// Water at i = min(maxLeft, maxRight) - height[i].
// Move the pointer whose max is smaller (it's the binding constraint).
// Complexity: O(n) time, O(1) space.
int trap(vector<int>& height) {
    int l = 0, r = (int)height.size() - 1;
    int maxL = 0, maxR = 0, water = 0;
    while (l < r) {
        if (height[l] <= height[r]) {
            if (height[l] >= maxL) maxL = height[l];
            else water += maxL - height[l];
            l++;
        } else {
            if (height[r] >= maxR) maxR = height[r];
            else water += maxR - height[r];
            r--;
        }
    }
    return water;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 13 — TRIES
// ═══════════════════════════════════════════════════════════════════════════

// ── 13A. Implement Trie (LC 208) ─────────────────────────────────────────
// Prefix tree with insert / search / startsWith.
// Each TrieNode holds children[26] and an isEnd flag.
// Complexity: O(L) per operation, O(ALPHABET * N * L) space total.
struct TrieNode {
    TrieNode* children[26];
    bool isEnd;
    TrieNode() : isEnd(false) { fill(children, children + 26, nullptr); }
};
struct Trie {
    TrieNode* root;
    Trie() { root = new TrieNode(); }
    void insert(const string& word) {
        TrieNode* cur = root;
        for (char c : word) {
            int i = c - 'a';
            if (!cur->children[i]) cur->children[i] = new TrieNode();
            cur = cur->children[i];
        }
        cur->isEnd = true;
    }
    bool search(const string& word) {
        TrieNode* cur = root;
        for (char c : word) {
            int i = c - 'a';
            if (!cur->children[i]) return false;
            cur = cur->children[i];
        }
        return cur->isEnd;
    }
    bool startsWith(const string& prefix) {
        TrieNode* cur = root;
        for (char c : prefix) {
            int i = c - 'a';
            if (!cur->children[i]) return false;
            cur = cur->children[i];
        }
        return true;
    }
};

// ── 13B. Add and Search Words with Wildcard (LC 211) ─────────────────────
// Trie supporting '.' wildcard that matches any single character.
// search() does DFS when it encounters '.'; otherwise standard trie walk.
// Worst case O(26^L) for all-dot queries — rarely hit in practice.
struct WordDictionary {
    TrieNode* root;
    WordDictionary() { root = new TrieNode(); }
    void addWord(const string& word) {
        TrieNode* cur = root;
        for (char c : word) {
            int i = c - 'a';
            if (!cur->children[i]) cur->children[i] = new TrieNode();
            cur = cur->children[i];
        }
        cur->isEnd = true;
    }
    bool searchFrom(TrieNode* node, const string& word, int idx) {
        if (idx == (int)word.size()) return node->isEnd;
        char c = word[idx];
        if (c == '.') {
            for (int i = 0; i < 26; i++)
                if (node->children[i] && searchFrom(node->children[i], word, idx+1))
                    return true;
            return false;
        }
        int i = c - 'a';
        return node->children[i] && searchFrom(node->children[i], word, idx+1);
    }
    bool search(const string& word) { return searchFrom(root, word, 0); }
};

// ── 13C. Word Search II (LC 212) ─────────────────────────────────────────
// Find all words from a dictionary that exist in a 2D character grid.
// Approach: build a Trie from the word list; DFS on the grid using the Trie
// to prune paths that can't lead to any word early.
// Mark visited cells with '#'; restore after DFS.
// Set isEnd=false after finding a word to deduplicate results.
// Complexity: O(m*n*4^L) bounded by Trie depth; much faster in practice.
vector<string> findWords(vector<vector<char>>& board, vector<string>& words) {
    TrieNode* root2 = new TrieNode();
    auto insertWord = [&](const string& w) {
        TrieNode* cur = root2;
        for (char c : w) {
            int i = c - 'a';
            if (!cur->children[i]) cur->children[i] = new TrieNode();
            cur = cur->children[i];
        }
        cur->isEnd = true;
    };
    for (auto& w : words) insertWord(w);

    int m = board.size(), n = board[0].size();
    vector<string> res;
    string path;

    function<void(int,int,TrieNode*)> dfs = [&](int r, int c, TrieNode* node) {
        if (r < 0 || r >= m || c < 0 || c >= n || board[r][c] == '#') return;
        char ch = board[r][c];
        TrieNode* next = node->children[ch - 'a'];
        if (!next) return;
        path += ch;
        board[r][c] = '#';
        if (next->isEnd) { res.push_back(path); next->isEnd = false; }
        dfs(r+1,c,next); dfs(r-1,c,next);
        dfs(r,c+1,next); dfs(r,c-1,next);
        board[r][c] = ch;
        path.pop_back();
    };

    for (int r = 0; r < m; r++)
        for (int c = 0; c < n; c++)
            dfs(r, c, root2);
    return res;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 14 — HEAP / PRIORITY QUEUE
// ═══════════════════════════════════════════════════════════════════════════

// ── 14A. K Closest Points to Origin (LC 973) ─────────────────────────────
// Find the k points nearest to (0,0) by Euclidean distance.
// Approach: max-heap of size k on squared distance (avoids sqrt).
// When the heap exceeds k, pop the farthest seen so far.
// Complexity: O(n log k).
vector<vector<int>> kClosest(vector<vector<int>>& points, int k) {
    priority_queue<pair<int,int>> pq;  // (dist^2, index), max-heap
    for (int i = 0; i < (int)points.size(); i++) {
        int d = points[i][0]*points[i][0] + points[i][1]*points[i][1];
        pq.push({d, i});
        if ((int)pq.size() > k) pq.pop();
    }
    vector<vector<int>> res;
    while (!pq.empty()) { res.push_back(points[pq.top().second]); pq.pop(); }
    return res;
}

// ── 14B. Find Median from Data Stream (LC 295) ────────────────────────────
// Maintain a running median as numbers are added one at a time.
// Approach: two heaps — maxHeap (lower half) and minHeap (upper half).
// Invariant: |maxHeap| == |minHeap| or |maxHeap| == |minHeap| + 1.
// Median = maxHeap.top() if sizes differ; average of tops if equal.
// Complexity: O(log n) per addNum, O(1) per findMedian.
struct MedianFinder {
    priority_queue<int> maxH;                              // lower half
    priority_queue<int,vector<int>,greater<int>> minH;    // upper half

    void addNum(int num) {
        maxH.push(num);
        minH.push(maxH.top()); maxH.pop();
        if (minH.size() > maxH.size()) { maxH.push(minH.top()); minH.pop(); }
    }
    double findMedian() {
        if (maxH.size() > minH.size()) return maxH.top();
        return (maxH.top() + minH.top()) / 2.0;
    }
};

// ── 14C. Merge K Sorted Lists (LC 23) ────────────────────────────────────
// Merge k sorted linked lists into one sorted list.
// Approach: min-heap of (value, node pointer) — always extend with the
// globally smallest current head across all lists.
// Complexity: O(N log k) where N = total nodes.
struct ListNode {
    int val;
    ListNode* next;
    ListNode(int v) : val(v), next(nullptr) {}
};
ListNode* mergeKLists(vector<ListNode*>& lists) {
    auto cmp = [](ListNode* a, ListNode* b) { return a->val > b->val; };
    priority_queue<ListNode*, vector<ListNode*>, decltype(cmp)> pq(cmp);
    for (auto* node : lists) if (node) pq.push(node);
    ListNode dummy(0);
    ListNode* tail = &dummy;
    while (!pq.empty()) {
        ListNode* node = pq.top(); pq.pop();
        tail->next = node;
        tail = tail->next;
        if (node->next) pq.push(node->next);
    }
    return dummy.next;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 15 — BIT MANIPULATION
// ═══════════════════════════════════════════════════════════════════════════

// ── 15A. Single Number (LC 136) ──────────────────────────────────────────
// Every element appears twice except one. Find the lone element.
// XOR all elements: pairs cancel (a^a=0); the single element survives.
// Complexity: O(n) time, O(1) space.
int singleNumber(vector<int>& nums) {
    int res = 0;
    for (int x : nums) res ^= x;
    return res;
}

// ── 15B. Counting Bits (LC 338) ───────────────────────────────────────────
// For each i in [0, n], count the number of set bits.
// DP: dp[i] = dp[i >> 1] + (i & 1).
// Bits in i = bits in i/2 plus the lowest bit of i.
// Complexity: O(n).
vector<int> countBits(int n) {
    vector<int> dp(n + 1, 0);
    for (int i = 1; i <= n; i++) dp[i] = dp[i >> 1] + (i & 1);
    return dp;
}

// ── 15C. Number of 1 Bits (LC 191) ───────────────────────────────────────
// Count set bits in a 32-bit unsigned integer.
// n & (n-1) clears the lowest set bit each iteration — loop runs k times
// where k is the popcount.
// Complexity: O(k).
int hammingWeight(uint32_t n) {
    int count = 0;
    while (n) { n &= (n - 1); count++; }
    return count;
}

// ── 15D. Reverse Bits (LC 190) ───────────────────────────────────────────
// Reverse the bits of a 32-bit unsigned integer.
// Shift result left and input right, ORing in the lowest bit each iteration.
// Complexity: O(32) = O(1).
uint32_t reverseBits(uint32_t n) {
    uint32_t res = 0;
    for (int i = 0; i < 32; i++) { res = (res << 1) | (n & 1); n >>= 1; }
    return res;
}

// ── 15E. Missing Number (LC 268) ─────────────────────────────────────────
// Find the missing number in [0..n] given n distinct numbers in that range.
// XOR every index AND every value — the missing index won't find a partner.
// Complexity: O(n) time, O(1) space.
int missingNumber(vector<int>& nums) {
    int n = nums.size(), res = n;
    for (int i = 0; i < n; i++) res ^= i ^ nums[i];
    return res;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 16 — TREES
// ═══════════════════════════════════════════════════════════════════════════

struct TreeNode {
    int val;
    TreeNode* left;
    TreeNode* right;
    TreeNode(int v) : val(v), left(nullptr), right(nullptr) {}
};

// ── 16A. Binary Tree Level Order Traversal (LC 102) ──────────────────────
// BFS layer by layer, returning each level as a separate list.
// Complexity: O(n).
vector<vector<int>> levelOrder(TreeNode* root) {
    vector<vector<int>> res;
    if (!root) return res;
    queue<TreeNode*> q;
    q.push(root);
    while (!q.empty()) {
        int sz = q.size();
        vector<int> level;
        while (sz--) {
            TreeNode* node = q.front(); q.pop();
            level.push_back(node->val);
            if (node->left)  q.push(node->left);
            if (node->right) q.push(node->right);
        }
        res.push_back(level);
    }
    return res;
}

// ── 16B. Binary Tree ZigZag Level Order (LC 103) ─────────────────────────
// Same as level order but alternate direction every level.
// Approach: run standard BFS, then reverse odd-indexed levels.
// Complexity: O(n).
vector<vector<int>> zigzagLevelOrder(TreeNode* root) {
    auto res = levelOrder(root);
    for (int i = 1; i < (int)res.size(); i += 2)
        reverse(res[i].begin(), res[i].end());
    return res;
}

// ── 16C. Lowest Common Ancestor (LC 236) ─────────────────────────────────
// Find the LCA of nodes p and q in a binary tree (not necessarily a BST).
// Approach: post-order DFS. A node is the LCA if:
//   (a) p and q are found in different subtrees, or
//   (b) the node itself is p (or q) and the other is in its subtree.
// Complexity: O(n).
TreeNode* lowestCommonAncestor(TreeNode* root, TreeNode* p, TreeNode* q) {
    if (!root || root == p || root == q) return root;
    TreeNode* left  = lowestCommonAncestor(root->left,  p, q);
    TreeNode* right = lowestCommonAncestor(root->right, p, q);
    if (left && right) return root;
    return left ? left : right;
}

// ── 16D. Binary Tree Maximum Path Sum (LC 124) ────────────────────────────
// A path can start and end at any node. Find the maximum sum path.
// Approach: post-order DFS tracking best one-sided gain from each subtree.
// At each node: candidate = leftGain + rightGain + node->val (full arch).
// Return only the best one-sided branch to the parent.
// Complexity: O(n).
int maxPathSumHelper(TreeNode* node, int& best) {
    if (!node) return 0;
    int l = max(0, maxPathSumHelper(node->left,  best));
    int r = max(0, maxPathSumHelper(node->right, best));
    best = max(best, l + r + node->val);
    return node->val + max(l, r);
}
int maxPathSum(TreeNode* root) {
    int best = INT_MIN;
    maxPathSumHelper(root, best);
    return best;
}

// ── 16E. Serialize and Deserialize Binary Tree (LC 297) ───────────────────
// Convert a binary tree to a string and reconstruct it exactly.
// Approach: preorder traversal; '#' marks null nodes; ',' is delimiter.
// Deserialize consumes tokens from an istringstream with a recursive helper.
// Complexity: O(n).
string serialize(TreeNode* root) {
    if (!root) return "#";
    return to_string(root->val) + "," + serialize(root->left) + "," + serialize(root->right);
}
TreeNode* deserializeHelper(istringstream& ss) {
    string token;
    if (!getline(ss, token, ',')) return nullptr;
    if (token == "#") return nullptr;
    TreeNode* node = new TreeNode(stoi(token));
    node->left  = deserializeHelper(ss);
    node->right = deserializeHelper(ss);
    return node;
}
TreeNode* deserialize(const string& data) {
    istringstream ss(data);
    return deserializeHelper(ss);
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 17 — BACKTRACKING
// ═══════════════════════════════════════════════════════════════════════════

// ── 17A. Combination Sum (LC 39) ─────────────────────────────────────────
// All unique combinations of candidates (reusable) that sum to target.
// Approach: sort + DFS with start index to prevent reverse duplicates.
// Prune: if candidates[i] > remaining target, stop (sorted order).
// Complexity: O(n^(target/min)).
void combinationSumHelper(vector<int>& cands, int target, int start,
                           vector<int>& path, vector<vector<int>>& res) {
    if (target == 0) { res.push_back(path); return; }
    for (int i = start; i < (int)cands.size(); i++) {
        if (cands[i] > target) break;
        path.push_back(cands[i]);
        combinationSumHelper(cands, target - cands[i], i, path, res);
        path.pop_back();
    }
}
vector<vector<int>> combinationSum(vector<int>& candidates, int target) {
    sort(candidates.begin(), candidates.end());
    vector<vector<int>> res;
    vector<int> path;
    combinationSumHelper(candidates, target, 0, path, res);
    return res;
}

// ── 17B. Permutations (LC 46) ────────────────────────────────────────────
// Generate all permutations of distinct integers.
// Approach: swap-based DFS — swap nums[start] with each nums[i≥start],
// recurse, then swap back. No extra visited array needed.
// Complexity: O(n * n!).
void permuteHelper(vector<int>& nums, int start, vector<vector<int>>& res) {
    if (start == (int)nums.size()) { res.push_back(nums); return; }
    for (int i = start; i < (int)nums.size(); i++) {
        swap(nums[start], nums[i]);
        permuteHelper(nums, start + 1, res);
        swap(nums[start], nums[i]);
    }
}
vector<vector<int>> permute(vector<int>& nums) {
    vector<vector<int>> res;
    permuteHelper(nums, 0, res);
    return res;
}

// ── 17C. Letter Combinations of a Phone Number (LC 17) ───────────────────
// Given a digit string, return all letter combinations (phone keypad mapping).
// Approach: DFS/backtracking over each digit's possible letters.
// Complexity: O(4^n * n) where n = |digits| (at most 4 letters per digit).
vector<string> letterCombinations(const string& digits) {
    if (digits.empty()) return {};
    vector<string> keys = {"","","abc","def","ghi","jkl","mno","pqrs","tuv","wxyz"};
    vector<string> res;
    string path;
    function<void(int)> dfs = [&](int idx) {
        if (idx == (int)digits.size()) { res.push_back(path); return; }
        for (char c : keys[digits[idx] - '0']) {
            path += c;
            dfs(idx + 1);
            path.pop_back();
        }
    };
    dfs(0);
    return res;
}

// ═══════════════════════════════════════════════════════════════════════════
// MAIN — smoke tests
// ═══════════════════════════════════════════════════════════════════════════

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    // ── Section 1: Strings ────────────────────────────────────────────────
    cout << "=== SECTION 1: STRINGS ===" << endl;

    cout << "decodeString(\"3[a2[c]]\") = " << decodeString("3[a2[c]]") << endl;
    // Expected: accaccacc

    {
        string S = "heeellooo";
        vector<string> words = {"hello","hi","helo"};
        cout << "expressiveWords(\"heeellooo\", ...) = " << expressiveWords(S, words) << endl;
        // Expected: 1
    }

    cout << "getPermutation(3, 3) = " << getPermutation(3, 3) << endl;
    // Expected: "213"

    {
        auto palperms = generatePalindromes("aabb");
        cout << "generatePalindromes(\"aabb\"): ";
        for (auto& s : palperms) cout << s << " ";
        cout << endl;
        // Expected: abba baab
    }

    // ── Section 2: Sliding Window ─────────────────────────────────────────
    cout << "\n=== SECTION 2: SLIDING WINDOW ===" << endl;

    cout << "minWindow(\"ADOBECODEBANC\", \"ABC\") = "
         << minWindow("ADOBECODEBANC", "ABC") << endl;
    // Expected: "BANC"

    cout << "lengthOfLongestSubstringKDistinct(\"eceba\", 2) = "
         << lengthOfLongestSubstringKDistinct("eceba", 2) << endl;
    // Expected: 3

    {
        vector<int> fruits = {1,2,1,2,3};
        cout << "totalFruit([1,2,1,2,3]) = " << totalFruit(fruits) << endl;
        // Expected: 4 (window [1,2,1,2])
    }
    {
        vector<int> nums = {1,1,1,0,0,0,1,1,1,1,0};
        cout << "longestOnes(k=2) = " << longestOnes(nums, 2) << endl;
        // Expected: 6
    }
    {
        vector<int> nums = {1,3,-1,-3,5,3,6,7};
        auto res = maxSlidingWindow(nums, 3);
        cout << "maxSlidingWindow(k=3): ";
        for (int x : res) cout << x << " ";
        cout << endl;
        // Expected: 3 3 5 5 6 7
    }

    // ── Section 3: Binary Search ──────────────────────────────────────────
    cout << "\n=== SECTION 3: BINARY SEARCH ===" << endl;

    BAD_VERSION_THRESHOLD = 4;
    cout << "firstBadVersion(5) = " << firstBadVersion(5) << endl;
    // Expected: 4

    {
        vector<vector<int>> heights = {{1,2,2},{3,8,2},{5,3,5}};
        cout << "minimumEffortPath = " << minimumEffortPath(heights) << endl;
        // Expected: 2
    }
    {
        vector<int> piles = {3,6,7,11};
        cout << "minEatingSpeed(h=8) = " << minEatingSpeed(piles, 8) << endl;
        // Expected: 4
    }
    {
        vector<int> nums = {1,2,3,1};
        cout << "findPeakElement([1,2,3,1]) = " << findPeakElement(nums) << endl;
        // Expected: 2
    }

    // ── Section 4: Monotonic Stack ────────────────────────────────────────
    cout << "\n=== SECTION 4: MONOTONIC STACK ===" << endl;

    {
        vector<int> h = {10,6,8,5,11,9};
        auto v = canSeePersonsCount(h);
        cout << "canSeePersonsCount: ";
        for (int x : v) cout << x << " ";
        cout << endl;
        // Expected: 3 1 2 1 1 0
    }
    {
        vector<int> temps = {73,74,75,71,69,72,76,73};
        auto v = dailyTemperatures(temps);
        cout << "dailyTemperatures: ";
        for (int x : v) cout << x << " ";
        cout << endl;
        // Expected: 1 1 4 2 1 1 0 0
    }
    {
        vector<int> h = {2,1,5,6,2,3};
        cout << "largestRectangleArea([2,1,5,6,2,3]) = " << largestRectangleArea(h) << endl;
        // Expected: 10
    }

    // ── Section 5: Graph ──────────────────────────────────────────────────
    cout << "\n=== SECTION 5: GRAPH ===" << endl;

    {
        vector<vector<int>> edges = {{0,1},{1,2},{2,0}};
        cout << "validPath(3, edges, 0, 2) = " << validPath(3, edges, 0, 2) << endl;
        // Expected: 1
    }
    {
        vector<vector<int>> grid = {{1,1,0,0,0},{1,1,0,0,0},{0,0,0,1,1},{0,0,0,1,1}};
        cout << "numDistinctIslands = " << numDistinctIslands(grid) << endl;
        // Expected: 1
    }
    {
        vector<vector<int>> edges = {{0,1},{0,2},{2,3},{2,4}};
        auto dists = sumOfDistancesInTree(5, edges);
        cout << "sumOfDistancesInTree(5): ";
        for (int d : dists) cout << d << " ";
        cout << endl;
        // Expected: 6 9 5 8 8
    }
    {
        vector<vector<char>> grid = {{'1','1','0'},{'0','1','0'},{'0','0','1'}};
        cout << "numIslands = " << numIslands(grid) << endl;
        // Expected: 2
    }
    {
        vector<vector<int>> image = {{1,1,1},{1,1,0},{1,0,1}};
        auto res = floodFill(image, 1, 1, 2);
        cout << "floodFill: ";
        for (auto& row : res) { for (int x : row) cout << x; cout << "|"; }
        cout << endl;
        // Expected: 222|220|201  (2s flood from (1,1))
    }
    {
        vector<string> wordList = {"hot","dot","dog","lot","log","cog"};
        cout << "ladderLength(hit->cog) = "
             << ladderLength("hit","cog",wordList) << endl;
        // Expected: 5
    }

    // ── Section 6: Topo Sort ──────────────────────────────────────────────
    cout << "\n=== SECTION 6: TOPOLOGICAL SORT ===" << endl;

    {
        vector<vector<int>> prereqs = {{1,0},{0,1}};
        cout << "canFinish(2, cycle) = " << canFinish(2, prereqs) << endl;
        // Expected: 0
    }
    {
        vector<vector<int>> prereqs = {{1,0}};
        auto order = findOrder(2, prereqs);
        cout << "findOrder(2, [[1,0]]): ";
        for (int x : order) cout << x << " ";
        cout << endl;
        // Expected: 0 1
    }
    {
        vector<string> words = {"wrt","wrf","er","ett","rftt"};
        cout << "alienOrder: " << alienOrder(words) << endl;
        // Expected: wertf (one valid ordering)
    }

    // ── Section 7: DP ─────────────────────────────────────────────────────
    cout << "\n=== SECTION 7: DYNAMIC PROGRAMMING ===" << endl;

    {
        vector<int> nums = {2,7,9,3,1};
        cout << "rob([2,7,9,3,1]) = " << rob(nums) << endl;
        // Expected: 12
    }
    {
        vector<int> nums = {1,5,11,5};
        cout << "canPartition([1,5,11,5]) = " << canPartition(nums) << endl;
        // Expected: 1
    }
    cout << "numDecodings(\"226\") = " << numDecodings("226") << endl;
    // Expected: 3
    cout << "longestDupSubstring(\"banana\") = " << longestDupSubstring("banana") << endl;
    // Expected: "ana"
    {
        vector<int> coins = {1,5,11};
        cout << "coinChange(coins=[1,5,11], 15) = " << coinChange(coins, 15) << endl;
        // Expected: 3  (5+5+5)
    }
    {
        vector<int> nums = {2,3,-2,4};
        cout << "maxProduct([2,3,-2,4]) = " << maxProduct(nums) << endl;
        // Expected: 6
    }
    cout << "LCS(\"abcde\",\"ace\") = " << longestCommonSubsequence("abcde","ace") << endl;
    // Expected: 3

    // ── Section 8: Intervals ──────────────────────────────────────────────
    cout << "\n=== SECTION 8: INTERVALS ===" << endl;

    {
        vector<vector<int>> meetings = {{0,10},{1,5},{2,7},{3,4}};
        cout << "mostBooked(2, ...) = " << mostBooked(2, meetings) << endl;
        // Expected: 0
    }
    {
        vector<vector<pair<int,int>>> sched = {{{1,3},{6,7}},{{2,4}},{{2,5},{9,12}}};
        auto gaps = employeeFreeTime(sched);
        cout << "employeeFreeTime gaps: ";
        for (auto& g : gaps) cout << "[" << g.first << "," << g.second << "] ";
        cout << endl;
        // Expected: [5,6] [7,9]
    }
    {
        vector<vector<int>> ivs = {{1,3},{2,6},{8,10},{15,18}};
        auto merged = mergeIntervals(ivs);
        cout << "mergeIntervals: ";
        for (auto& v : merged) cout << "[" << v[0] << "," << v[1] << "] ";
        cout << endl;
        // Expected: [1,6] [8,10] [15,18]
    }

    // ── Section 9: Matrix ─────────────────────────────────────────────────
    cout << "\n=== SECTION 9: MATRIX ===" << endl;

    {
        vector<vector<int>> mat = {{9,9,4},{6,6,8},{2,1,1}};
        cout << "longestIncreasingPath = " << longestIncreasingPath(mat) << endl;
        // Expected: 4
    }
    {
        vector<vector<int>> mat = {{1,2,3},{4,5,6},{7,8,9}};
        auto res = spiralOrder(mat);
        cout << "spiralOrder: ";
        for (int x : res) cout << x << " ";
        cout << endl;
        // Expected: 1 2 3 6 9 8 7 4 5
    }

    // ── Section 10: Math ──────────────────────────────────────────────────
    cout << "\n=== SECTION 10: MATH/SIMULATION ===" << endl;

    cout << "isHappy(19) = " << isHappy(19) << endl;   // Expected: 1
    cout << "isHappy(2)  = " << isHappy(2)  << endl;   // Expected: 0
    {
        auto groups = largeGroupPositions("abbxxxxzzy");
        cout << "largeGroupPositions: ";
        for (auto& g : groups) cout << "[" << g[0] << "," << g[1] << "] ";
        cout << endl;
        // Expected: [3,6]
    }
    cout << "isRobotBounded(\"GGLLGG\") = " << isRobotBounded("GGLLGG") << endl;
    // Expected: 1

    // ── Section 11: Google-Specific ───────────────────────────────────────
    cout << "\n=== SECTION 11: GOOGLE-SPECIFIC ===" << endl;

    {
        vector<string> cpdomains = {"9001 discuss.leetcode.com","50 leetcode.com","1 blog.medium.com"};
        auto subs = subdomainVisits(cpdomains);
        cout << "subdomainVisits: ";
        for (auto& s : subs) cout << "[" << s << "] ";
        cout << endl;
    }
    {
        vector<vector<int>> A = {{1,0,0},{-1,0,3}};
        vector<vector<int>> B = {{7,0,0},{0,0,0},{0,0,1}};
        auto C = multiply(A, B);
        cout << "sparse multiply: ";
        for (auto& row : C) { for (int x : row) cout << x << " "; cout << "| "; }
        cout << endl;
        // Expected: 7 0 0 | -7 0 3
    }
    {
        vector<tuple<int,int,int>> queries = {make_tuple(1,3,2), make_tuple(0,2,1)};
        auto arr = applyRangeUpdates(5, queries);
        cout << "rangeUpdate: ";
        for (int x : arr) cout << x << " ";
        cout << endl;
        // Expected: 1 3 3 2 0
    }
    {
        vector<tuple<string,int,int>> jobs = {
            make_tuple("job1",0,10), make_tuple("job2",1,5), make_tuple("job3",6,20)
        };
        cout << "detectFirstTimedOutJob(8): " << detectFirstTimedOutJob(jobs,8) << endl;
        // Expected: job1
    }
    {
        vector<pair<string,int>> msgs = {{"alice",1},{"bob",2},{"alice",3},{"carol",4},{"bob",5},{"alice",6}};
        auto top2 = topKActiveUsers(msgs, 2);
        cout << "topKActiveUsers(k=2): ";
        for (auto& u : top2) cout << u << " ";
        cout << endl;
        // Expected: alice bob
    }

    // ── Section 12: Two Pointers ──────────────────────────────────────────
    cout << "\n=== SECTION 12: TWO POINTERS ===" << endl;

    {
        vector<int> nums = {-1,0,1,2,-1,-4};
        auto res = threeSum(nums);
        cout << "3Sum([-1,0,1,2,-1,-4]): ";
        for (auto& v : res) cout << "[" << v[0] << "," << v[1] << "," << v[2] << "] ";
        cout << endl;
        // Expected: [-1,-1,2] [-1,0,1]
    }
    {
        vector<int> h = {1,8,6,2,5,4,8,3,7};
        cout << "maxArea = " << maxArea(h) << endl;
        // Expected: 49
    }
    {
        vector<int> h = {0,1,0,2,1,0,1,3,2,1,2,1};
        cout << "trap = " << trap(h) << endl;
        // Expected: 6
    }

    // ── Section 13: Tries ─────────────────────────────────────────────────
    cout << "\n=== SECTION 13: TRIES ===" << endl;

    {
        Trie trie;
        trie.insert("apple");
        cout << "Trie search(apple)=" << trie.search("apple")
             << " search(app)=" << trie.search("app")
             << " startsWith(app)=" << trie.startsWith("app") << endl;
        // Expected: 1 0 1
    }
    {
        WordDictionary wd;
        wd.addWord("bad"); wd.addWord("dad"); wd.addWord("mad");
        cout << "WordDict search(.ad)=" << wd.search(".ad")
             << " search(b..)=" << wd.search("b..") << endl;
        // Expected: 1 1
    }
    {
        vector<vector<char>> board = {{'o','a','a','n'},{'e','t','a','e'},
                                      {'i','h','k','r'},{'i','f','l','v'}};
        vector<string> words = {"oath","pea","eat","rain"};
        auto found = findWords(board, words);
        cout << "findWords: ";
        for (auto& w : found) cout << w << " ";
        cout << endl;
        // Expected: eat oath (order may vary)
    }

    // ── Section 14: Heap / PQ ─────────────────────────────────────────────
    cout << "\n=== SECTION 14: HEAP / PRIORITY QUEUE ===" << endl;

    {
        vector<vector<int>> pts = {{1,3},{-2,2},{5,8},{0,1}};
        auto res = kClosest(pts, 2);
        cout << "kClosest(k=2): ";
        for (auto& p : res) cout << "[" << p[0] << "," << p[1] << "] ";
        cout << endl;
        // Expected: two of {[0,1],[-2,2]} (closest)
    }
    {
        MedianFinder mf;
        mf.addNum(1); mf.addNum(2); mf.addNum(3);
        cout << "MedianFinder median after [1,2,3] = " << mf.findMedian() << endl;
        // Expected: 2
    }
    {
        // Build two lists: [1->4->5] and [1->3->4]
        ListNode* l1 = new ListNode(1); l1->next = new ListNode(4); l1->next->next = new ListNode(5);
        ListNode* l2 = new ListNode(1); l2->next = new ListNode(3); l2->next->next = new ListNode(4);
        vector<ListNode*> lists = {l1, l2};
        ListNode* merged = mergeKLists(lists);
        cout << "mergeKLists: ";
        while (merged) { cout << merged->val << " "; merged = merged->next; }
        cout << endl;
        // Expected: 1 1 3 4 4 5
    }

    // ── Section 15: Bit Manipulation ──────────────────────────────────────
    cout << "\n=== SECTION 15: BIT MANIPULATION ===" << endl;

    {
        vector<int> nums = {4,1,2,1,2};
        cout << "singleNumber([4,1,2,1,2]) = " << singleNumber(nums) << endl;
        // Expected: 4
    }
    {
        auto bits = countBits(5);
        cout << "countBits(5): ";
        for (int x : bits) cout << x << " ";
        cout << endl;
        // Expected: 0 1 1 2 1 2
    }
    cout << "hammingWeight(11) = " << hammingWeight(11) << endl;
    // Expected: 3  (1011)
    cout << "reverseBits(43261596) = " << reverseBits(43261596) << endl;
    // Expected: 964176192
    {
        vector<int> nums = {3,0,1};
        cout << "missingNumber([3,0,1]) = " << missingNumber(nums) << endl;
        // Expected: 2
    }

    // ── Section 16: Trees ─────────────────────────────────────────────────
    cout << "\n=== SECTION 16: TREES ===" << endl;

    {
        // Build tree: 3 -> (9, 20 -> (15, 7))
        TreeNode* root = new TreeNode(3);
        root->left  = new TreeNode(9);
        root->right = new TreeNode(20);
        root->right->left  = new TreeNode(15);
        root->right->right = new TreeNode(7);

        auto lo = levelOrder(root);
        cout << "levelOrder: ";
        for (auto& lv : lo) { for (int x : lv) cout << x << " "; cout << "| "; }
        cout << endl;
        // Expected: 3 | 9 20 | 15 7 |

        auto zz = zigzagLevelOrder(root);
        cout << "zigzag:     ";
        for (auto& lv : zz) { for (int x : lv) cout << x << " "; cout << "| "; }
        cout << endl;
        // Expected: 3 | 20 9 | 15 7 |

        TreeNode* p = root->right->left;   // node 15
        TreeNode* q = root->right->right;  // node 7
        TreeNode* lca = lowestCommonAncestor(root, p, q);
        cout << "LCA(15,7) = " << lca->val << endl;
        // Expected: 20

        cout << "maxPathSum = " << maxPathSum(root) << endl;
        // Expected: 47  (9+3+20+15)

        string s = serialize(root);
        TreeNode* root2 = deserialize(s);
        cout << "serialize/deserialize root val = " << root2->val << endl;
        // Expected: 3
    }

    // ── Section 17: Backtracking ──────────────────────────────────────────
    cout << "\n=== SECTION 17: BACKTRACKING ===" << endl;

    {
        vector<int> cands = {2,3,6,7};
        auto res = combinationSum(cands, 7);
        cout << "combinationSum(target=7): " << res.size() << " combos" << endl;
        // Expected: 2  ([7] and [2,2,3])
    }
    {
        vector<int> nums = {1,2,3};
        auto res = permute(nums);
        cout << "permute([1,2,3]): " << res.size() << " permutations" << endl;
        // Expected: 6
    }
    {
        auto res = letterCombinations("23");
        cout << "letterCombinations(\"23\"): ";
        for (auto& s : res) cout << s << " ";
        cout << endl;
        // Expected: ad ae af bd be bf cd ce cf
    }

    return 0;
}
