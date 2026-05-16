// SliceDSA.cpp  — Slice-tagged DSA problems (C++14, g++ -std=c++14)
// Compile:  g++ -std=c++14 -O2 -o SliceDSA SliceDSA.cpp
//
// Sections:
//   1.  Linked List   — Reverse LL, Cycle Detection (Floyd's), Intersection
//   2.  Stack         — Maximum Stack (O(1) getMax), Valid Parentheses
//   3.  Binary Tree   — LCA, Maximum Path Sum
//   4.  Arrays/Greedy — Search in Rotated Sorted Array, Remove K Digits
//   5.  Graph         — Task Scheduling with Dependencies (topological sort)

#include <bits/stdc++.h>
using namespace std;

// ─── Shared node types ────────────────────────────────────────────────────

struct ListNode {
    int val;
    ListNode* next;
    ListNode(int x) : val(x), next(nullptr) {}
};

struct TreeNode {
    int val;
    TreeNode* left;
    TreeNode* right;
    TreeNode(int x) : val(x), left(nullptr), right(nullptr) {}
};

// Build a linked list from a vector; returns head.
ListNode* buildList(vector<int> vals) {
    if (vals.empty()) return nullptr;
    ListNode* head = new ListNode(vals[0]);
    ListNode* cur = head;
    for (int i = 1; i < (int)vals.size(); i++) {
        cur->next = new ListNode(vals[i]);
        cur = cur->next;
    }
    return head;
}

string listToStr(ListNode* head) {
    string s = "[";
    while (head) {
        s += to_string(head->val);
        if (head->next) s += "->";
        head = head->next;
    }
    return s + "]";
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 1 — LINKED LIST
// ═══════════════════════════════════════════════════════════════════════════

// ── 1A. Reverse Linked List (LC 206) ─────────────────────────────────────
// Reverse a singly linked list; return the new head.
// Approach (iterative): maintain prev/curr; redirect curr->next to prev, advance both.
//   O(n) time, O(1) space.
// Approach (recursive): recursively reverse the tail; then make head->next->next
//   point back to head and clear head->next. O(n) time, O(n) stack space.

ListNode* reverseListIter(ListNode* head) {
    ListNode* prev = nullptr;
    ListNode* cur  = head;
    while (cur) {
        ListNode* nxt = cur->next;
        cur->next = prev;
        prev = cur;
        cur  = nxt;
    }
    return prev;
}

ListNode* reverseListRec(ListNode* head) {
    if (!head || !head->next) return head;
    ListNode* newHead = reverseListRec(head->next);
    head->next->next = head;  // reverse the link
    head->next = nullptr;
    return newHead;
}

// ── 1B. Linked List Cycle — Floyd's Two-Pointer Algorithm (LC 142) ────────
// Detect if a linked list has a cycle; return the cycle-entry node (null if none).
// Approach: Phase 1 — slow (1 step) and fast (2 steps) advance until they meet,
//   proving a cycle exists. Phase 2 — reset one pointer to head; advance both
//   1 step until they meet again at the cycle entry.
//   Proof: at Phase 1 meeting, slow traveled F+a steps (F = head→entry,
//   a = entry→meeting). fast = 2×slow = F+a+nC, so F = nC − a, meaning
//   head→entry and meeting-point→entry are the same distance (mod cycle length).
// Complexity: O(n) time, O(1) space.

ListNode* detectCycleEntry(ListNode* head) {
    ListNode* slow = head;
    ListNode* fast = head;
    while (fast && fast->next) {
        slow = slow->next;
        fast = fast->next->next;
        if (slow == fast) {
            slow = head;  // reset one pointer to head for phase 2
            while (slow != fast) {
                slow = slow->next;
                fast = fast->next;
            }
            return slow;  // cycle entry
        }
    }
    return nullptr;
}

// ── 1C. Intersection of Two Linked Lists (LC 160) ─────────────────────────
// Find the node where two lists share the same physical node; null if none.
// Approach: pointers a and b start at their respective heads. When a reaches null,
//   redirect to headB; when b reaches null, redirect to headA. After at most
//   len(A)+len(B) steps they both will be equidistant from the intersection and
//   will meet there (or both reach null simultaneously if no intersection).
// Complexity: O(m+n) time, O(1) space.

ListNode* getIntersection(ListNode* headA, ListNode* headB) {
    ListNode* a = headA;
    ListNode* b = headB;
    while (a != b) {
        a = a ? a->next : headB;  // redirect at end
        b = b ? b->next : headA;
    }
    return a;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 2 — STACK
// ═══════════════════════════════════════════════════════════════════════════

// ── 2A. Maximum Stack — O(1) push/pop/getMax (LC 155 variant) ────────────
// Stack supporting push, pop, top, and getMax in O(1) time.
// Approach: each entry stores (value, maxSoFar) as a pair. When pushing x,
//   maxSoFar = max(x, previous maxSoFar). getMax() reads the top pair's second.
//   No extra pass is needed on pop because each level carries its own max.
// Complexity: O(1) all ops, O(n) space.

struct MaxStack {
    vector<pair<int,int>> st;  // (val, maxSoFar)

    void push(int x) {
        int mx = st.empty() ? x : max(x, st.back().second);
        st.push_back(make_pair(x, mx));
    }
    void pop()    { st.pop_back(); }
    int  top()    { return st.back().first; }
    int  getMax() { return st.back().second; }
    bool empty()  { return st.empty(); }
};

// ── 2B. Valid Parentheses (LC 20) ─────────────────────────────────────────
// Return true if every open bracket has a matching close bracket in order.
// Approach: stack-based scan. Push open brackets; on close bracket, check that
//   the stack top is the matching open. Final state must be an empty stack.
// Complexity: O(n) time, O(n) space.

bool isValid(const string& s) {
    stack<char> st;
    for (char c : s) {
        if (c == '(' || c == '[' || c == '{') {
            st.push(c);
        } else {
            if (st.empty()) return false;
            char top = st.top(); st.pop();
            if (c == ')' && top != '(') return false;
            if (c == ']' && top != '[') return false;
            if (c == '}' && top != '{') return false;
        }
    }
    return st.empty();
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 3 — BINARY TREE
// ═══════════════════════════════════════════════════════════════════════════

// ── 3A. Lowest Common Ancestor (LC 236) ───────────────────────────────────
// Return the deepest node that is an ancestor of both p and q.
// Approach: recursive DFS. If root is null, p, or q, return root. Recurse into
//   both subtrees. If both return non-null, root is the LCA (p and q are on
//   different sides). Otherwise return whichever side is non-null.
// Complexity: O(n) time, O(h) space (h = tree height).

TreeNode* lca(TreeNode* root, TreeNode* p, TreeNode* q) {
    if (!root || root == p || root == q) return root;
    TreeNode* left  = lca(root->left,  p, q);
    TreeNode* right = lca(root->right, p, q);
    if (left && right) return root;  // p and q are on opposite sides
    return left ? left : right;
}

// ── 3B. Binary Tree Maximum Path Sum (LC 124) ─────────────────────────────
// Maximum sum of any path in the tree (any start/end, each node used at most once).
// Approach: DFS with a running global max. At each node, compute the max gain from
//   each subtree (clamped to 0 to skip negative branches). Update global max with
//   the path through this node (node->val + leftGain + rightGain). Return to parent
//   only node->val + max(leftGain, rightGain) since a path can extend in only one
//   direction upward.
// Complexity: O(n) time, O(h) space.

int maxPathSum(TreeNode* root) {
    int ans = INT_MIN;
    function<int(TreeNode*)> dfs = [&](TreeNode* node) -> int {
        if (!node) return 0;
        int lg = max(0, dfs(node->left));
        int rg = max(0, dfs(node->right));
        ans = max(ans, node->val + lg + rg);   // best path through this node
        return node->val + max(lg, rg);         // extend only one branch upward
    };
    dfs(root);
    return ans;
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 4 — ARRAYS & GREEDY
// ═══════════════════════════════════════════════════════════════════════════

// ── 4A. Search in Rotated Sorted Array (LC 33) ───────────────────────────
// Find target's index in a sorted array rotated at some unknown pivot; -1 if absent.
// Approach: modified binary search. At each step one half is always sorted.
//   If nums[lo] <= nums[mid], left half is sorted: check if target ∈ [lo, mid);
//   else right half is sorted: check if target ∈ (mid, hi]. Narrow accordingly.
// Complexity: O(log n) time, O(1) space.

int searchRotated(const vector<int>& nums, int target) {
    int lo = 0, hi = (int)nums.size() - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (nums[mid] == target) return mid;
        if (nums[lo] <= nums[mid]) {            // left half is sorted
            if (nums[lo] <= target && target < nums[mid]) hi = mid - 1;
            else                                            lo = mid + 1;
        } else {                                // right half is sorted
            if (nums[mid] < target && target <= nums[hi]) lo = mid + 1;
            else                                           hi = mid - 1;
        }
    }
    return -1;
}

// ── 4B. Remove K Digits (LC 402) ─────────────────────────────────────────
// Remove k digits from a numeric string to form the smallest possible number.
// Approach: monotone-increasing stack. For each digit, while k > 0 and the stack
//   top is larger than the current digit, greedily pop (remove a higher-order larger
//   digit). After scanning, if k > 0 remain, remove k digits from the tail (the
//   sequence is now non-decreasing, so the tail digits are least impactful). Strip
//   leading zeros; return "0" for an empty result.
// Complexity: O(n) time and space.

string removeKDigits(const string& num, int k) {
    string st;                                  // acts as stack; back = top
    for (char c : num) {
        while (k > 0 && !st.empty() && st.back() > c) {
            st.pop_back();
            k--;
        }
        st.push_back(c);
    }
    st.resize(st.size() - k);                  // remove remaining k from tail
    size_t start = st.find_first_not_of('0');
    return (start == string::npos) ? "0" : st.substr(start);
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 5 — GRAPH
// ═══════════════════════════════════════════════════════════════════════════

// ── 5A. Task Scheduling with Dependencies — Kahn's Topological Sort ───────
// Given n tasks and prerequisite pairs (task, before) meaning `before` must
// complete before `task`, return a valid execution order; return {} if a cycle exists.
// Approach: Kahn's BFS-based topological sort. Build adjacency list and indegree
//   array. Enqueue all tasks with indegree 0. Process each: reduce neighbours'
//   indegrees and enqueue any that reach 0. If processed count < n, a cycle prevents
//   a valid ordering.
// Complexity: O(V + E) time and space.

vector<int> taskOrder(int n, vector<pair<int,int> >& prereqs) {
    vector<vector<int> > adj(n);
    vector<int> indegree(n, 0);
    for (int i = 0; i < (int)prereqs.size(); i++) {
        int task   = prereqs[i].first;
        int before = prereqs[i].second;
        adj[before].push_back(task);
        indegree[task]++;
    }
    queue<int> q;
    for (int i = 0; i < n; i++)
        if (indegree[i] == 0) q.push(i);

    vector<int> order;
    while (!q.empty()) {
        int u = q.front(); q.pop();
        order.push_back(u);
        for (int v : adj[u])
            if (--indegree[v] == 0) q.push(v);
    }
    return ((int)order.size() == n) ? order : vector<int>();  // empty = cycle
}

// ═══════════════════════════════════════════════════════════════════════════
// MAIN
// ═══════════════════════════════════════════════════════════════════════════

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    // ── Section 1: Linked List ─────────────────────────────────────────────
    cout << "=== SECTION 1: LINKED LIST ===" << endl;

    // 1A — Reverse
    ListNode* l1 = buildList({1, 2, 3, 4, 5});
    cout << "reverseIter([1->2->3->4->5]) = " << listToStr(reverseListIter(l1)) << endl;
    // Expected: [5->4->3->2->1]

    ListNode* l2 = buildList({1, 2, 3, 4, 5});
    cout << "reverseRec ([1->2->3->4->5]) = " << listToStr(reverseListRec(l2)) << endl;
    // Expected: [5->4->3->2->1]

    // 1B — Cycle detection: 3->2->0->-4->2 (cycle at node 2)
    ListNode* n3  = new ListNode(3);
    ListNode* n2  = new ListNode(2);
    ListNode* n0  = new ListNode(0);
    ListNode* nm4 = new ListNode(-4);
    n3->next = n2; n2->next = n0; n0->next = nm4; nm4->next = n2;
    ListNode* entry = detectCycleEntry(n3);
    cout << "detectCycleEntry(3->2->0->-4->|2|): entry val = "
         << (entry ? entry->val : -1) << endl;
    // Expected: 2

    ListNode* noCycle = buildList({1, 2, 3});
    cout << "detectCycleEntry(no cycle) = "
         << (detectCycleEntry(noCycle) ? "cycle" : "null") << endl;
    // Expected: null

    // 1C — Intersection: A=[4->1] + common=[8->4->5], B=[5->6->1] + common
    ListNode* common = buildList({8, 4, 5});
    ListNode* hA = new ListNode(4); hA->next = new ListNode(1); hA->next->next = common;
    ListNode* hB = new ListNode(5); hB->next = new ListNode(6);
    hB->next->next = new ListNode(1); hB->next->next->next = common;
    ListNode* inter = getIntersection(hA, hB);
    cout << "getIntersection(A=[4,1,8,4,5] B=[5,6,1,8,4,5]): intersection val = "
         << (inter ? inter->val : -1) << endl;
    // Expected: 8

    // ── Section 2: Stack ───────────────────────────────────────────────────
    cout << "\n=== SECTION 2: STACK ===" << endl;

    MaxStack ms;
    ms.push(3); ms.push(1); ms.push(5); ms.push(2);
    cout << "MaxStack push [3,1,5,2] -> getMax = " << ms.getMax() << endl;
    // Expected: 5
    ms.pop();
    cout << "after pop(2) -> getMax = " << ms.getMax() << endl;
    // Expected: 5
    ms.pop();
    cout << "after pop(5) -> getMax = " << ms.getMax() << endl;
    // Expected: 3

    cout << "isValid(\"()[]{}\")  = " << isValid("()[]{}") << endl;
    // Expected: 1
    cout << "isValid(\"([)]\")    = " << isValid("([)]")   << endl;
    // Expected: 0
    cout << "isValid(\"{[]}\")    = " << isValid("{[]}")   << endl;
    // Expected: 1

    // ── Section 3: Binary Tree ─────────────────────────────────────────────
    cout << "\n=== SECTION 3: BINARY TREE ===" << endl;

    //       3
    //      / \
    //     5   1
    //    / \ / \
    //   6  2 0  8
    //     / \
    //    7   4
    TreeNode* root = new TreeNode(3);
    root->left  = new TreeNode(5); root->right = new TreeNode(1);
    root->left->left  = new TreeNode(6); root->left->right  = new TreeNode(2);
    root->right->left = new TreeNode(0); root->right->right = new TreeNode(8);
    root->left->right->left  = new TreeNode(7);
    root->left->right->right = new TreeNode(4);

    cout << "LCA(5, 1) = " << lca(root, root->left, root->right)->val << endl;
    // Expected: 3
    cout << "LCA(5, 4) = "
         << lca(root, root->left, root->left->right->right)->val << endl;
    // Expected: 5

    //     -10
    //     / \
    //    9  20
    //      /  \
    //     15   7
    TreeNode* r2  = new TreeNode(-10);
    r2->left      = new TreeNode(9);
    r2->right     = new TreeNode(20);
    r2->right->left  = new TreeNode(15);
    r2->right->right = new TreeNode(7);
    cout << "maxPathSum([-10,9,20,_,_,15,7]) = " << maxPathSum(r2) << endl;
    // Expected: 42  (path 15->20->7)

    // ── Section 4: Arrays & Greedy ─────────────────────────────────────────
    cout << "\n=== SECTION 4: ARRAYS & GREEDY ===" << endl;

    vector<int> rot = {4, 5, 6, 7, 0, 1, 2};
    cout << "searchRotated([4,5,6,7,0,1,2], 0) = " << searchRotated(rot, 0) << endl;
    // Expected: 4
    cout << "searchRotated([4,5,6,7,0,1,2], 3) = " << searchRotated(rot, 3) << endl;
    // Expected: -1

    cout << "removeKDigits(\"1432219\", 3) = " << removeKDigits("1432219", 3) << endl;
    // Expected: 1219
    cout << "removeKDigits(\"10200\",   1) = " << removeKDigits("10200",   1) << endl;
    // Expected: 200
    cout << "removeKDigits(\"10\",      2) = " << removeKDigits("10",      2) << endl;
    // Expected: 0

    // ── Section 5: Graph ───────────────────────────────────────────────────
    cout << "\n=== SECTION 5: GRAPH ===" << endl;

    // 4 tasks: 0 -> {1,2} -> 3
    vector<pair<int,int> > prereqs;
    prereqs.push_back(make_pair(1, 0));
    prereqs.push_back(make_pair(2, 0));
    prereqs.push_back(make_pair(3, 1));
    prereqs.push_back(make_pair(3, 2));
    vector<int> ord = taskOrder(4, prereqs);
    cout << "taskOrder(4, [[1,0],[2,0],[3,1],[3,2]]): ";
    for (int t : ord) cout << t << " ";
    cout << endl;
    // Expected: 0 1 2 3  (or 0 2 1 3 — both valid)

    vector<pair<int,int> > cyc;
    cyc.push_back(make_pair(0, 1));
    cyc.push_back(make_pair(1, 0));
    vector<int> noOrd = taskOrder(2, cyc);
    cout << "taskOrder with cycle: "
         << (noOrd.empty() ? "no valid order" : "valid") << endl;
    // Expected: no valid order

    return 0;
}
