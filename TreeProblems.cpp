#include <bits/stdc++.h>
using namespace std;

// ─────────────────────────────────────────────────────────────────────────
// Tree Problems Reference — C++
// Covers: BST ops | Traversals | Properties | Path problems | LCA |
//         Serialize/Deserialize | Structural transforms | Complete tree
// ─────────────────────────────────────────────────────────────────────────

struct TreeNode {
    int val;
    TreeNode *left, *right;
    TreeNode(int v = 0) : val(v), left(nullptr), right(nullptr) {}
};

// Build tree from level-order array (-1 = null)
TreeNode* build(vector<int>& a) {
    if (a.empty() || a[0] == -1) return nullptr;
    TreeNode* root = new TreeNode(a[0]);
    queue<TreeNode*> q;
    q.push(root);
    int i = 1;
    while (!q.empty() && i < (int)a.size()) {
        TreeNode* node = q.front(); q.pop();
        if (i < (int)a.size() && a[i] != -1) { node->left = new TreeNode(a[i]); q.push(node->left); }
        i++;
        if (i < (int)a.size() && a[i] != -1) { node->right = new TreeNode(a[i]); q.push(node->right); }
        i++;
    }
    return root;
}


// ─────────────────────────────────────────────────────────────────────────
// 1. BST: Insert / Search / Delete
//    MENTAL MODEL: "always go left if smaller, right if larger"
//    Delete: 3 cases — (a) leaf → nullptr, (b) one child → replace with child,
//            (c) two children → replace val with inorder successor (leftmost of right subtree)
// ─────────────────────────────────────────────────────────────────────────
TreeNode* bstInsert(TreeNode* root, int val) {
    if (!root) return new TreeNode(val);
    if (val < root->val) root->left  = bstInsert(root->left, val);
    else if (val > root->val) root->right = bstInsert(root->right, val);
    return root;
}

bool bstSearch(TreeNode* root, int val) {
    if (!root) return false;
    if (val == root->val) return true;
    return val < root->val ? bstSearch(root->left, val) : bstSearch(root->right, val);
}

TreeNode* bstDelete(TreeNode* root, int val) {
    if (!root) return nullptr;
    if (val < root->val) { root->left = bstDelete(root->left, val); }
    else if (val > root->val) { root->right = bstDelete(root->right, val); }
    else {
        if (!root->left) return root->right;
        if (!root->right) return root->left;
        // find inorder successor: leftmost node of right subtree
        TreeNode* succ = root->right;
        while (succ->left) succ = succ->left;
        root->val = succ->val;
        root->right = bstDelete(root->right, succ->val);
    }
    return root;
}


// ─────────────────────────────────────────────────────────────────────────
// 2. Traversals (recursive)
//    MENTAL MODEL: "pre=root first; in=sorted BST; post=children before parent"
//    Preorder: useful for copying/serializing trees (root first)
//    Inorder: gives sorted sequence for BST
//    Postorder: useful for deletion, evaluating expression trees
// ─────────────────────────────────────────────────────────────────────────
void inorder(TreeNode* root, vector<int>& res) {
    if (!root) return;
    inorder(root->left, res);
    res.push_back(root->val);
    inorder(root->right, res);
}
void preorder(TreeNode* root, vector<int>& res) {
    if (!root) return;
    res.push_back(root->val);
    preorder(root->left, res);
    preorder(root->right, res);
}
void postorder(TreeNode* root, vector<int>& res) {
    if (!root) return;
    postorder(root->left, res);
    postorder(root->right, res);
    res.push_back(root->val);
}

// Iterative inorder — uses stack; simulates call frames
vector<int> inorderIter(TreeNode* root) {
    vector<int> res;
    stack<TreeNode*> st;
    TreeNode* cur = root;
    while (cur || !st.empty()) {
        while (cur) { st.push(cur); cur = cur->left; }  // go left as far as possible
        cur = st.top(); st.pop();
        res.push_back(cur->val);
        cur = cur->right;
    }
    return res;
}

// Iterative preorder — push right before left (right processed after left)
vector<int> preorderIter(TreeNode* root) {
    if (!root) return {};
    vector<int> res;
    stack<TreeNode*> st;
    st.push(root);
    while (!st.empty()) {
        TreeNode* node = st.top(); st.pop();
        res.push_back(node->val);
        if (node->right) st.push(node->right);  // right first → processed second
        if (node->left)  st.push(node->left);
    }
    return res;
}


// ─────────────────────────────────────────────────────────────────────────
// 3. Level-Order BFS / Right Side View / Zigzag
//    MENTAL MODEL: "BFS with queue; snapshot queue.size() at start of each level"
// ─────────────────────────────────────────────────────────────────────────
vector<vector<int>> levelOrder(TreeNode* root) {
    if (!root) return {};
    vector<vector<int>> res;
    queue<TreeNode*> q;
    q.push(root);
    while (!q.empty()) {
        int sz = q.size();
        vector<int> level;
        for (int i = 0; i < sz; i++) {
            TreeNode* node = q.front(); q.pop();
            level.push_back(node->val);
            if (node->left)  q.push(node->left);
            if (node->right) q.push(node->right);
        }
        res.push_back(level);
    }
    return res;
}

// Right side view: last node at each level
vector<int> rightSideView(TreeNode* root) {
    if (!root) return {};
    vector<int> res;
    queue<TreeNode*> q;
    q.push(root);
    while (!q.empty()) {
        int sz = q.size();
        for (int i = 0; i < sz; i++) {
            TreeNode* node = q.front(); q.pop();
            if (i == sz - 1) res.push_back(node->val);
            if (node->left)  q.push(node->left);
            if (node->right) q.push(node->right);
        }
    }
    return res;
}

// Zigzag level order: alternate left→right and right→left
vector<vector<int>> zigzagOrder(TreeNode* root) {
    if (!root) return {};
    vector<vector<int>> res;
    queue<TreeNode*> q;
    q.push(root);
    bool leftToRight = true;
    while (!q.empty()) {
        int sz = q.size();
        deque<int> level;
        for (int i = 0; i < sz; i++) {
            TreeNode* node = q.front(); q.pop();
            if (leftToRight) level.push_back(node->val);
            else             level.push_front(node->val);
            if (node->left)  q.push(node->left);
            if (node->right) q.push(node->right);
        }
        res.push_back(vector<int>(level.begin(), level.end()));
        leftToRight = !leftToRight;
    }
    return res;
}


// ─────────────────────────────────────────────────────────────────────────
// 4. Height / Depth / Balance
//    MENTAL MODEL: "height = 1 + max(left, right); balanced iff |left-right| <= 1 for ALL nodes"
// ─────────────────────────────────────────────────────────────────────────
int height(TreeNode* root) {
    if (!root) return 0;
    return 1 + max(height(root->left), height(root->right));
}

// Returns -1 if unbalanced, else height
int checkBalanced(TreeNode* root) {
    if (!root) return 0;
    int l = checkBalanced(root->left);
    if (l == -1) return -1;
    int r = checkBalanced(root->right);
    if (r == -1) return -1;
    if (abs(l - r) > 1) return -1;
    return 1 + max(l, r);
}
bool isBalanced(TreeNode* root) { return checkBalanced(root) != -1; }


// ─────────────────────────────────────────────────────────────────────────
// 5. Validate BST
//    MENTAL MODEL: "pass down (min, max) bounds; every node must be strictly within"
//    Common mistake: checking only local parent comparison. Must propagate bounds.
// ─────────────────────────────────────────────────────────────────────────
bool validateBST(TreeNode* root, long min_val = LONG_MIN, long max_val = LONG_MAX) {
    if (!root) return true;
    if (root->val <= min_val || root->val >= max_val) return false;
    return validateBST(root->left, min_val, root->val) &&
           validateBST(root->right, root->val, max_val);
}


// ─────────────────────────────────────────────────────────────────────────
// 6. LCA — Lowest Common Ancestor
//    MENTAL MODEL (binary tree): "if root matches p or q, root IS the LCA;
//                                 else LCA is on whichever side both are found"
//    MENTAL MODEL (BST): "walk left if both < root, right if both > root, else root IS LCA"
// ─────────────────────────────────────────────────────────────────────────
TreeNode* lca(TreeNode* root, TreeNode* p, TreeNode* q) {
    if (!root || root == p || root == q) return root;
    TreeNode* left  = lca(root->left, p, q);
    TreeNode* right = lca(root->right, p, q);
    if (left && right) return root;   // p and q split across subtrees
    return left ? left : right;
}

TreeNode* lcaBST(TreeNode* root, int p, int q) {
    if (p < root->val && q < root->val) return lcaBST(root->left, p, q);
    if (p > root->val && q > root->val) return lcaBST(root->right, p, q);
    return root;
}


// ─────────────────────────────────────────────────────────────────────────
// 7. Path Sum problems
//    MENTAL MODEL: "carry a running sum down; at leaf, check if it hits target"
//    Max path sum: at each node, decide whether to extend the path or start fresh
// ─────────────────────────────────────────────────────────────────────────
bool hasPathSum(TreeNode* root, int target) {
    if (!root) return false;
    if (!root->left && !root->right) return root->val == target;
    return hasPathSum(root->left, target - root->val) ||
           hasPathSum(root->right, target - root->val);
}

void allPaths(TreeNode* root, int target, vector<int>& path, vector<vector<int>>& res) {
    if (!root) return;
    path.push_back(root->val);
    if (!root->left && !root->right && root->val == target) res.push_back(path);
    allPaths(root->left,  target - root->val, path, res);
    allPaths(root->right, target - root->val, path, res);
    path.pop_back();
}

// Maximum path sum — path can start and end at any node
int maxPathSum(TreeNode* root, int& globalMax) {
    if (!root) return 0;
    int l = max(0, maxPathSum(root->left, globalMax));   // ignore negative gains
    int r = max(0, maxPathSum(root->right, globalMax));
    globalMax = max(globalMax, l + r + root->val);       // path through root
    return max(l, r) + root->val;                        // extend one side to parent
}
int maxPathSum(TreeNode* root) {
    int res = INT_MIN;
    maxPathSum(root, res);
    return res;
}


// ─────────────────────────────────────────────────────────────────────────
// 8. Diameter of Binary Tree
//    MENTAL MODEL: "diameter through a node = left_height + right_height;
//                  track global max; return max(l,r)+1 to parent"
//    Diameter is NOT necessarily through the root.
// ─────────────────────────────────────────────────────────────────────────
int diameterHelper(TreeNode* root, int& diam) {
    if (!root) return 0;
    int l = diameterHelper(root->left, diam);
    int r = diameterHelper(root->right, diam);
    diam = max(diam, l + r);
    return 1 + max(l, r);
}
int diameter(TreeNode* root) { int d = 0; diameterHelper(root, d); return d; }


// ─────────────────────────────────────────────────────────────────────────
// 9. Symmetric Tree
//    MENTAL MODEL: "two pointers mirror each other: left.left ↔ right.right, left.right ↔ right.left"
// ─────────────────────────────────────────────────────────────────────────
bool isMirror(TreeNode* a, TreeNode* b) {
    if (!a && !b) return true;
    if (!a || !b) return false;
    return a->val == b->val && isMirror(a->left, b->right) && isMirror(a->right, b->left);
}
bool isSymmetric(TreeNode* root) { return !root || isMirror(root->left, root->right); }


// -----------------------------------------------------------------------------
// 9b. Invert Binary Tree
//     MENTAL MODEL: "mirror every node by swapping its left and right children"
//     The root stays the same, but every subtree changes sides.
//     Example:       1                 1
//                  /   \     ->      /   \
//                 2     3           3     2
// -----------------------------------------------------------------------------
TreeNode* invertTree(TreeNode* root) {
    // Empty subtree: nothing to invert, so return nullptr.
    if (!root) return nullptr;

    // Swap the two child pointers at the current node.
    swap(root->left, root->right);

    // Recursively invert the children after the swap.
    // Order does not matter because both subtrees must be mirrored.
    invertTree(root->left);
    invertTree(root->right);

    // Return the same root pointer, now representing the inverted tree.
    return root;
}


// ─────────────────────────────────────────────────────────────────────────
// 10. Serialize / Deserialize Binary Tree
//     MENTAL MODEL: "preorder with '#' markers for null → reconstruct from stream"
//     Unique encoding because we record null positions.
//     BFS alternative: use level-order and '-1' for nulls (like LeetCode input).
// ─────────────────────────────────────────────────────────────────────────
string serialize(TreeNode* root) {
    if (!root) return "#,";
    return to_string(root->val) + "," + serialize(root->left) + serialize(root->right);
}

TreeNode* deserializeHelper(istringstream& ss) {
    string token;
    getline(ss, token, ',');
    if (token == "#") return nullptr;
    TreeNode* node = new TreeNode(stoi(token));
    node->left  = deserializeHelper(ss);
    node->right = deserializeHelper(ss);
    return node;
}
TreeNode* deserialize(const string& s) {
    istringstream ss(s);
    return deserializeHelper(ss);
}


// ─────────────────────────────────────────────────────────────────────────
// 11. Kth Smallest in BST
//     MENTAL MODEL: "inorder traversal of BST gives sorted order; stop at kth element"
//     Use a counter; iterative inorder avoids extra vector allocation.
// ─────────────────────────────────────────────────────────────────────────
int kthSmallest(TreeNode* root, int k) {
    stack<TreeNode*> st;
    TreeNode* cur = root;
    while (cur || !st.empty()) {
        while (cur) { st.push(cur); cur = cur->left; }
        cur = st.top(); st.pop();
        if (--k == 0) return cur->val;
        cur = cur->right;
    }
    return -1;
}


// ─────────────────────────────────────────────────────────────────────────
// 12. Flatten Binary Tree to Linked List (in-place, preorder)
//     MENTAL MODEL: "for each node: save right, attach left subtree as right,
//                   append original right at the tail of the new right chain"
//     Result: all nodes form a right-skewed tree in preorder.
// ─────────────────────────────────────────────────────────────────────────
void flatten(TreeNode* root) {
    TreeNode* cur = root;
    while (cur) {
        if (cur->left) {
            TreeNode* rightmost = cur->left;
            while (rightmost->right) rightmost = rightmost->right;  // find tail of left subtree
            rightmost->right = cur->right;   // attach original right to tail
            cur->right = cur->left;          // promote left subtree to right
            cur->left  = nullptr;
        }
        cur = cur->right;
    }
}


// ─────────────────────────────────────────────────────────────────────────
// 13. Count Nodes in Complete Binary Tree  [O(log^2 n)]
//     MENTAL MODEL: "if left_depth == right_depth → left subtree is full → 2^d - 1 + recurse right"
//     Complete tree: all levels full except last; last level filled from left.
//     Key: reach leaf by always going left or always going right → O(log n) depth check.
// ─────────────────────────────────────────────────────────────────────────
int countNodes(TreeNode* root) {
    if (!root) return 0;
    int l = 0, r = 0;
    TreeNode* left = root, *right = root;
    while (left)  { l++; left  = left->left; }
    while (right) { r++; right = right->right; }
    if (l == r) return (1 << l) - 1;  // perfect subtree: 2^l - 1 nodes
    return 1 + countNodes(root->left) + countNodes(root->right);
}


// ─────────────────────────────────────────────────────────────────────────
// Helper: print inorder
void printVec(const vector<int>& v) {
    for (int x : v) cout << x << " ";
    cout << endl;
}

int main() {
    // BST
    cout << "=== BST ===" << endl;
    TreeNode* bst = nullptr;
    for (int v : {5,3,7,1,4,6,8}) bst = bstInsert(bst, v);
    vector<int> res;
    inorder(bst, res);
    cout << "Inorder: "; printVec(res);           // 1 3 4 5 6 7 8
    cout << "Search 4: " << boolalpha << bstSearch(bst, 4) << endl;  // true
    bst = bstDelete(bst, 3);
    res.clear(); inorder(bst, res);
    cout << "After delete 3: "; printVec(res);    // 1 4 5 6 7 8

    // Traversals
    cout << "\n=== Traversals ===" << endl;
    vector<int> a{1,2,3,4,5,6,7};
    TreeNode* tree = build(a);
    auto io = inorderIter(tree); cout << "Inorder iter: "; printVec(io);
    auto po = preorderIter(tree); cout << "Preorder iter: "; printVec(po);

    // Level order / right view
    cout << "\n=== Level Order ===" << endl;
    auto lo = levelOrder(tree);
    for (auto& level : lo) { printVec(level); }
    cout << "Right view: "; printVec(rightSideView(tree));

    // Height, balance, validate
    cout << "\n=== Properties ===" << endl;
    cout << "Height: " << height(tree) << endl;
    cout << "Balanced: " << isBalanced(tree) << endl;
    cout << "Valid BST: " << validateBST(bst) << endl;

    // LCA
    cout << "\n=== LCA ===" << endl;
    TreeNode* p = bst->left;       // node 4
    TreeNode* q = bst->right;      // node 7
    cout << "LCA of 4,7 in BST: " << lcaBST(bst, 4, 7)->val << endl; // 5

    // Path sum
    cout << "\n=== Path Sum ===" << endl;
    vector<int> b{5,4,8,11,-1,13,4,7,2,-1,-1,-1,1};
    TreeNode* pt = build(b);
    cout << "HasPathSum(22): " << hasPathSum(pt, 22) << endl; // true
    cout << "MaxPathSum: " << maxPathSum(tree) << endl;       // 1+3+7=11? actually 4+2+1+3+7=17? depends on tree

    // Diameter & symmetric
    cout << "\n=== Diameter & Symmetric ===" << endl;
    cout << "Diameter: " << diameter(tree) << endl;
    cout << "Symmetric: " << isSymmetric(tree) << endl;  // perfect tree is symmetric

    // Invert binary tree
    cout << "\n=== Invert Binary Tree ===" << endl;
    vector<int> invInput{1,2,3,4,5,6,7};
    TreeNode* inv = build(invInput);
    invertTree(inv);
    cout << "Level order after invert:" << endl;
    for (auto& level : levelOrder(inv)) { printVec(level); }  // 1 / 3 2 / 7 6 5 4

    // Serialize/Deserialize
    cout << "\n=== Serialize/Deserialize ===" << endl;
    string s = serialize(tree);
    cout << "Serialized (prefix): " << s.substr(0, 30) << "..." << endl;
    TreeNode* t2 = deserialize(s);
    vector<int> r2; inorder(t2, r2);
    cout << "Deserialized inorder: "; printVec(r2);

    // Kth smallest in BST
    cout << "\n=== Kth Smallest (BST) ===" << endl;
    cout << "2nd smallest: " << kthSmallest(bst, 2) << endl;  // 4 (after deleting 3)

    // Flatten
    cout << "\n=== Flatten to Linked List ===" << endl;
    vector<int> c{1,2,5,3,4,-1,6};
    TreeNode* ft = build(c);
    flatten(ft);
    cout << "Flattened (right chain): ";
    for (TreeNode* n = ft; n; n = n->right) cout << n->val << " ";
    cout << endl;  // 1 2 3 4 5 6

    // Count nodes complete tree
    cout << "\n=== Count Complete Tree Nodes ===" << endl;
    cout << "Nodes in 7-node perfect tree: " << countNodes(tree) << endl; // 7

    return 0;
}
