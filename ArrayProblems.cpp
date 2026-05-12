#include <bits/stdc++.h>
using namespace std;

// ---------------------------------------------------------------------------
// Array Problems Reference - C++
// Covers: two pointers | sliding window | prefix sums | Kadane | binary search |
//         intervals | matrix traversal | cyclic sort | sorting tricks
// ---------------------------------------------------------------------------

void printVec(const vector<int>& v) {
    for (int x : v) cout << x << " ";
    cout << endl;
}

void printMatrix(const vector<vector<int>>& m) {
    for (auto& row : m) printVec(row);
}


// ---------------------------------------------------------------------------
// 1. Two Sum
//    PROBLEM: Given an array and target, return indexes of two numbers whose
//             sum equals the target.
//    MENTAL MODEL: "for each number, ask if its complement was seen before"
// ---------------------------------------------------------------------------
vector<int> twoSum(vector<int>& nums, int target) {
    unordered_map<int, int> seen; // value -> index
    for (int i = 0; i < (int)nums.size(); i++) {
        int need = target - nums[i];
        if (seen.count(need)) return {seen[need], i};
        seen[nums[i]] = i;
    }
    return {};
}


// ---------------------------------------------------------------------------
// 2. Two Pointers on Sorted Array
//    PROBLEM: Given a sorted array and target, return indexes of two numbers
//             whose sum equals the target.
//    MENTAL MODEL: "small sum moves left pointer right; large sum moves right left"
// ---------------------------------------------------------------------------
vector<int> twoSumSorted(vector<int>& nums, int target) {
    int l = 0, r = nums.size() - 1;
    while (l < r) {
        int sum = nums[l] + nums[r];
        if (sum == target) return {l, r};
        if (sum < target) l++;
        else r--;
    }
    return {};
}


// ---------------------------------------------------------------------------
// 3. 3Sum
//    PROBLEM: Return all unique triplets whose sum is zero.
//    MENTAL MODEL: "sort, fix one number, solve two-sum with two pointers"
// ---------------------------------------------------------------------------
vector<vector<int>> threeSum(vector<int>& nums) {
    sort(nums.begin(), nums.end());
    vector<vector<int>> res;

    for (int i = 0; i < (int)nums.size(); i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;

        int l = i + 1, r = nums.size() - 1;
        while (l < r) {
            int sum = nums[i] + nums[l] + nums[r];
            if (sum == 0) {
                res.push_back({nums[i], nums[l], nums[r]});
                while (l < r && nums[l] == nums[l + 1]) l++;
                while (l < r && nums[r] == nums[r - 1]) r--;
                l++;
                r--;
            } else if (sum < 0) {
                l++;
            } else {
                r--;
            }
        }
    }
    return res;
}


// ---------------------------------------------------------------------------
// 4. Container With Most Water
//    PROBLEM: Given vertical line heights, return the maximum water container
//             area formed by choosing two lines.
//    MENTAL MODEL: "area limited by shorter wall; move the shorter wall inward"
// ---------------------------------------------------------------------------
int maxArea(vector<int>& height) {
    int l = 0, r = height.size() - 1;
    int best = 0;

    while (l < r) {
        best = max(best, min(height[l], height[r]) * (r - l));
        if (height[l] < height[r]) l++;
        else r--;
    }
    return best;
}


// ---------------------------------------------------------------------------
// 5. Trapping Rain Water
//    PROBLEM: Given bar heights, compute total rain water trapped between bars.
//    MENTAL MODEL: "water depends on smaller side's best wall"
// ---------------------------------------------------------------------------
int trap(vector<int>& height) {
    int l = 0, r = height.size() - 1;
    int leftMax = 0, rightMax = 0, water = 0;

    while (l < r) {
        if (height[l] < height[r]) {
            leftMax = max(leftMax, height[l]);
            water += leftMax - height[l];
            l++;
        } else {
            rightMax = max(rightMax, height[r]);
            water += rightMax - height[r];
            r--;
        }
    }
    return water;
}


// ---------------------------------------------------------------------------
// 6. Sliding Window: Longest Subarray With At Most K Distinct
//    PROBLEM: Return the longest contiguous subarray containing at most k
//             distinct values.
//    MENTAL MODEL: "expand right; shrink left until window is valid again"
// ---------------------------------------------------------------------------
int longestAtMostKDistinct(vector<int>& nums, int k) {
    unordered_map<int, int> freq;
    int l = 0, best = 0;

    for (int r = 0; r < (int)nums.size(); r++) {
        freq[nums[r]]++;
        while ((int)freq.size() > k) {
            if (--freq[nums[l]] == 0) freq.erase(nums[l]);
            l++;
        }
        best = max(best, r - l + 1);
    }
    return best;
}


// ---------------------------------------------------------------------------
// 7. Minimum Size Subarray Sum
//    PROBLEM: Given positive integers, find the minimum length subarray with
//             sum at least target.
//    MENTAL MODEL: "positive numbers let us shrink while sum is enough"
// ---------------------------------------------------------------------------
int minSubArrayLen(int target, vector<int>& nums) {
    int l = 0, sum = 0, best = INT_MAX;

    for (int r = 0; r < (int)nums.size(); r++) {
        sum += nums[r];
        while (sum >= target) {
            best = min(best, r - l + 1);
            sum -= nums[l++];
        }
    }
    return best == INT_MAX ? 0 : best;
}


// ---------------------------------------------------------------------------
// 8. Prefix Sum: Subarray Sum Equals K
//    PROBLEM: Count contiguous subarrays whose sum equals k.
//    MENTAL MODEL: "currentPrefix - oldPrefix = k"
// ---------------------------------------------------------------------------
int subarraySum(vector<int>& nums, int k) {
    unordered_map<int, int> count;
    count[0] = 1;
    int prefix = 0, ans = 0;

    for (int x : nums) {
        prefix += x;
        ans += count[prefix - k];
        count[prefix]++;
    }
    return ans;
}


// ---------------------------------------------------------------------------
// 9. Product of Array Except Self
//    PROBLEM: Return an array where each index contains the product of all
//             other elements, without using division.
//    MENTAL MODEL: "answer = product of everything left * everything right"
// ---------------------------------------------------------------------------
vector<int> productExceptSelf(vector<int>& nums) {
    int n = nums.size();
    vector<int> res(n, 1);

    int left = 1;
    for (int i = 0; i < n; i++) {
        res[i] = left;
        left *= nums[i];
    }

    int right = 1;
    for (int i = n - 1; i >= 0; i--) {
        res[i] *= right;
        right *= nums[i];
    }
    return res;
}


// ---------------------------------------------------------------------------
// 10. Kadane's Algorithm: Maximum Subarray
//     PROBLEM: Find the maximum possible sum of any non-empty contiguous subarray.
//     MENTAL MODEL: "extend if helpful; otherwise restart at current number"
// ---------------------------------------------------------------------------
int maxSubArray(vector<int>& nums) {
    int cur = nums[0], best = nums[0];
    for (int i = 1; i < (int)nums.size(); i++) {
        cur = max(nums[i], cur + nums[i]);
        best = max(best, cur);
    }
    return best;
}


// ---------------------------------------------------------------------------
// 11. Maximum Product Subarray
//     PROBLEM: Find the maximum possible product of any non-empty contiguous
//              subarray.
//     MENTAL MODEL: "negative number swaps min and max products"
// ---------------------------------------------------------------------------
int maxProduct(vector<int>& nums) {
    int hi = nums[0], lo = nums[0], best = nums[0];

    for (int i = 1; i < (int)nums.size(); i++) {
        if (nums[i] < 0) swap(hi, lo);
        hi = max(nums[i], hi * nums[i]);
        lo = min(nums[i], lo * nums[i]);
        best = max(best, hi);
    }
    return best;
}


// ---------------------------------------------------------------------------
// 12. Binary Search
//     PROBLEM: Search a sorted array for a target value, or find the first
//              position where a target could be inserted.
//     MENTAL MODEL: "keep the answer inside [l, r]"
// ---------------------------------------------------------------------------
int binarySearch(vector<int>& nums, int target) {
    int l = 0, r = nums.size() - 1;
    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] == target) return mid;
        if (nums[mid] < target) l = mid + 1;
        else r = mid - 1;
    }
    return -1;
}

int lowerBound(vector<int>& nums, int target) {
    int l = 0, r = nums.size();
    while (l < r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] < target) l = mid + 1;
        else r = mid;
    }
    return l;
}


// ---------------------------------------------------------------------------
// 13. Search in Rotated Sorted Array
//     PROBLEM: Search for a target in a sorted array that has been rotated.
//     MENTAL MODEL: "one half is always sorted; decide if target is inside it"
// ---------------------------------------------------------------------------
int searchRotated(vector<int>& nums, int target) {
    int l = 0, r = nums.size() - 1;

    while (l <= r) {
        int mid = l + (r - l) / 2;
        if (nums[mid] == target) return mid;

        if (nums[l] <= nums[mid]) {
            if (nums[l] <= target && target < nums[mid]) r = mid - 1;
            else l = mid + 1;
        } else {
            if (nums[mid] < target && target <= nums[r]) l = mid + 1;
            else r = mid - 1;
        }
    }
    return -1;
}


// ---------------------------------------------------------------------------
// 14. Find First and Last Position
//     PROBLEM: In a sorted array, return the first and last index of target.
//     MENTAL MODEL: "lower_bound(target) and lower_bound(target + 1) - 1"
// ---------------------------------------------------------------------------
vector<int> searchRange(vector<int>& nums, int target) {
    int first = lowerBound(nums, target);
    if (first == (int)nums.size() || nums[first] != target) return {-1, -1};

    int after = lowerBound(nums, target + 1);
    return {first, after - 1};
}


// ---------------------------------------------------------------------------
// 15. Binary Search on Answer: Ship Packages Within D Days
//     PROBLEM: Find the minimum ship capacity needed to ship all packages
//              within the given number of days.
//     MENTAL MODEL: "guess capacity; check if it works"
// ---------------------------------------------------------------------------
bool canShip(vector<int>& weights, int days, int cap) {
    int usedDays = 1, cur = 0;
    for (int w : weights) {
        if (cur + w > cap) {
            usedDays++;
            cur = 0;
        }
        cur += w;
    }
    return usedDays <= days;
}

int shipWithinDays(vector<int>& weights, int days) {
    int l = *max_element(weights.begin(), weights.end());
    int r = accumulate(weights.begin(), weights.end(), 0);

    while (l < r) {
        int mid = l + (r - l) / 2;
        if (canShip(weights, days, mid)) r = mid;
        else l = mid + 1;
    }
    return l;
}


// ---------------------------------------------------------------------------
// 16. Intervals: Merge Intervals
//     PROBLEM: Merge all overlapping intervals and return the non-overlapping
//              result.
//     MENTAL MODEL: "sort by start; merge into last interval if overlapping"
// ---------------------------------------------------------------------------
vector<vector<int>> mergeIntervals(vector<vector<int>>& intervals) {
    sort(intervals.begin(), intervals.end());
    vector<vector<int>> res;

    for (auto& in : intervals) {
        if (res.empty() || res.back()[1] < in[0]) {
            res.push_back(in);
        } else {
            res.back()[1] = max(res.back()[1], in[1]);
        }
    }
    return res;
}


// ---------------------------------------------------------------------------
// 17. Intervals: Insert Interval
//     PROBLEM: Insert a new interval into a sorted non-overlapping interval list
//              and merge overlaps.
//     MENTAL MODEL: "copy before, merge overlaps, copy after"
// ---------------------------------------------------------------------------
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


// ---------------------------------------------------------------------------
// 18. Sort Colors / Dutch National Flag
//     PROBLEM: Sort an array containing only 0, 1, and 2 in-place.
//     MENTAL MODEL: "three zones: 0s | unknown | 2s"
// ---------------------------------------------------------------------------
void sortColors(vector<int>& nums) {
    int low = 0, mid = 0, high = nums.size() - 1;

    while (mid <= high) {
        if (nums[mid] == 0) swap(nums[low++], nums[mid++]);
        else if (nums[mid] == 1) mid++;
        else swap(nums[mid], nums[high--]);
    }
}


// ---------------------------------------------------------------------------
// 19. Cyclic Sort: Missing Number
//     PROBLEM: Given n distinct numbers from range [0, n], find the missing one.
//     MENTAL MODEL: "value x belongs at index x when x is in range"
// ---------------------------------------------------------------------------
int missingNumber(vector<int>& nums) {
    int n = nums.size();
    for (int i = 0; i < n; i++) {
        while (nums[i] < n && nums[i] != nums[nums[i]]) {
            swap(nums[i], nums[nums[i]]);
        }
    }

    for (int i = 0; i < n; i++) {
        if (nums[i] != i) return i;
    }
    return n;
}


// ---------------------------------------------------------------------------
// 20. Find Duplicate Number
//     PROBLEM: Given n + 1 integers in range [1, n], find the repeated value
//              without modifying the array.
//     MENTAL MODEL: "array values act like next pointers; duplicate is cycle start"
// ---------------------------------------------------------------------------
int findDuplicate(vector<int>& nums) {
    int slow = nums[0], fast = nums[0];

    do {
        slow = nums[slow];
        fast = nums[nums[fast]];
    } while (slow != fast);

    slow = nums[0];
    while (slow != fast) {
        slow = nums[slow];
        fast = nums[fast];
    }
    return slow;
}


// ---------------------------------------------------------------------------
// 21. Rotate Array
//     PROBLEM: Rotate an array to the right by k steps in-place.
//     MENTAL MODEL: "reverse whole array, then reverse each side"
// ---------------------------------------------------------------------------
void rotateArray(vector<int>& nums, int k) {
    int n = nums.size();
    k %= n;
    reverse(nums.begin(), nums.end());
    reverse(nums.begin(), nums.begin() + k);
    reverse(nums.begin() + k, nums.end());
}


// ---------------------------------------------------------------------------
// 22. Spiral Matrix
//     PROBLEM: Return all matrix values in clockwise spiral order.
//     MENTAL MODEL: "peel the matrix layer by layer"
// ---------------------------------------------------------------------------
vector<int> spiralOrder(vector<vector<int>>& matrix) {
    vector<int> res;
    int top = 0, bottom = matrix.size() - 1;
    int left = 0, right = matrix[0].size() - 1;

    while (top <= bottom && left <= right) {
        for (int c = left; c <= right; c++) res.push_back(matrix[top][c]);
        top++;

        for (int r = top; r <= bottom; r++) res.push_back(matrix[r][right]);
        right--;

        if (top <= bottom) {
            for (int c = right; c >= left; c--) res.push_back(matrix[bottom][c]);
            bottom--;
        }

        if (left <= right) {
            for (int r = bottom; r >= top; r--) res.push_back(matrix[r][left]);
            left++;
        }
    }
    return res;
}


// ---------------------------------------------------------------------------
// 23. Rotate Image
//     PROBLEM: Rotate an n x n matrix 90 degrees clockwise in-place.
//     MENTAL MODEL: "transpose, then reverse each row"
// ---------------------------------------------------------------------------
void rotateImage(vector<vector<int>>& matrix) {
    int n = matrix.size();
    for (int r = 0; r < n; r++) {
        for (int c = r + 1; c < n; c++) {
            swap(matrix[r][c], matrix[c][r]);
        }
    }
    for (auto& row : matrix) reverse(row.begin(), row.end());
}


// ---------------------------------------------------------------------------
// 24. Set Matrix Zeroes
//     PROBLEM: If a matrix cell is zero, set its entire row and column to zero
//              in-place.
//     MENTAL MODEL: "use first row/column as marker storage"
// ---------------------------------------------------------------------------
void setZeroes(vector<vector<int>>& matrix) {
    int m = matrix.size(), n = matrix[0].size();
    bool firstRow = false, firstCol = false;

    for (int c = 0; c < n; c++) firstRow = firstRow || matrix[0][c] == 0;
    for (int r = 0; r < m; r++) firstCol = firstCol || matrix[r][0] == 0;

    for (int r = 1; r < m; r++) {
        for (int c = 1; c < n; c++) {
            if (matrix[r][c] == 0) {
                matrix[r][0] = 0;
                matrix[0][c] = 0;
            }
        }
    }

    for (int r = 1; r < m; r++) {
        for (int c = 1; c < n; c++) {
            if (matrix[r][0] == 0 || matrix[0][c] == 0) matrix[r][c] = 0;
        }
    }

    if (firstRow) for (int c = 0; c < n; c++) matrix[0][c] = 0;
    if (firstCol) for (int r = 0; r < m; r++) matrix[r][0] = 0;
}


int main() {
    cout << "=== Two Pointers ===" << endl;
    vector<int> ts{2,7,11,15};
    printVec(twoSum(ts, 9));
    printVec(twoSumSorted(ts, 18));
    vector<int> three{-1,0,1,2,-1,-4};
    for (auto& triplet : threeSum(three)) printVec(triplet);
    vector<int> water{1,8,6,2,5,4,8,3,7};
    cout << "Max area: " << maxArea(water) << endl;
    vector<int> bars{0,1,0,2,1,0,1,3,2,1,2,1};
    cout << "Trapped water: " << trap(bars) << endl;

    cout << "\n=== Sliding Window / Prefix ===" << endl;
    vector<int> distinct{1,2,1,2,3};
    cout << "At most 2 distinct: " << longestAtMostKDistinct(distinct, 2) << endl;
    vector<int> minLen{2,3,1,2,4,3};
    cout << "Min subarray len: " << minSubArrayLen(7, minLen) << endl;
    vector<int> sub{1,1,1};
    cout << "Subarray sum 2 count: " << subarraySum(sub, 2) << endl;
    vector<int> prod{1,2,3,4};
    printVec(productExceptSelf(prod));

    cout << "\n=== Kadane ===" << endl;
    vector<int> kad{-2,1,-3,4,-1,2,1,-5,4};
    cout << "Max subarray: " << maxSubArray(kad) << endl;
    vector<int> mp{2,3,-2,4};
    cout << "Max product: " << maxProduct(mp) << endl;

    cout << "\n=== Binary Search ===" << endl;
    vector<int> sorted{1,2,3,4,5};
    cout << "Search 4: " << binarySearch(sorted, 4) << endl;
    vector<int> rotated{4,5,6,7,0,1,2};
    cout << "Search rotated 0: " << searchRotated(rotated, 0) << endl;
    vector<int> rangeInput{5,7,7,8,8,10};
    printVec(searchRange(rangeInput, 8));
    vector<int> weights{1,2,3,4,5,6,7,8,9,10};
    cout << "Ship capacity: " << shipWithinDays(weights, 5) << endl;

    cout << "\n=== Intervals / Sorting ===" << endl;
    vector<vector<int>> intervals{{1,3},{2,6},{8,10},{15,18}};
    for (auto& in : mergeIntervals(intervals)) printVec(in);
    vector<vector<int>> insertIn{{1,3},{6,9}};
    for (auto& in : insertInterval(insertIn, {2,5})) printVec(in);
    vector<int> colors{2,0,2,1,1,0};
    sortColors(colors);
    printVec(colors);

    cout << "\n=== Cyclic / Duplicate / Rotate ===" << endl;
    vector<int> miss{3,0,1};
    cout << "Missing: " << missingNumber(miss) << endl;
    vector<int> dup{1,3,4,2,2};
    cout << "Duplicate: " << findDuplicate(dup) << endl;
    vector<int> rot{1,2,3,4,5,6,7};
    rotateArray(rot, 3);
    printVec(rot);

    cout << "\n=== Matrix ===" << endl;
    vector<vector<int>> mat{{1,2,3},{4,5,6},{7,8,9}};
    printVec(spiralOrder(mat));
    rotateImage(mat);
    printMatrix(mat);
    vector<vector<int>> zero{{1,1,1},{1,0,1},{1,1,1}};
    setZeroes(zero);
    printMatrix(zero);

    return 0;
}
