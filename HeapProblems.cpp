#include <bits/stdc++.h>
using namespace std;

// ---------------------------------------------------------------------------
// Heap / Priority Queue Problems Reference - C++
// Covers: kth element | top K frequency | k-way merge | two heaps | intervals |
//         greedy scheduling | custom comparators | matrix/grid heap patterns
// ---------------------------------------------------------------------------

struct ListNode {
    int val;
    ListNode* next;
    ListNode(int v = 0) : val(v), next(nullptr) {}
};

ListNode* buildList(const vector<int>& a) {
    ListNode dummy;
    ListNode* tail = &dummy;
    for (int x : a) {
        tail->next = new ListNode(x);
        tail = tail->next;
    }
    return dummy.next;
}

void printVec(const vector<int>& v) {
    for (int x : v) cout << x << " ";
    cout << endl;
}

void printList(ListNode* head) {
    while (head) {
        cout << head->val;
        if (head->next) cout << " -> ";
        head = head->next;
    }
    cout << endl;
}


// ---------------------------------------------------------------------------
// 1. Kth Largest / Kth Smallest
//    PROBLEM: Return the kth largest or kth smallest value from an unsorted array.
//    MENTAL MODEL: "keep only the best k candidates"
//    Kth largest: min-heap of size k. Heap top is the kth largest so far.
// ---------------------------------------------------------------------------
int kthLargest(vector<int> nums, int k) {
    priority_queue<int, vector<int>, greater<int>> pq;
    for (int x : nums) {
        pq.push(x);
        if ((int)pq.size() > k) pq.pop();
    }
    return pq.top();
}

int kthSmallest(vector<int> nums, int k) {
    priority_queue<int> pq;  // max-heap keeps k smallest; top is kth smallest
    for (int x : nums) {
        pq.push(x);
        if ((int)pq.size() > k) pq.pop();
    }
    return pq.top();
}


// ---------------------------------------------------------------------------
// 2. Top K Frequent Elements
//    PROBLEM: Given an array, return the k values that appear most often.
//    MENTAL MODEL: "count first, then keep k most frequent entries"
//    Min-heap by frequency removes the least useful candidate.
// ---------------------------------------------------------------------------
vector<int> topKFrequent(vector<int>& nums, int k) {
    unordered_map<int, int> freq;
    for (int x : nums) freq[x]++;

    using P = pair<int, int>; // {frequency, value}
    priority_queue<P, vector<P>, greater<P>> pq;

    for (auto& it : freq) {
        pq.push({it.second, it.first});
        if ((int)pq.size() > k) pq.pop();
    }

    vector<int> res;
    while (!pq.empty()) {
        res.push_back(pq.top().second);
        pq.pop();
    }
    reverse(res.begin(), res.end());
    return res;
}


// ---------------------------------------------------------------------------
// 3. K Closest Points to Origin
//    PROBLEM: Given points on a 2D plane, return the k points with the smallest
//             distance from (0, 0).
//    MENTAL MODEL: "distance ranks points; keep k smallest distances"
// ---------------------------------------------------------------------------
vector<vector<int>> kClosest(vector<vector<int>>& points, int k) {
    auto dist = [](const vector<int>& p) {
        return p[0] * p[0] + p[1] * p[1];
    };

    using P = pair<int, int>; // {distance, index}
    priority_queue<P> pq;    // max-heap removes farthest among chosen k

    for (int i = 0; i < (int)points.size(); i++) {
        pq.push({dist(points[i]), i});
        if ((int)pq.size() > k) pq.pop();
    }

    vector<vector<int>> res;
    while (!pq.empty()) {
        res.push_back(points[pq.top().second]);
        pq.pop();
    }
    return res;
}


// ---------------------------------------------------------------------------
// 4. Merge K Sorted Lists
//    PROBLEM: Merge k sorted linked lists into one sorted linked list.
//    MENTAL MODEL: "heap stores the current smallest head from each list"
// ---------------------------------------------------------------------------
struct ListCmp {
    bool operator()(ListNode* a, ListNode* b) const {
        return a->val > b->val; // min-heap by node value
    }
};

ListNode* mergeKLists(vector<ListNode*>& lists) {
    priority_queue<ListNode*, vector<ListNode*>, ListCmp> pq;
    for (ListNode* head : lists) {
        if (head) pq.push(head);
    }

    ListNode dummy;
    ListNode* tail = &dummy;
    while (!pq.empty()) {
        ListNode* node = pq.top();
        pq.pop();

        tail->next = node;
        tail = tail->next;

        if (node->next) pq.push(node->next);
    }

    return dummy.next;
}


// ---------------------------------------------------------------------------
// 5. Merge K Sorted Arrays
//    PROBLEM: Merge multiple sorted arrays into one sorted array.
//    MENTAL MODEL: "same as k lists; store value plus which array/index it came from"
// ---------------------------------------------------------------------------
vector<int> mergeKSortedArrays(vector<vector<int>>& arrays) {
    using T = tuple<int, int, int>; // {value, array index, element index}
    priority_queue<T, vector<T>, greater<T>> pq;

    for (int i = 0; i < (int)arrays.size(); i++) {
        if (!arrays[i].empty()) pq.push({arrays[i][0], i, 0});
    }

    vector<int> res;
    while (!pq.empty()) {
        auto cur = pq.top();
        pq.pop();
        int val = get<0>(cur);
        int row = get<1>(cur);
        int col = get<2>(cur);

        res.push_back(val);
        if (col + 1 < (int)arrays[row].size()) {
            pq.push({arrays[row][col + 1], row, col + 1});
        }
    }
    return res;
}


// ---------------------------------------------------------------------------
// 6. Find Median From Data Stream
//    PROBLEM: Support adding numbers one by one and returning the current median.
//    MENTAL MODEL: "left half is max-heap, right half is min-heap"
//    Keep sizes equal or left one larger. Median is top(s).
// ---------------------------------------------------------------------------
class MedianFinder {
    priority_queue<int> left; // smaller half
    priority_queue<int, vector<int>, greater<int>> right; // larger half

public:
    void addNum(int num) {
        if (left.empty() || num <= left.top()) left.push(num);
        else right.push(num);

        if ((int)left.size() > (int)right.size() + 1) {
            right.push(left.top());
            left.pop();
        } else if ((int)right.size() > (int)left.size()) {
            left.push(right.top());
            right.pop();
        }
    }

    double findMedian() {
        if (left.size() == right.size()) return (left.top() + right.top()) / 2.0;
        return left.top();
    }
};


// ---------------------------------------------------------------------------
// 7. Kth Largest Stream
//    PROBLEM: Maintain the kth largest value after each new number is added.
//    MENTAL MODEL: "same as kth largest, but update one value at a time"
// ---------------------------------------------------------------------------
class KthLargestStream {
    int k;
    priority_queue<int, vector<int>, greater<int>> pq;

public:
    KthLargestStream(int k, vector<int>& nums) : k(k) {
        for (int x : nums) add(x);
    }

    int add(int val) {
        pq.push(val);
        if ((int)pq.size() > k) pq.pop();
        return pq.top();
    }
};


// ---------------------------------------------------------------------------
// 8. Last Stone Weight
//    PROBLEM: Repeatedly smash the two heaviest stones and return the final
//             stone weight, or 0 if none remain.
//    MENTAL MODEL: "always smash the two largest remaining stones"
// ---------------------------------------------------------------------------
int lastStoneWeight(vector<int>& stones) {
    priority_queue<int> pq(stones.begin(), stones.end());
    while (pq.size() > 1) {
        int a = pq.top(); pq.pop();
        int b = pq.top(); pq.pop();
        if (a != b) pq.push(a - b);
    }
    return pq.empty() ? 0 : pq.top();
}


// ---------------------------------------------------------------------------
// 9. Connect Ropes / Minimum Cost to Combine
//    PROBLEM: Given rope lengths, repeatedly connect two ropes with cost equal
//             to their sum; return the minimum total cost.
//    MENTAL MODEL: "always merge the two smallest ropes first"
//    This is the same greedy shape as Huffman coding.
// ---------------------------------------------------------------------------
int connectRopes(vector<int>& ropes) {
    priority_queue<int, vector<int>, greater<int>> pq(ropes.begin(), ropes.end());
    int cost = 0;

    while (pq.size() > 1) {
        int a = pq.top(); pq.pop();
        int b = pq.top(); pq.pop();
        cost += a + b;
        pq.push(a + b);
    }
    return cost;
}


// ---------------------------------------------------------------------------
// 10. Meeting Rooms II
//     PROBLEM: Given meeting intervals, return the minimum number of rooms
//              needed so all meetings can happen.
//     MENTAL MODEL: "min-heap tracks the earliest ending active meeting"
// ---------------------------------------------------------------------------
int minMeetingRooms(vector<vector<int>>& intervals) {
    sort(intervals.begin(), intervals.end());
    priority_queue<int, vector<int>, greater<int>> ends;

    for (auto& in : intervals) {
        if (!ends.empty() && ends.top() <= in[0]) ends.pop();
        ends.push(in[1]);
    }
    return ends.size();
}


// ---------------------------------------------------------------------------
// 11. Task Scheduler
//     PROBLEM: Given task letters and cooldown n, return the minimum time needed
//              to run all tasks while respecting cooldowns.
//     MENTAL MODEL: "run the most frequent available task; cooldown delays reuse"
// ---------------------------------------------------------------------------
int leastInterval(vector<char>& tasks, int n) {
    unordered_map<char, int> freq;
    for (char c : tasks) freq[c]++;

    priority_queue<int> available;
    for (auto& it : freq) available.push(it.second);

    queue<pair<int, int>> cooling; // {remaining count, time when available}
    int time = 0;

    while (!available.empty() || !cooling.empty()) {
        time++;

        if (!available.empty()) {
            int cnt = available.top();
            available.pop();
            if (--cnt > 0) cooling.push({cnt, time + n});
        }

        if (!cooling.empty() && cooling.front().second == time) {
            available.push(cooling.front().first);
            cooling.pop();
        }
    }

    return time;
}


// ---------------------------------------------------------------------------
// 12. Reorganize String
//     PROBLEM: Rearrange a string so no two adjacent characters are equal, or
//              return an empty string if impossible.
//     MENTAL MODEL: "always place the two most frequent different characters"
// ---------------------------------------------------------------------------
string reorganizeString(string s) {
    vector<int> freq(26, 0);
    for (char c : s) freq[c - 'a']++;

    priority_queue<pair<int, char>> pq;
    for (int i = 0; i < 26; i++) {
        if (freq[i]) pq.push({freq[i], char('a' + i)});
    }

    string res;
    while (pq.size() >= 2) {
        auto a = pq.top(); pq.pop();
        auto b = pq.top(); pq.pop();

        res += a.second;
        res += b.second;

        if (--a.first > 0) pq.push(a);
        if (--b.first > 0) pq.push(b);
    }

    if (!pq.empty()) {
        if (pq.top().first > 1) return "";
        res += pq.top().second;
    }
    return res;
}


// ---------------------------------------------------------------------------
// 13. Furthest Building You Can Reach
//     PROBLEM: Given building heights, bricks, and ladders, return the furthest
//              building index reachable from left to right.
//     MENTAL MODEL: "use ladders on the largest climbs, bricks on smaller climbs"
// ---------------------------------------------------------------------------
int furthestBuilding(vector<int>& heights, int bricks, int ladders) {
    priority_queue<int, vector<int>, greater<int>> ladderClimbs;

    for (int i = 0; i + 1 < (int)heights.size(); i++) {
        int climb = heights[i + 1] - heights[i];
        if (climb <= 0) continue;

        ladderClimbs.push(climb);
        if ((int)ladderClimbs.size() > ladders) {
            bricks -= ladderClimbs.top();
            ladderClimbs.pop();
        }
        if (bricks < 0) return i;
    }
    return heights.size() - 1;
}


// ---------------------------------------------------------------------------
// 14. K Smallest Pairs
//     PROBLEM: Given two sorted arrays, return k pairs with the smallest sums.
//     MENTAL MODEL: "each row is sorted by pairing nums1[i] with increasing nums2"
// ---------------------------------------------------------------------------
vector<vector<int>> kSmallestPairs(vector<int>& nums1, vector<int>& nums2, int k) {
    vector<vector<int>> res;
    if (nums1.empty() || nums2.empty() || k == 0) return res;

    using T = tuple<int, int, int>; // {sum, i, j}
    priority_queue<T, vector<T>, greater<T>> pq;

    for (int i = 0; i < (int)nums1.size() && i < k; i++) {
        pq.push({nums1[i] + nums2[0], i, 0});
    }

    while (!pq.empty() && (int)res.size() < k) {
        auto cur = pq.top();
        pq.pop();
        int i = get<1>(cur);
        int j = get<2>(cur);
        res.push_back({nums1[i], nums2[j]});
        if (j + 1 < (int)nums2.size()) {
            pq.push({nums1[i] + nums2[j + 1], i, j + 1});
        }
    }
    return res;
}


// ---------------------------------------------------------------------------
// 15. Kth Smallest in Sorted Matrix
//     PROBLEM: Given a row-sorted matrix, return the kth smallest element.
//     MENTAL MODEL: "matrix rows are sorted; heap advances one cell per row"
// ---------------------------------------------------------------------------
int kthSmallestMatrix(vector<vector<int>>& matrix, int k) {
    using T = tuple<int, int, int>; // {value, row, col}
    priority_queue<T, vector<T>, greater<T>> pq;

    int n = matrix.size();
    for (int r = 0; r < n; r++) pq.push({matrix[r][0], r, 0});

    while (--k) {
        auto cur = pq.top();
        pq.pop();
        int r = get<1>(cur);
        int c = get<2>(cur);
        if (c + 1 < (int)matrix[r].size()) {
            pq.push({matrix[r][c + 1], r, c + 1});
        }
    }
    return get<0>(pq.top());
}


// ---------------------------------------------------------------------------
// 16. Smallest Range Covering K Lists
//     PROBLEM: Given k sorted lists, find the smallest numeric range containing
//              at least one number from each list.
//     MENTAL MODEL: "heap gives current minimum; track current maximum"
// ---------------------------------------------------------------------------
vector<int> smallestRange(vector<vector<int>>& nums) {
    using T = tuple<int, int, int>; // {value, list index, element index}
    priority_queue<T, vector<T>, greater<T>> pq;
    int currentMax = INT_MIN;

    for (int i = 0; i < (int)nums.size(); i++) {
        pq.push({nums[i][0], i, 0});
        currentMax = max(currentMax, nums[i][0]);
    }

    vector<int> best{get<0>(pq.top()), currentMax};
    while (true) {
        auto cur = pq.top();
        pq.pop();
        int val = get<0>(cur);
        int row = get<1>(cur);
        int col = get<2>(cur);

        if (currentMax - val < best[1] - best[0]) best = {val, currentMax};
        if (col + 1 == (int)nums[row].size()) break;

        int nextVal = nums[row][col + 1];
        pq.push({nextVal, row, col + 1});
        currentMax = max(currentMax, nextVal);
    }
    return best;
}


// ---------------------------------------------------------------------------
// 17. Trapping Rain Water II
//     PROBLEM: Given a 2D elevation map, compute how much rain water can be
//              trapped after raining.
//     MENTAL MODEL: "process boundary from lowest wall inward"
//     The lowest boundary decides how much water a neighbor can hold.
// ---------------------------------------------------------------------------
int trapRainWater(vector<vector<int>>& heightMap) {
    int m = heightMap.size();
    int n = heightMap[0].size();
    if (m < 3 || n < 3) return 0;

    using T = tuple<int, int, int>; // {height, row, col}
    priority_queue<T, vector<T>, greater<T>> pq;
    vector<vector<bool>> seen(m, vector<bool>(n, false));

    for (int r = 0; r < m; r++) {
        pq.push({heightMap[r][0], r, 0});
        pq.push({heightMap[r][n - 1], r, n - 1});
        seen[r][0] = seen[r][n - 1] = true;
    }
    for (int c = 1; c + 1 < n; c++) {
        pq.push({heightMap[0][c], 0, c});
        pq.push({heightMap[m - 1][c], m - 1, c});
        seen[0][c] = seen[m - 1][c] = true;
    }

    int water = 0;
    int dirs[5] = {1, 0, -1, 0, 1};
    while (!pq.empty()) {
        auto cur = pq.top();
        pq.pop();
        int h = get<0>(cur);
        int r = get<1>(cur);
        int c = get<2>(cur);

        for (int d = 0; d < 4; d++) {
            int nr = r + dirs[d];
            int nc = c + dirs[d + 1];
            if (nr < 0 || nc < 0 || nr >= m || nc >= n || seen[nr][nc]) continue;

            seen[nr][nc] = true;
            water += max(0, h - heightMap[nr][nc]);
            pq.push({max(h, heightMap[nr][nc]), nr, nc});
        }
    }
    return water;
}


int main() {
    cout << "=== Kth Elements ===" << endl;
    vector<int> nums{3,2,1,5,6,4};
    cout << "2nd largest: " << kthLargest(nums, 2) << endl;
    cout << "2nd smallest: " << kthSmallest(nums, 2) << endl;

    cout << "\n=== Top K / Closest ===" << endl;
    vector<int> freqInput{1,1,1,2,2,3};
    printVec(topKFrequent(freqInput, 2));
    vector<vector<int>> points{{1,3},{-2,2},{5,8}};
    for (auto& p : kClosest(points, 2)) cout << "(" << p[0] << "," << p[1] << ") ";
    cout << endl;

    cout << "\n=== K-way Merge ===" << endl;
    vector<ListNode*> lists{buildList({1,4,5}), buildList({1,3,4}), buildList({2,6})};
    printList(mergeKLists(lists));
    vector<vector<int>> arrays{{1,4,7},{2,5,8},{3,6,9}};
    printVec(mergeKSortedArrays(arrays));

    cout << "\n=== Two Heaps ===" << endl;
    MedianFinder mf;
    for (int x : {1,2,3}) mf.addNum(x);
    cout << "Median: " << mf.findMedian() << endl;
    vector<int> streamInit{4,5,8,2};
    KthLargestStream stream(3, streamInit);
    cout << "Kth stream after add 3: " << stream.add(3) << endl;

    cout << "\n=== Greedy Heap ===" << endl;
    vector<int> stones{2,7,4,1,8,1};
    cout << "Last stone: " << lastStoneWeight(stones) << endl;
    vector<int> ropes{1,2,3,4,5};
    cout << "Connect ropes cost: " << connectRopes(ropes) << endl;
    vector<vector<int>> meetings{{0,30},{5,10},{15,20}};
    cout << "Meeting rooms: " << minMeetingRooms(meetings) << endl;

    cout << "\n=== Scheduling / Strings ===" << endl;
    vector<char> tasks{'A','A','A','B','B','B'};
    cout << "Least interval: " << leastInterval(tasks, 2) << endl;
    cout << "Reorganized aab: " << reorganizeString("aab") << endl;

    cout << "\n=== Advanced Heap Patterns ===" << endl;
    vector<int> heights{4,2,7,6,9,14,12};
    cout << "Furthest building: " << furthestBuilding(heights, 5, 1) << endl;
    vector<int> a{1,7,11}, b{2,4,6};
    for (auto& p : kSmallestPairs(a, b, 3)) cout << "(" << p[0] << "," << p[1] << ") ";
    cout << endl;

    vector<vector<int>> matrix{{1,5,9},{10,11,13},{12,13,15}};
    cout << "8th smallest matrix: " << kthSmallestMatrix(matrix, 8) << endl;
    vector<vector<int>> rangeLists{{4,10,15,24,26},{0,9,12,20},{5,18,22,30}};
    vector<int> range = smallestRange(rangeLists);
    cout << "Smallest range: [" << range[0] << ", " << range[1] << "]" << endl;
    vector<vector<int>> water{{1,4,3,1,3,2},{3,2,1,3,2,4},{2,3,3,2,3,1}};
    cout << "Trapped water II: " << trapRainWater(water) << endl;

    return 0;
}
