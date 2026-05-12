import java.util.*;
import java.util.stream.*;

public class Algorithms {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Sliding Window — Fixed Size
    //    Pattern: maintain a window of size k; add right, remove left
    // ─────────────────────────────────────────────────────────────────────────
    // Max sum subarray of size k — O(n)
    static int maxSumFixed(int[] arr, int k) {
        int sum = 0;
        for (int i = 0; i < k; i++) sum += arr[i];
        int max = sum;
        for (int i = k; i < arr.length; i++) {
            sum += arr[i] - arr[i - k];
            max = Math.max(max, sum);
        }
        return max;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Sliding Window — Variable Size
    //    Pattern: expand right until invalid, shrink left until valid again
    // ─────────────────────────────────────────────────────────────────────────
    // Longest substring without repeating characters — O(n)
    static int longestNoRepeat(String s) {
        Map<Character, Integer> last = new HashMap<>();
        int max = 0, left = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (last.containsKey(c) && last.get(c) >= left)
                left = last.get(c) + 1;
            last.put(c, right);
            max = Math.max(max, right - left + 1);
        }
        return max;
    }

    // Minimum window containing all chars of t — O(n)
    static String minWindow(String s, String t) {
        Map<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) need.merge(c, 1, Integer::sum);
        int have = 0, required = need.size(), left = 0, minLen = Integer.MAX_VALUE, minL = 0;
        Map<Character, Integer> window = new HashMap<>();
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            window.merge(c, 1, Integer::sum);
            if (need.containsKey(c) && window.get(c).equals(need.get(c))) have++;
            while (have == required) {
                if (right - left + 1 < minLen) { minLen = right - left + 1; minL = left; }
                char lc = s.charAt(left++);
                window.merge(lc, -1, Integer::sum);
                if (need.containsKey(lc) && window.get(lc) < need.get(lc)) have--;
            }
        }
        return minLen == Integer.MAX_VALUE ? "" : s.substring(minL, minL + minLen);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Two Pointers
    //    Pattern: left/right from both ends; move based on comparison
    // ─────────────────────────────────────────────────────────────────────────
    // Two sum on sorted array — O(n)
    static int[] twoSum(int[] arr, int target) {
        int l = 0, r = arr.length - 1;
        while (l < r) {
            int sum = arr[l] + arr[r];
            if (sum == target) return new int[]{l, r};
            else if (sum < target) l++;
            else r--;
        }
        return new int[]{};
    }

    // 3Sum — all unique triplets summing to 0 — O(n²)
    static List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int sum = nums[i] + nums[l] + nums[r];
                if (sum == 0) {
                    res.add(Arrays.asList(nums[i], nums[l], nums[r]));
                    while (l < r && nums[l] == nums[l + 1]) l++;
                    while (l < r && nums[r] == nums[r - 1]) r--;
                    l++; r--;
                } else if (sum < 0) l++;
                else r--;
            }
        }
        return res;
    }

    // Container with most water — O(n)
    static int maxWater(int[] height) {
        int l = 0, r = height.length - 1, max = 0;
        while (l < r) {
            max = Math.max(max, Math.min(height[l], height[r]) * (r - l));
            if (height[l] < height[r]) l++; else r--;
        }
        return max;
    }

    // Trapping rain water — O(n)
    static int trapWater(int[] height) {
        int l = 0, r = height.length - 1, maxL = 0, maxR = 0, water = 0;
        while (l < r) {
            if (height[l] < height[r]) {
                maxL = Math.max(maxL, height[l]);
                water += maxL - height[l++];
            } else {
                maxR = Math.max(maxR, height[r]);
                water += maxR - height[r--];
            }
        }
        return water;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Fast & Slow Pointers
    //    Pattern: slow moves 1 step, fast moves 2; meet at cycle or middle
    // ─────────────────────────────────────────────────────────────────────────
    static class ListNode {
        int val; ListNode next;
        ListNode(int v) { val = v; }
    }

    // Detect cycle — O(n)
    static boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next; fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }

    // Find middle — O(n)  [even length: returns second middle]
    static ListNode middle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        return slow;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Binary Search
    //    Key: use l + (r-l)/2 to avoid overflow; decide l=mid+1 or r=mid-1
    // ─────────────────────────────────────────────────────────────────────────
    // Standard — O(log n)
    static int binarySearch(int[] arr, int target) {
        int l = 0, r = arr.length - 1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (arr[mid] == target) return mid;
            else if (arr[mid] < target) l = mid + 1;
            else r = mid - 1;
        }
        return -1;
    }

    // First occurrence — O(log n)
    static int firstOccurrence(int[] arr, int target) {
        int l = 0, r = arr.length - 1, res = -1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (arr[mid] == target) { res = mid; r = mid - 1; } // keep searching left
            else if (arr[mid] < target) l = mid + 1;
            else r = mid - 1;
        }
        return res;
    }

    // Search in rotated sorted array — O(log n)
    static int searchRotated(int[] arr, int target) {
        int l = 0, r = arr.length - 1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (arr[mid] == target) return mid;
            if (arr[l] <= arr[mid]) { // left half sorted
                if (target >= arr[l] && target < arr[mid]) r = mid - 1;
                else l = mid + 1;
            } else { // right half sorted
                if (target > arr[mid] && target <= arr[r]) l = mid + 1;
                else r = mid - 1;
            }
        }
        return -1;
    }

    // Minimum in rotated sorted array — O(log n)
    static int minRotated(int[] arr) {
        int l = 0, r = arr.length - 1;
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
    static int[][] mergeIntervals(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        List<int[]> res = new ArrayList<>();
        res.add(intervals[0]);
        for (int i = 1; i < intervals.length; i++) {
            int[] last = res.get(res.size() - 1);
            if (intervals[i][0] <= last[1]) last[1] = Math.max(last[1], intervals[i][1]);
            else res.add(intervals[i]);
        }
        return res.toArray(new int[0][]);
    }

    // Insert and merge a new interval — O(n)
    static int[][] insertInterval(int[][] intervals, int[] newInterval) {
        List<int[]> res = new ArrayList<>();
        int i = 0, n = intervals.length;
        while (i < n && intervals[i][1] < newInterval[0]) res.add(intervals[i++]);
        while (i < n && intervals[i][0] <= newInterval[1]) {
            newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
            newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
            i++;
        }
        res.add(newInterval);
        while (i < n) res.add(intervals[i++]);
        return res.toArray(new int[0][]);
    }

    // Meeting rooms II — min rooms needed — O(n log n)
    static int minMeetingRooms(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        PriorityQueue<Integer> ends = new PriorityQueue<>(); // tracks end times
        for (int[] iv : intervals) {
            if (!ends.isEmpty() && ends.peek() <= iv[0]) ends.poll(); // reuse room
            ends.offer(iv[1]);
        }
        return ends.size();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Top K Elements (Heap)
    //    Min-heap of size k → keeps k largest seen so far
    // ─────────────────────────────────────────────────────────────────────────
    // K largest — O(n log k)
    static int[] kLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int n : nums) {
            minHeap.offer(n);
            if (minHeap.size() > k) minHeap.poll();
        }
        return minHeap.stream().mapToInt(x -> x).toArray();
    }

    // K most frequent — O(n log k)
    static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);
        PriorityQueue<int[]> minHeap = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        for (var e : freq.entrySet()) {
            minHeap.offer(new int[]{e.getKey(), e.getValue()});
            if (minHeap.size() > k) minHeap.poll();
        }
        return minHeap.stream().mapToInt(x -> x[0]).toArray();
    }

    // Kth largest element — O(n log k)
    static int kthLargest(int[] nums, int k) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int n : nums) {
            minHeap.offer(n);
            if (minHeap.size() > k) minHeap.poll();
        }
        return minHeap.peek();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Backtracking
    //    Pattern: choose → recurse → unchoose (state restoration)
    //    Prune early: sort candidates, break when candidate > remaining
    // ─────────────────────────────────────────────────────────────────────────
    // All subsets — O(2^n)
    static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        backtrackSubsets(nums, 0, new ArrayList<>(), res);
        return res;
    }
    static void backtrackSubsets(int[] nums, int start, List<Integer> cur, List<List<Integer>> res) {
        res.add(new ArrayList<>(cur));
        for (int i = start; i < nums.length; i++) {
            cur.add(nums[i]);
            backtrackSubsets(nums, i + 1, cur, res);
            cur.remove(cur.size() - 1);
        }
    }

    // All permutations — O(n!)
    static List<List<Integer>> permutations(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        backtrackPerm(nums, new boolean[nums.length], new ArrayList<>(), res);
        return res;
    }
    static void backtrackPerm(int[] nums, boolean[] used, List<Integer> cur, List<List<Integer>> res) {
        if (cur.size() == nums.length) { res.add(new ArrayList<>(cur)); return; }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true; cur.add(nums[i]);
            backtrackPerm(nums, used, cur, res);
            cur.remove(cur.size() - 1); used[i] = false;
        }
    }

    // Combination sum — candidates can be reused — O(n^(t/min))
    static List<List<Integer>> combinationSum(int[] candidates, int target) {
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(candidates);
        backtrackComb(candidates, target, 0, new ArrayList<>(), res);
        return res;
    }
    static void backtrackComb(int[] cands, int remain, int start, List<Integer> cur, List<List<Integer>> res) {
        if (remain == 0) { res.add(new ArrayList<>(cur)); return; }
        for (int i = start; i < cands.length; i++) {
            if (cands[i] > remain) break;
            cur.add(cands[i]);
            backtrackComb(cands, remain - cands[i], i, cur, res); // i not i+1 (reuse)
            cur.remove(cur.size() - 1);
        }
    }

    // Word search on grid — O(m*n*4^L)
    static boolean wordSearch(char[][] board, String word) {
        int m = board.length, n = board[0].length;
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (dfsWord(board, word, i, j, 0)) return true;
        return false;
    }
    static boolean dfsWord(char[][] board, String word, int i, int j, int idx) {
        if (idx == word.length()) return true;
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length) return false;
        if (board[i][j] != word.charAt(idx)) return false;
        char tmp = board[i][j]; board[i][j] = '#'; // mark visited
        boolean found = dfsWord(board, word, i+1, j, idx+1) || dfsWord(board, word, i-1, j, idx+1)
                     || dfsWord(board, word, i, j+1, idx+1) || dfsWord(board, word, i, j-1, idx+1);
        board[i][j] = tmp; // restore
        return found;
    }

    // N-Queens — O(n!)
    static List<List<String>> nQueens(int n) {
        List<List<String>> res = new ArrayList<>();
        char[][] board = new char[n][n];
        for (char[] row : board) Arrays.fill(row, '.');
        backtrackQueens(board, 0, res);
        return res;
    }
    static void backtrackQueens(char[][] board, int row, List<List<String>> res) {
        if (row == board.length) {
            List<String> sol = new ArrayList<>();
            for (char[] r : board) sol.add(new String(r));
            res.add(sol); return;
        }
        for (int col = 0; col < board.length; col++) {
            if (isValidQueen(board, row, col)) {
                board[row][col] = 'Q';
                backtrackQueens(board, row + 1, res);
                board[row][col] = '.';
            }
        }
    }
    static boolean isValidQueen(char[][] board, int row, int col) {
        int n = board.length;
        for (int i = 0; i < row; i++) if (board[i][col] == 'Q') return false;
        for (int i = row-1, j = col-1; i >= 0 && j >= 0; i--, j--) if (board[i][j] == 'Q') return false;
        for (int i = row-1, j = col+1; i >= 0 && j < n; i--, j++) if (board[i][j] == 'Q') return false;
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. Dynamic Programming
    //    Identify: overlapping subproblems + optimal substructure
    //    Bottom-up: fill dp table; Top-down: memoized recursion
    // ─────────────────────────────────────────────────────────────────────────
    // Fibonacci — O(n) time, O(1) space
    static int fibonacci(int n) {
        if (n <= 1) return n;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) { int c = a + b; a = b; b = c; }
        return b;
    }

    // Coin change — min coins for amount — O(amount * coins)
    static int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int i = 1; i <= amount; i++)
            for (int coin : coins)
                if (coin <= i) dp[i] = Math.min(dp[i], dp[i - coin] + 1);
        return dp[amount] > amount ? -1 : dp[amount];
    }

    // Longest common subsequence — O(m*n)
    static int lcs(String s1, String s2) {
        int m = s1.length(), n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = s1.charAt(i-1) == s2.charAt(j-1)
                    ? dp[i-1][j-1] + 1 : Math.max(dp[i-1][j], dp[i][j-1]);
        return dp[m][n];
    }

    // Longest increasing subsequence — O(n log n) with patience sorting
    static int lis(int[] nums) {
        List<Integer> tails = new ArrayList<>();
        for (int n : nums) {
            int pos = Collections.binarySearch(tails, n);
            if (pos < 0) pos = -(pos + 1);
            if (pos == tails.size()) tails.add(n); else tails.set(pos, n);
        }
        return tails.size();
    }

    // 0/1 Knapsack — O(n * capacity)
    static int knapsack(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];
        for (int i = 1; i <= n; i++)
            for (int w = 0; w <= capacity; w++) {
                dp[i][w] = dp[i-1][w];
                if (weights[i-1] <= w) dp[i][w] = Math.max(dp[i][w], dp[i-1][w - weights[i-1]] + values[i-1]);
            }
        return dp[n][capacity];
    }

    // Word break — O(n²)
    static boolean wordBreak(String s, List<String> dict) {
        Set<String> set = new HashSet<>(dict);
        boolean[] dp = new boolean[s.length() + 1];
        dp[0] = true;
        for (int i = 1; i <= s.length(); i++)
            for (int j = 0; j < i; j++)
                if (dp[j] && set.contains(s.substring(j, i))) { dp[i] = true; break; }
        return dp[s.length()];
    }

    // House robber — max non-adjacent sum — O(n)
    static int houseRobber(int[] nums) {
        int prev2 = 0, prev1 = 0;
        for (int n : nums) { int cur = Math.max(prev1, prev2 + n); prev2 = prev1; prev1 = cur; }
        return prev1;
    }

    // Edit distance — O(m*n)
    static int editDistance(String s, String t) {
        int m = s.length(), n = t.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = s.charAt(i-1) == t.charAt(j-1) ? dp[i-1][j-1]
                    : 1 + Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1]));
        return dp[m][n];
    }

    // Unique paths — O(m*n)
    static int uniquePaths(int m, int n) {
        int[] dp = new int[n];
        Arrays.fill(dp, 1);
        for (int i = 1; i < m; i++) for (int j = 1; j < n; j++) dp[j] += dp[j - 1];
        return dp[n - 1];
    }

    // Max product subarray — O(n)
    static int maxProduct(int[] nums) {
        int max = nums[0], min = nums[0], res = nums[0];
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] < 0) { int tmp = max; max = min; min = tmp; }
            max = Math.max(nums[i], max * nums[i]);
            min = Math.min(nums[i], min * nums[i]);
            res = Math.max(res, max);
        }
        return res;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. Prefix Sum / Difference Array
    //     Pattern: precompute cumulative sums for O(1) range queries
    // ─────────────────────────────────────────────────────────────────────────
    // Subarray sum equals k — O(n)
    static int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> prefixCount = new HashMap<>();
        prefixCount.put(0, 1);
        int sum = 0, count = 0;
        for (int n : nums) {
            sum += n;
            count += prefixCount.getOrDefault(sum - k, 0);
            prefixCount.merge(sum, 1, Integer::sum);
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 11. Monotonic Stack
    //     Pattern: maintain stack in sorted order; pop when invariant broken
    // ─────────────────────────────────────────────────────────────────────────
    // Next greater element — O(n)
    static int[] nextGreater(int[] nums) {
        int n = nums.length;
        int[] res = new int[n];
        Arrays.fill(res, -1);
        Deque<Integer> stack = new ArrayDeque<>(); // stores indices
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && nums[stack.peek()] < nums[i])
                res[stack.pop()] = nums[i];
            stack.push(i);
        }
        return res;
    }

    // Largest rectangle in histogram — O(n)
    static int largestRectangle(int[] heights) {
        Deque<Integer> stack = new ArrayDeque<>();
        int max = 0;
        for (int i = 0; i <= heights.length; i++) {
            int h = i == heights.length ? 0 : heights[i];
            while (!stack.isEmpty() && heights[stack.peek()] > h) {
                int height = heights[stack.pop()];
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                max = Math.max(max, height * width);
            }
            stack.push(i);
        }
        return max;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Demo
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Sliding window
        System.out.println("Max sum k=3: " + maxSumFixed(new int[]{2,1,5,1,3,2}, 3)); // 9
        System.out.println("Longest no repeat: " + longestNoRepeat("abcabcbb")); // 3
        System.out.println("Min window: " + minWindow("ADOBECODEBANC", "ABC")); // BANC

        // Two pointers
        System.out.println("Two sum: " + Arrays.toString(twoSum(new int[]{2,7,11,15}, 9))); // [0,1]
        System.out.println("3Sum: " + threeSum(new int[]{-1,0,1,2,-1,-4}));
        System.out.println("Max water: " + maxWater(new int[]{1,8,6,2,5,4,8,3,7})); // 49
        System.out.println("Trap water: " + trapWater(new int[]{0,1,0,2,1,0,1,3,2,1,2,1})); // 6

        // Fast/slow
        ListNode head = new ListNode(1);
        head.next = new ListNode(2); head.next.next = new ListNode(3);
        System.out.println("Has cycle: " + hasCycle(head)); // false
        System.out.println("Middle: " + middle(head).val); // 2

        // Binary search
        System.out.println("Binary search: " + binarySearch(new int[]{1,3,5,7,9}, 7)); // 3
        System.out.println("First occurrence: " + firstOccurrence(new int[]{1,2,2,2,3}, 2)); // 1
        System.out.println("Search rotated: " + searchRotated(new int[]{4,5,6,7,0,1,2}, 0)); // 4
        System.out.println("Min rotated: " + minRotated(new int[]{3,4,5,1,2})); // 1

        // Merge intervals
        System.out.println("Merge: " + Arrays.deepToString(
            mergeIntervals(new int[][]{{1,3},{2,6},{8,10},{15,18}}))); // [[1,6],[8,10],[15,18]]
        System.out.println("Meeting rooms: " + minMeetingRooms(new int[][]{{0,30},{5,10},{15,20}})); // 2

        // Heap
        System.out.println("K largest: " + Arrays.toString(kLargest(new int[]{3,2,1,5,6,4}, 2)));
        System.out.println("Kth largest: " + kthLargest(new int[]{3,2,1,5,6,4}, 2)); // 5
        System.out.println("Top K frequent: " + Arrays.toString(topKFrequent(new int[]{1,1,1,2,2,3}, 2)));

        // Backtracking
        System.out.println("Subsets: " + subsets(new int[]{1,2,3}));
        System.out.println("Permutations count: " + permutations(new int[]{1,2,3}).size()); // 6
        System.out.println("Combination sum: " + combinationSum(new int[]{2,3,6,7}, 7));
        System.out.println("N-Queens(4): " + nQueens(4).size() + " solutions"); // 2
        System.out.println("Word search: " + wordSearch(
            new char[][]{{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}}, "ABCCED")); // true

        // DP
        System.out.println("Fib(10): " + fibonacci(10)); // 55
        System.out.println("Coin change: " + coinChange(new int[]{1,5,11}, 15)); // 3
        System.out.println("LCS: " + lcs("abcde", "ace")); // 3
        System.out.println("LIS: " + lis(new int[]{10,9,2,5,3,7,101,18})); // 4
        System.out.println("Knapsack: " + knapsack(new int[]{2,3,4,5}, new int[]{3,4,5,6}, 5)); // 7
        System.out.println("Word break: " + wordBreak("leetcode", Arrays.asList("leet","code"))); // true
        System.out.println("House robber: " + houseRobber(new int[]{2,7,9,3,1})); // 12
        System.out.println("Edit distance: " + editDistance("horse", "ros")); // 3
        System.out.println("Unique paths: " + uniquePaths(3, 7)); // 28
        System.out.println("Max product: " + maxProduct(new int[]{2,3,-2,4})); // 6

        // Prefix sum
        System.out.println("Subarray sum=2: " + subarraySum(new int[]{1,1,1}, 2)); // 2

        // Monotonic stack
        System.out.println("Next greater: " + Arrays.toString(nextGreater(new int[]{2,1,2,4,3}))); // [4,2,4,-1,-1]
        System.out.println("Largest rect: " + largestRectangle(new int[]{2,1,5,6,2,3})); // 10
    }
}
