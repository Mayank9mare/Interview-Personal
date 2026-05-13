#include <bits/stdc++.h>
using namespace std;

// ─────────────────────────────────────────────────────────────────────────
// 1. Sliding Window — Fixed Size
//    Pattern: maintain a window of size k; add right, remove left
// ─────────────────────────────────────────────────────────────────────────
// Max sum subarray of size k — O(n)
int maxSumFixed(vector<int>& arr, int k) {
    int sum = 0;
    for (int i = 0; i < k; i++) sum += arr[i];
    int mx = sum;
    for (int i = k; i < (int)arr.size(); i++) {
        sum += arr[i] - arr[i - k];
        mx = max(mx, sum);
    }
    return mx;
}

// ─────────────────────────────────────────────────────────────────────────
// 2. Sliding Window — Variable Size
//    Pattern: expand right until invalid, shrink left until valid again
// ─────────────────────────────────────────────────────────────────────────
// Longest substring without repeating characters — O(n)
int longestNoRepeat(const string& s) {
    unordered_map<char, int> last;
    int mx = 0, left = 0;
    for (int right = 0; right < (int)s.size(); right++) {
        char c = s[right];
        if (last.count(c) && last[c] >= left) left = last[c] + 1;
        last[c] = right;
        mx = max(mx, right - left + 1);
    }
    return mx;
}

// Minimum window containing all chars of t — O(n)
string minWindow(const string& s, const string& t) {
    unordered_map<char, int> need;
    for (char c : t) need[c]++;
    int have = 0, required = need.size(), left = 0;
    int minLen = INT_MAX, minL = 0;
    unordered_map<char, int> window;
    for (int right = 0; right < (int)s.size(); right++) {
        char c = s[right];
        window[c]++;
        if (need.count(c) && window[c] == need[c]) have++;
        while (have == required) {
            if (right - left + 1 < minLen) { minLen = right - left + 1; minL = left; }
            char lc = s[left++];
            window[lc]--;
            if (need.count(lc) && window[lc] < need[lc]) have--;
        }
    }
    return minLen == INT_MAX ? "" : s.substr(minL, minLen);
}

// ─────────────────────────────────────────────────────────────────────────
// 3. Two Pointers
//    Pattern: left/right from both ends; move based on comparison
// ─────────────────────────────────────────────────────────────────────────
// Two sum on sorted array — O(n)
vector<int> twoSum(vector<int>& arr, int target) {
    int l = 0, r = arr.size() - 1;
    while (l < r) {
        int sum = arr[l] + arr[r];
        if (sum == target) return {l, r};
        else if (sum < target) l++;
        else r--;
    }
    return {};
}

// 3Sum — all unique triplets summing to 0 — O(n²)
vector<vector<int>> threeSum(vector<int> nums) {
    sort(nums.begin(), nums.end());
    vector<vector<int>> res;
    for (int i = 0; i < (int)nums.size() - 2; i++) {
        if (i > 0 && nums[i] == nums[i-1]) continue;
        int l = i + 1, r = nums.size() - 1;
        while (l < r) {
            int sum = nums[i] + nums[l] + nums[r];
            if (sum == 0) {
                res.push_back({nums[i], nums[l], nums[r]});
                while (l < r && nums[l] == nums[l+1]) l++;
                while (l < r && nums[r] == nums[r-1]) r--;
                l++; r--;
            } else if (sum < 0) l++;
            else r--;
        }
    }
    return res;
}

// Container with most water — O(n)
int maxWater(vector<int>& height) {
    int l = 0, r = height.size() - 1, mx = 0;
    while (l < r) {
        mx = max(mx, min(height[l], height[r]) * (r - l));
        if (height[l] < height[r]) l++; else r--;
    }
    return mx;
}

// Trapping rain water — O(n)
int trapWater(vector<int>& height) {
    int l = 0, r = height.size() - 1, maxL = 0, maxR = 0, water = 0;
    while (l < r) {
        if (height[l] < height[r]) {
            maxL = max(maxL, height[l]);
            water += maxL - height[l++];
        } else {
            maxR = max(maxR, height[r]);
            water += maxR - height[r--];
        }
    }
    return water;
}

// ─────────────────────────────────────────────────────────────────────────
// 4. Fast & Slow Pointers
//    Pattern: slow moves 1 step, fast moves 2; meet at cycle or middle
// ─────────────────────────────────────────────────────────────────────────
struct ListNode {
    int val; ListNode* next;
    ListNode(int v) : val(v), next(nullptr) {}
};

// Detect cycle — O(n)
bool hasCycle(ListNode* head) {
    ListNode* slow = head, *fast = head;
    while (fast && fast->next) {
        slow = slow->next; fast = fast->next->next;
        if (slow == fast) return true;
    }
    return false;
}

// Find middle — O(n)  [even length: returns second middle]
ListNode* middle(ListNode* head) {
    ListNode* slow = head, *fast = head;
    while (fast && fast->next) { slow = slow->next; fast = fast->next->next; }
    return slow;
}

// ─────────────────────────────────────────────────────────────────────────
// 5. Binary Search
//    Key: use l + (r-l)/2 to avoid overflow; decide l=mid+1 or r=mid-1
// ─────────────────────────────────────────────────────────────────────────
// Standard — O(log n)
int binarySearch(vector<int>& arr, int target) {
    int l = 0, r = arr.size() - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (arr[mid] == target) return mid;
        else if (arr[mid] < target) l = mid + 1;
        else r = mid - 1;
    }
    return -1;
}

// First occurrence — O(log n)
int firstOccurrence(vector<int>& arr, int target) {
    int l = 0, r = arr.size() - 1, res = -1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (arr[mid] == target) { res = mid; r = mid - 1; }
        else if (arr[mid] < target) l = mid + 1;
        else r = mid - 1;
    }
    return res;
}

// Search in rotated sorted array — O(log n)
int searchRotated(vector<int>& arr, int target) {
    int l = 0, r = arr.size() - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (arr[mid] == target) return mid;
        if (arr[l] <= arr[mid]) {
            if (target >= arr[l] && target < arr[mid]) r = mid - 1;
            else l = mid + 1;
        } else {
            if (target > arr[mid] && target <= arr[r]) l = mid + 1;
            else r = mid - 1;
        }
    }
    return -1;
}

// Minimum in rotated sorted array — O(log n)
int minRotated(vector<int>& arr) {
    int l = 0, r = arr.size() - 1;
    while (l < r) {
        int mid = l + (r - l) / 2;
        if (arr[mid] > arr[r]) l = mid + 1; else r = mid;
    }
    return arr[l];
}

// ─────────────────────────────────────────────────────────────────────────
// 6. Merge Intervals
//    Pattern: sort by start, merge overlapping
// ─────────────────────────────────────────────────────────────────────────
// Merge overlapping — O(n log n)
vector<vector<int>> mergeIntervals(vector<vector<int>> intervals) {
    sort(intervals.begin(), intervals.end());
    vector<vector<int>> res = {intervals[0]};
    for (int i = 1; i < (int)intervals.size(); i++) {
        if (intervals[i][0] <= res.back()[1])
            res.back()[1] = max(res.back()[1], intervals[i][1]);
        else res.push_back(intervals[i]);
    }
    return res;
}

// Insert and merge a new interval — O(n)
vector<vector<int>> insertInterval(vector<vector<int>>& intervals, vector<int> newInterval) {
    vector<vector<int>> res;
    int i = 0, n = intervals.size();
    while (i < n && intervals[i][1] < newInterval[0]) res.push_back(intervals[i++]);
    while (i < n && intervals[i][0] <= newInterval[1]) {
        newInterval[0] = min(newInterval[0], intervals[i][0]);
        newInterval[1] = max(newInterval[1], intervals[i][1]);
        i++;
    }
    res.push_back(newInterval);
    while (i < n) res.push_back(intervals[i++]);
    return res;
}

// Meeting rooms II — min rooms needed — O(n log n)
int minMeetingRooms(vector<vector<int>>& intervals) {
    sort(intervals.begin(), intervals.end());
    priority_queue<int, vector<int>, greater<int>> ends; // min-heap of end times
    for (auto& iv : intervals) {
        if (!ends.empty() && ends.top() <= iv[0]) ends.pop(); // reuse room
        ends.push(iv[1]);
    }
    return ends.size();
}

// ─────────────────────────────────────────────────────────────────────────
// 7. Top K Elements (Heap)
//    Min-heap of size k → keeps k largest seen so far
// ─────────────────────────────────────────────────────────────────────────
// K largest — O(n log k)
vector<int> kLargest(vector<int>& nums, int k) {
    priority_queue<int, vector<int>, greater<int>> minHeap;
    for (int n : nums) {
        minHeap.push(n);
        if ((int)minHeap.size() > k) minHeap.pop();
    }
    vector<int> res;
    while (!minHeap.empty()) { res.push_back(minHeap.top()); minHeap.pop(); }
    return res;
}

// K most frequent — O(n log k)
vector<int> topKFrequent(vector<int>& nums, int k) {
    unordered_map<int, int> freq;
    for (int n : nums) freq[n]++;
    // min-heap: {freq, num}
    priority_queue<pair<int,int>, vector<pair<int,int>>, greater<>> minHeap;
    for (auto& kv : freq) {
        minHeap.push({kv.second, kv.first});
        if ((int)minHeap.size() > k) minHeap.pop();
    }
    vector<int> res;
    while (!minHeap.empty()) { res.push_back(minHeap.top().second); minHeap.pop(); }
    return res;
}

// Kth largest element — O(n log k)
int kthLargest(vector<int>& nums, int k) {
    priority_queue<int, vector<int>, greater<int>> minHeap;
    for (int n : nums) {
        minHeap.push(n);
        if ((int)minHeap.size() > k) minHeap.pop();
    }
    return minHeap.top();
}

// ─────────────────────────────────────────────────────────────────────────
// 8. Backtracking
//    Pattern: choose → recurse → unchoose (state restoration)
//    Prune early: sort candidates, break when candidate > remaining
// ─────────────────────────────────────────────────────────────────────────
// All subsets — O(2^n)
void backtrackSubsets(vector<int>& nums, int start, vector<int>& cur, vector<vector<int>>& res) {
    res.push_back(cur);
    for (int i = start; i < (int)nums.size(); i++) {
        cur.push_back(nums[i]);
        backtrackSubsets(nums, i + 1, cur, res);
        cur.pop_back();
    }
}
vector<vector<int>> subsets(vector<int>& nums) {
    vector<vector<int>> res; vector<int> cur;
    backtrackSubsets(nums, 0, cur, res);
    return res;
}

// All permutations — O(n!)
void backtrackPerm(vector<int>& nums, vector<bool>& used, vector<int>& cur, vector<vector<int>>& res) {
    if ((int)cur.size() == (int)nums.size()) { res.push_back(cur); return; }
    for (int i = 0; i < (int)nums.size(); i++) {
        if (used[i]) continue;
        used[i] = true; cur.push_back(nums[i]);
        backtrackPerm(nums, used, cur, res);
        cur.pop_back(); used[i] = false;
    }
}
vector<vector<int>> permutations(vector<int>& nums) {
    vector<vector<int>> res; vector<int> cur; vector<bool> used(nums.size(), false);
    backtrackPerm(nums, used, cur, res);
    return res;
}

// Combination sum — candidates can be reused — O(n^(t/min))
void backtrackComb(vector<int>& cands, int remain, int start, vector<int>& cur, vector<vector<int>>& res) {
    if (remain == 0) { res.push_back(cur); return; }
    for (int i = start; i < (int)cands.size(); i++) {
        if (cands[i] > remain) break;
        cur.push_back(cands[i]);
        backtrackComb(cands, remain - cands[i], i, cur, res); // i not i+1 (reuse)
        cur.pop_back();
    }
}
vector<vector<int>> combinationSum(vector<int> candidates, int target) {
    sort(candidates.begin(), candidates.end());
    vector<vector<int>> res; vector<int> cur;
    backtrackComb(candidates, target, 0, cur, res);
    return res;
}

// Word search on grid — O(m*n*4^L)
bool dfsWord(vector<vector<char>>& board, const string& word, int i, int j, int idx) {
    if (idx == (int)word.size()) return true;
    if (i < 0 || i >= (int)board.size() || j < 0 || j >= (int)board[0].size()) return false;
    if (board[i][j] != word[idx]) return false;
    char tmp = board[i][j]; board[i][j] = '#';
    bool found = dfsWord(board, word, i+1, j, idx+1) || dfsWord(board, word, i-1, j, idx+1)
              || dfsWord(board, word, i, j+1, idx+1) || dfsWord(board, word, i, j-1, idx+1);
    board[i][j] = tmp;
    return found;
}
bool wordSearch(vector<vector<char>>& board, const string& word) {
    for (int i = 0; i < (int)board.size(); i++)
        for (int j = 0; j < (int)board[0].size(); j++)
            if (dfsWord(board, word, i, j, 0)) return true;
    return false;
}

// N-Queens — O(n!)
bool isValidQueen(vector<string>& board, int row, int col) {
    int n = board.size();
    for (int i = 0; i < row; i++) if (board[i][col] == 'Q') return false;
    for (int i = row-1, j = col-1; i >= 0 && j >= 0; i--, j--) if (board[i][j] == 'Q') return false;
    for (int i = row-1, j = col+1; i >= 0 && j < n; i--, j++) if (board[i][j] == 'Q') return false;
    return true;
}
void backtrackQueens(vector<string>& board, int row, vector<vector<string>>& res) {
    if (row == (int)board.size()) { res.push_back(board); return; }
    for (int col = 0; col < (int)board.size(); col++) {
        if (isValidQueen(board, row, col)) {
            board[row][col] = 'Q';
            backtrackQueens(board, row + 1, res);
            board[row][col] = '.';
        }
    }
}
vector<vector<string>> nQueens(int n) {
    vector<vector<string>> res;
    vector<string> board(n, string(n, '.'));
    backtrackQueens(board, 0, res);
    return res;
}

// ─────────────────────────────────────────────────────────────────────────
// 9. Dynamic Programming
//    Identify: overlapping subproblems + optimal substructure
//    Bottom-up: fill dp table; Top-down: memoized recursion
// ─────────────────────────────────────────────────────────────────────────
// Fibonacci — O(n) time, O(1) space
int fibonacci(int n) {
    if (n <= 1) return n;
    int a = 0, b = 1;
    for (int i = 2; i <= n; i++) { int c = a + b; a = b; b = c; }
    return b;
}

// Coin change — min coins for amount — O(amount * coins)
int coinChange(vector<int>& coins, int amount) {
    vector<int> dp(amount + 1, amount + 1);
    dp[0] = 0;
    for (int i = 1; i <= amount; i++)
        for (int coin : coins)
            if (coin <= i) dp[i] = min(dp[i], dp[i - coin] + 1);
    return dp[amount] > amount ? -1 : dp[amount];
}

// Longest common subsequence — O(m*n)
int lcs(const string& s1, const string& s2) {
    int m = s1.size(), n = s2.size();
    vector<vector<int>> dp(m + 1, vector<int>(n + 1, 0));
    for (int i = 1; i <= m; i++)
        for (int j = 1; j <= n; j++)
            dp[i][j] = s1[i-1] == s2[j-1] ? dp[i-1][j-1] + 1 : max(dp[i-1][j], dp[i][j-1]);
    return dp[m][n];
}

// Longest increasing subsequence — O(n log n) with patience sorting
int lis(vector<int>& nums) {
    vector<int> tails;
    for (int n : nums) {
        auto it = lower_bound(tails.begin(), tails.end(), n);
        if (it == tails.end()) tails.push_back(n);
        else *it = n;
    }
    return tails.size();
}

// 0/1 Knapsack — O(n * capacity)
int knapsack(vector<int>& weights, vector<int>& values, int capacity) {
    int n = weights.size();
    vector<vector<int>> dp(n + 1, vector<int>(capacity + 1, 0));
    for (int i = 1; i <= n; i++)
        for (int w = 0; w <= capacity; w++) {
            dp[i][w] = dp[i-1][w];
            if (weights[i-1] <= w)
                dp[i][w] = max(dp[i][w], dp[i-1][w - weights[i-1]] + values[i-1]);
        }
    return dp[n][capacity];
}

// Word break — O(n²)
bool wordBreak(const string& s, vector<string>& dict) {
    unordered_set<string> st(dict.begin(), dict.end());
    int n = s.size();
    vector<bool> dp(n + 1, false);
    dp[0] = true;
    for (int i = 1; i <= n; i++)
        for (int j = 0; j < i; j++)
            if (dp[j] && st.count(s.substr(j, i - j))) { dp[i] = true; break; }
    return dp[n];
}

// House robber — max non-adjacent sum — O(n)
int houseRobber(vector<int>& nums) {
    int prev2 = 0, prev1 = 0;
    for (int n : nums) { int cur = max(prev1, prev2 + n); prev2 = prev1; prev1 = cur; }
    return prev1;
}

// Edit distance — O(m*n)
int editDistance(const string& s, const string& t) {
    int m = s.size(), n = t.size();
    vector<vector<int>> dp(m + 1, vector<int>(n + 1));
    for (int i = 0; i <= m; i++) dp[i][0] = i;
    for (int j = 0; j <= n; j++) dp[0][j] = j;
    for (int i = 1; i <= m; i++)
        for (int j = 1; j <= n; j++)
            dp[i][j] = s[i-1] == t[j-1] ? dp[i-1][j-1]
                : 1 + min({dp[i-1][j-1], dp[i-1][j], dp[i][j-1]});
    return dp[m][n];
}

// Unique paths — O(m*n)
int uniquePaths(int m, int n) {
    vector<int> dp(n, 1);
    for (int i = 1; i < m; i++) for (int j = 1; j < n; j++) dp[j] += dp[j-1];
    return dp[n-1];
}

// Max product subarray — O(n)
int maxProduct(vector<int>& nums) {
    int mx = nums[0], mn = nums[0], res = nums[0];
    for (int i = 1; i < (int)nums.size(); i++) {
        if (nums[i] < 0) swap(mx, mn);
        mx = max(nums[i], mx * nums[i]);
        mn = min(nums[i], mn * nums[i]);
        res = max(res, mx);
    }
    return res;
}

// ─────────────────────────────────────────────────────────────────────────
// 10. Prefix Sum / Difference Array
//     Pattern: precompute cumulative sums for O(1) range queries
// ─────────────────────────────────────────────────────────────────────────
// Subarray sum equals k — O(n)
int subarraySum(vector<int>& nums, int k) {
    unordered_map<int,int> prefixCount;
    prefixCount[0] = 1;
    int sum = 0, count = 0;
    for (int n : nums) {
        sum += n;
        count += prefixCount[sum - k];
        prefixCount[sum]++;
    }
    return count;
}

// ─────────────────────────────────────────────────────────────────────────
// 11. Monotonic Stack
//     Pattern: maintain stack in sorted order; pop when invariant broken
// ─────────────────────────────────────────────────────────────────────────
// Next greater element — O(n)
vector<int> nextGreater(vector<int>& nums) {
    int n = nums.size();
    vector<int> res(n, -1);
    stack<int> st; // stores indices
    for (int i = 0; i < n; i++) {
        while (!st.empty() && nums[st.top()] < nums[i]) {
            res[st.top()] = nums[i]; st.pop();
        }
        st.push(i);
    }
    return res;
}

// Largest rectangle in histogram — O(n)
int largestRectangle(vector<int>& heights) {
    stack<int> st;
    int mx = 0;
    for (int i = 0; i <= (int)heights.size(); i++) {
        int h = (i == (int)heights.size()) ? 0 : heights[i];
        while (!st.empty() && heights[st.top()] > h) {
            int height = heights[st.top()]; st.pop();
            int width = st.empty() ? i : i - st.top() - 1;
            mx = max(mx, height * width);
        }
        st.push(i);
    }
    return mx;
}

// ─────────────────────────────────────────────────────────────────────────
// Demo
// ─────────────────────────────────────────────────────────────────────────
int main() {
    // Sliding window
    vector<int> a1 = {2,1,5,1,3,2};
    cout << "Max sum k=3: " << maxSumFixed(a1, 3) << "\n"; // 9
    cout << "Longest no repeat: " << longestNoRepeat("abcabcbb") << "\n"; // 3
    cout << "Min window: " << minWindow("ADOBECODEBANC", "ABC") << "\n"; // BANC

    // Two pointers
    vector<int> a2 = {2,7,11,15};
    auto ts = twoSum(a2, 9);
    cout << "Two sum: [" << ts[0] << "," << ts[1] << "]\n"; // [0,1]
    vector<int> a3 = {-1,0,1,2,-1,-4};
    auto trip = threeSum(a3);
    cout << "3Sum count: " << trip.size() << "\n"; // 2
    vector<int> a4 = {1,8,6,2,5,4,8,3,7};
    cout << "Max water: " << maxWater(a4) << "\n"; // 49
    vector<int> a5 = {0,1,0,2,1,0,1,3,2,1,2,1};
    cout << "Trap water: " << trapWater(a5) << "\n"; // 6

    // Fast/slow
    ListNode* head = new ListNode(1);
    head->next = new ListNode(2); head->next->next = new ListNode(3);
    cout << "Has cycle: " << hasCycle(head) << "\n"; // 0
    cout << "Middle: " << middle(head)->val << "\n"; // 2

    // Binary search
    vector<int> a6 = {1,3,5,7,9};
    cout << "Binary search: " << binarySearch(a6, 7) << "\n"; // 3
    vector<int> a7 = {1,2,2,2,3};
    cout << "First occurrence: " << firstOccurrence(a7, 2) << "\n"; // 1
    vector<int> a8 = {4,5,6,7,0,1,2};
    cout << "Search rotated: " << searchRotated(a8, 0) << "\n"; // 4
    vector<int> a9 = {3,4,5,1,2};
    cout << "Min rotated: " << minRotated(a9) << "\n"; // 1

    // Merge intervals
    vector<vector<int>> ivs = {{1,3},{2,6},{8,10},{15,18}};
    auto merged = mergeIntervals(ivs);
    cout << "Merge: "; for (auto& v : merged) cout << "[" << v[0] << "," << v[1] << "] "; cout << "\n";
    vector<vector<int>> rooms = {{0,30},{5,10},{15,20}};
    cout << "Meeting rooms: " << minMeetingRooms(rooms) << "\n"; // 2

    // Heap
    vector<int> a10 = {3,2,1,5,6,4};
    auto kl = kLargest(a10, 2);
    cout << "K largest: "; for (int x : kl) cout << x << " "; cout << "\n";
    cout << "Kth largest: " << kthLargest(a10, 2) << "\n"; // 5
    vector<int> a11 = {1,1,1,2,2,3};
    auto tf = topKFrequent(a11, 2);
    cout << "Top K frequent: "; for (int x : tf) cout << x << " "; cout << "\n";

    // Backtracking
    vector<int> a12 = {1,2,3};
    cout << "Subsets count: " << subsets(a12).size() << "\n"; // 8
    cout << "Permutations count: " << permutations(a12).size() << "\n"; // 6
    vector<int> cands = {2,3,6,7};
    cout << "Combination sum count: " << combinationSum(cands, 7).size() << "\n"; // 2
    cout << "N-Queens(4): " << nQueens(4).size() << " solutions\n"; // 2
    vector<vector<char>> board = {{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
    cout << "Word search: " << wordSearch(board, "ABCCED") << "\n"; // 1

    // DP
    cout << "Fib(10): " << fibonacci(10) << "\n"; // 55
    vector<int> coins = {1,5,11};
    cout << "Coin change: " << coinChange(coins, 15) << "\n"; // 3
    cout << "LCS: " << lcs("abcde", "ace") << "\n"; // 3
    vector<int> lisArr = {10,9,2,5,3,7,101,18};
    cout << "LIS: " << lis(lisArr) << "\n"; // 4
    vector<int> wts = {2,3,4,5}, vals = {3,4,5,6};
    cout << "Knapsack: " << knapsack(wts, vals, 5) << "\n"; // 7
    vector<string> dict = {"leet","code"};
    cout << "Word break: " << wordBreak("leetcode", dict) << "\n"; // 1
    vector<int> houses = {2,7,9,3,1};
    cout << "House robber: " << houseRobber(houses) << "\n"; // 12
    cout << "Edit distance: " << editDistance("horse", "ros") << "\n"; // 3
    cout << "Unique paths: " << uniquePaths(3, 7) << "\n"; // 28
    vector<int> prod = {2,3,-2,4};
    cout << "Max product: " << maxProduct(prod) << "\n"; // 6

    // Prefix sum
    vector<int> psum = {1,1,1};
    cout << "Subarray sum=2: " << subarraySum(psum, 2) << "\n"; // 2

    // Monotonic stack
    vector<int> ng = {2,1,2,4,3};
    auto ngr = nextGreater(ng);
    cout << "Next greater: "; for (int x : ngr) cout << x << " "; cout << "\n"; // 4 2 4 -1 -1
    vector<int> hist = {2,1,5,6,2,3};
    cout << "Largest rect: " << largestRectangle(hist) << "\n"; // 10

    return 0;
}
