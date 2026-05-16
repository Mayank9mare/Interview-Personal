// AmazonDSA.cpp  — Amazon-tagged DSA problems (C++14, g++ -std=c++14)
// Compile:  g++ -std=c++14 -O2 -o AmazonDSA AmazonDSA.cpp
//
// Sections:
//   1.  Arrays / Sliding Window
//   2.  Heap / Intervals
//   3.  Graphs
//   4.  Stack / Design

#include <bits/stdc++.h>
using namespace std;

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 1 — ARRAYS / SLIDING WINDOW
// ═══════════════════════════════════════════════════════════════════════════

// ── 1A. Two Sum Closest To And Less Than Target ───────────────────────────
// Find the pair whose sum is maximum among all sums strictly less than target.
// Approach: sort, then two pointers from both ends. If sum < target, it's a
// candidate — record it and advance left. If sum >= target, shrink from right.
// Complexity: O(n log n) time, O(1) space.
int twoSumClosest(vector<int>& nums, int target) {
    sort(nums.begin(), nums.end());
    int lo = 0, hi = (int)nums.size() - 1, best = INT_MIN;
    while (lo < hi) {
        int s = nums[lo] + nums[hi];
        if (s < target) {
            best = max(best, s);
            lo++;
        } else {
            hi--;
        }
    }
    return best;
}

// ── 1B. Longest Subarray with Sum Equals Zero ────────────────────────────
// Find the length of the longest contiguous subarray whose elements sum to 0.
// Approach: prefix sum + hash map. Store the first index where each prefix sum
// appears. When the same prefix sum recurs at index i, the subarray
// (firstSeen+1 .. i) has sum 0.
// Complexity: O(n) time, O(n) space.
int longestZeroSumSubarray(vector<int>& nums) {
    unordered_map<int, int> firstSeen;
    firstSeen[0] = -1; // empty prefix has sum 0 at index -1
    int prefix = 0, best = 0;
    for (int i = 0; i < (int)nums.size(); i++) {
        prefix += nums[i];
        if (firstSeen.count(prefix)) {
            best = max(best, i - firstSeen[prefix]);
        } else {
            firstSeen[prefix] = i;
        }
    }
    return best;
}

// ── 1C. Longest Substring with At Most K Distinct Characters (LC 340) ────
// Find the length of the longest substring containing at most k distinct chars.
// Approach: sliding window with a frequency map. Shrink left whenever the
// number of distinct characters exceeds k.
// Complexity: O(n) time, O(k) space.
int longestSubstringKDistinct(const string& s, int k) {
    unordered_map<char, int> freq;
    int lo = 0, best = 0;
    for (int hi = 0; hi < (int)s.size(); hi++) {
        freq[s[hi]]++;
        while ((int)freq.size() > k) {
            freq[s[lo]]--;
            if (freq[s[lo]] == 0) freq.erase(s[lo]);
            lo++;
        }
        best = max(best, hi - lo + 1);
    }
    return best;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 2 — HEAP / INTERVALS
// ═══════════════════════════════════════════════════════════════════════════

// ── 2A. Minimum Cost to Connect Sticks (LC 1167) ─────────────────────────
// Each join costs the sum of two sticks; find the minimum total cost.
// Approach: greedy with a min-heap — always merge the two shortest sticks.
// This is equivalent to building a Huffman tree where leaf depths minimise
// total cost.
// Complexity: O(n log n) time, O(n) space.
long long minCostConnectSticks(vector<int>& sticks) {
    priority_queue<long long, vector<long long>, greater<long long>> pq(
        sticks.begin(), sticks.end());
    long long cost = 0;
    while (pq.size() > 1) {
        long long a = pq.top(); pq.pop();
        long long b = pq.top(); pq.pop();
        cost += a + b;
        pq.push(a + b);
    }
    return cost;
}

// ── 2B. Minimum Meeting Rooms (LC 253) ───────────────────────────────────
// Given n meeting intervals, find the minimum number of rooms required.
// Approach: sort by start time; use a min-heap of end times. For each
// meeting, if it starts after the earliest-ending ongoing meeting, reuse
// that room (pop + push new end). Otherwise add a room.
// Complexity: O(n log n) time, O(n) space.
int minMeetingRooms(vector<pair<int,int>>& intervals) {
    sort(intervals.begin(), intervals.end());
    priority_queue<int, vector<int>, greater<int>> endTimes; // min-heap of end times
    for (auto& iv : intervals) {
        if (!endTimes.empty() && endTimes.top() <= iv.first) {
            endTimes.pop(); // reuse the room that freed up earliest
        }
        endTimes.push(iv.second);
    }
    return (int)endTimes.size();
}

// ── 2C. Employee Free Time (LC 759) ──────────────────────────────────────
// Given each employee's sorted working intervals, find all free time gaps
// shared across all employees.
// Approach: merge all intervals into one sorted list, then scan for gaps
// between consecutive merged intervals.
// Complexity: O(N log N) time where N = total intervals, O(N) space.
vector<pair<int,int>> employeeFreeTime(vector<vector<pair<int,int>>>& schedule) {
    vector<pair<int,int>> all;
    for (auto& emp : schedule)
        for (auto& iv : emp)
            all.push_back(iv);
    sort(all.begin(), all.end());

    vector<pair<int,int>> free;
    int end = all[0].second;
    for (int i = 1; i < (int)all.size(); i++) {
        if (all[i].first > end) {
            free.push_back({end, all[i].first}); // gap between merged blocks
        }
        end = max(end, all[i].second);
    }
    return free;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 3 — GRAPHS
// ═══════════════════════════════════════════════════════════════════════════

// ── 3A. Count Connected Components (LC 323) ──────────────────────────────
// Count the number of connected components in an undirected graph with n nodes.
// Approach: Union-Find with path compression and union by rank. Each union
// reduces the component count by 1.
// Complexity: O(n + E * α(n)) ≈ O(n + E) time, O(n) space.
int countComponents(int n, vector<pair<int,int>>& edges) {
    vector<int> parent(n), rank(n, 0);
    iota(parent.begin(), parent.end(), 0); // parent[i] = i initially

    function<int(int)> find = [&](int x) -> int {
        if (parent[x] != x) parent[x] = find(parent[x]); // path compression
        return parent[x];
    };

    int components = n;
    for (auto& e : edges) {
        int pu = find(e.first), pv = find(e.second);
        if (pu != pv) {
            // union by rank
            if (rank[pu] < rank[pv]) swap(pu, pv);
            parent[pv] = pu;
            if (rank[pu] == rank[pv]) rank[pu]++;
            components--;
        }
    }
    return components;
}

// ── 3B. Graph Valid Tree (LC 261) ────────────────────────────────────────
// Determine if n nodes and the given edges form a valid tree (connected + acyclic).
// Approach: a tree has exactly n-1 edges and is fully connected. Use Union-Find:
// if any edge connects two nodes already in the same component → cycle → not a tree.
// Complexity: O(n + E) time, O(n) space.
bool validTree(int n, vector<pair<int,int>>& edges) {
    if ((int)edges.size() != n - 1) return false; // wrong edge count
    vector<int> parent(n);
    iota(parent.begin(), parent.end(), 0);

    function<int(int)> find = [&](int x) -> int {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    };

    for (auto& e : edges) {
        int pu = find(e.first), pv = find(e.second);
        if (pu == pv) return false; // cycle detected
        parent[pu] = pv;
    }
    return true;
}

// ── 3C. Find the Celebrity (LC 277) ──────────────────────────────────────
// A celebrity is known by everyone but knows nobody. Find them in O(n).
// Approach: one linear pass to find the candidate — if A knows B, A can't be
// the celebrity; move candidate to B. Then verify the candidate with two passes.
// Complexity: O(n) time (assuming knows(a,b) is O(1)), O(1) space.
//
// knows(a, b) simulated via an adjacency matrix for testing.
int findCelebrity(int n, vector<vector<int>>& knows) {
    int candidate = 0;
    for (int i = 1; i < n; i++) {
        if (knows[candidate][i]) candidate = i; // candidate knows i → can't be celeb
    }
    // verify: candidate knows nobody AND everyone knows candidate
    for (int i = 0; i < n; i++) {
        if (i == candidate) continue;
        if (knows[candidate][i] || !knows[i][candidate]) return -1;
    }
    return candidate;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 4 — STACK / DESIGN
// ═══════════════════════════════════════════════════════════════════════════

// ── 4A. Basic Calculator II (LC 227) ─────────────────────────────────────
// Evaluate a string expression with +, -, *, / (integer division, no parens).
// Approach: scan left to right; track the last operator. For + and -, push the
// signed value onto the stack. For * and /, pop the top and push the result.
// Sum the stack at the end.
// Complexity: O(n) time, O(n) space.
int calculate(const string& s) {
    stack<int> stk;
    int num = 0;
    char op = '+'; // op applied to current num
    for (int i = 0; i <= (int)s.size(); i++) {
        char c = (i < (int)s.size()) ? s[i] : '+'; // sentinel flush at end
        if (isdigit(c)) {
            num = num * 10 + (c - '0');
        } else if (c != ' ') {
            if (op == '+') stk.push(num);
            else if (op == '-') stk.push(-num);
            else if (op == '*') { int t = stk.top(); stk.pop(); stk.push(t * num); }
            else if (op == '/') { int t = stk.top(); stk.pop(); stk.push(t / num); }
            op = c;
            num = 0;
        }
    }
    int result = 0;
    while (!stk.empty()) { result += stk.top(); stk.pop(); }
    return result;
}

// ── 4B. Design Tic Tac Toe (LC 348) ──────────────────────────────────────
// Support O(1) per move: determine if the current move wins the game.
// Approach: track row sums and column sums per player (±1 per move).
// Also track two diagonal sums. A win is when any sum reaches ±n.
// Avoids scanning the board on every move.
// Complexity: O(1) per move, O(n) space.
struct TicTacToe {
    int sz;
    vector<int> rows, cols;
    int diag, antiDiag;

    TicTacToe(int size) : sz(size), rows(size, 0), cols(size, 0), diag(0), antiDiag(0) {}

    // Returns 0 if no winner, 1 if player 1 wins, 2 if player 2 wins.
    int move(int row, int col, int player) {
        int v = (player == 1) ? 1 : -1; // +1 for P1, -1 for P2
        rows[row] += v;
        cols[col] += v;
        if (row == col) diag += v;
        if (row + col == sz - 1) antiDiag += v;
        if (abs(rows[row]) == sz || abs(cols[col]) == sz ||
            abs(diag) == sz || abs(antiDiag) == sz) return player;
        return 0;
    }
};

// ═══════════════════════════════════════════════════════════════════════════
// MAIN — smoke tests
// ═══════════════════════════════════════════════════════════════════════════

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    // Section 1: Arrays / Sliding Window
    cout << "=== SECTION 1: ARRAYS / SLIDING WINDOW ===" << endl;

    vector<int> a1 = {1, 3, 8, 12};
    cout << "twoSumClosest([1,3,8,12], target=10) = " << twoSumClosest(a1, 10) << endl;
    // Expected: 9 (1+8)

    vector<int> a2 = {3, -1, 2, -4, 1, -2};
    cout << "longestZeroSumSubarray([3,-1,2,-4,1,-2]) = " << longestZeroSumSubarray(a2) << endl;
    // Expected: 5 (indices 1..5: -1+2-4+1-2= -4... let me trace: prefix[0]=3,prefix[1]=2,prefix[2]=4,prefix[3]=0 -> seen at -1, length=4)
    // Actually: prefix sums: 3,2,4,0,1,-1 -> prefix[3]=0 which was seen at -1, so length=3-(-1)=4
    // Expected: 4

    cout << "longestSubstringKDistinct(\"eceba\", k=2) = " << longestSubstringKDistinct("eceba", 2) << endl;
    // Expected: 3 ("ece")

    // Section 2: Heap / Intervals
    cout << "\n=== SECTION 2: HEAP / INTERVALS ===" << endl;

    vector<int> sticks = {2, 4, 3};
    cout << "minCostConnectSticks([2,4,3]) = " << minCostConnectSticks(sticks) << endl;
    // Expected: 14 (merge 2+3=5, cost=5; merge 4+5=9, cost=9; total=14)

    vector<pair<int,int>> meetings = {{0,30},{5,10},{15,20}};
    cout << "minMeetingRooms([[0,30],[5,10],[15,20]]) = " << minMeetingRooms(meetings) << endl;
    // Expected: 2

    vector<vector<pair<int,int>>> sched = {{{1,3},{6,7}}, {{2,4}}, {{2,5},{9,12}}};
    auto free = employeeFreeTime(sched);
    cout << "employeeFreeTime: free intervals = ";
    for (auto& iv : free) cout << "[" << iv.first << "," << iv.second << "] ";
    cout << endl;
    // Expected: [5,6] [7,9]

    // Section 3: Graphs
    cout << "\n=== SECTION 3: GRAPHS ===" << endl;

    vector<pair<int,int>> edges1 = {{0,1},{1,2},{3,4}};
    cout << "countComponents(5, [[0,1],[1,2],[3,4]]) = " << countComponents(5, edges1) << endl;
    // Expected: 2

    vector<pair<int,int>> edges2 = {{0,1},{0,2},{0,3},{1,4}};
    cout << "validTree(5, [[0,1],[0,2],[0,3],[1,4]]) = " << validTree(5, edges2) << endl;
    // Expected: 1 (true)

    vector<pair<int,int>> edges3 = {{0,1},{1,2},{2,0}};
    cout << "validTree(3, [[0,1],[1,2],[2,0]]) = " << validTree(3, edges3) << endl;
    // Expected: 0 (false, has cycle)

    // knows[a][b] = 1 means a knows b; celebrity = node 2 (known by all, knows none)
    vector<vector<int>> knows = {{0,1,1},{0,0,1},{0,0,0}};
    cout << "findCelebrity(3) = " << findCelebrity(3, knows) << endl;
    // Expected: 2

    // Section 4: Stack / Design
    cout << "\n=== SECTION 4: STACK / DESIGN ===" << endl;

    cout << "calculate(\"3+2*2\") = " << calculate("3+2*2") << endl;
    // Expected: 7

    cout << "calculate(\"14-3/2\") = " << calculate("14-3/2") << endl;
    // Expected: 13

    TicTacToe ttt(3);
    // Evaluate each move in a separate statement — chaining calls in one cout expression
    // gives unspecified evaluation order in C++14 (right-to-left on GCC), corrupting game state.
    int m1 = ttt.move(0,0,1), m2 = ttt.move(0,2,2), m3 = ttt.move(1,1,1);
    int m4 = ttt.move(2,0,2), m5 = ttt.move(2,2,1);
    cout << "TicTacToe moves: " << m1 << " " << m2 << " " << m3 << " " << m4 << " " << m5 << endl;
    // Expected: 0 0 0 0 1  (player 1 wins on diagonal at (0,0),(1,1),(2,2))

    return 0;
}
