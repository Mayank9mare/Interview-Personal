import java.util.*;
import java.util.concurrent.*;

public class DataStructures {

    public static void main(String[] args) {
        arrayDemo();
        arrayListDemo();
        linkedListDemo();
        stackDemo();
        queueDemo();
        dequeDemo();
        priorityQueueDemo();
        hashMapDemo();
        linkedHashMapDemo();
        treeMapDemo();
        hashSetDemo();
        linkedHashSetDemo();
        treeSetDemo();
        arrayDequeDemo();
        binaryTreeDemo();
        bstDemo();
        trieDemo();
        graphBfsDemo();
        graphDfsDemo();
        sortingDemo();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Array  (fixed size)
    // Access   O(1)
    // Search   O(n)
    // Insert   O(n)  shift elements
    // Delete   O(n)  shift elements
    // Space    O(n)
    // ─────────────────────────────────────────────────────────────────────────
    static void arrayDemo() {
        int[] arr = new int[5];
        arr[0] = 10;                          // set   O(1)
        int x = arr[0];                       // get   O(1)
        Arrays.sort(arr);                     // sort  O(n log n)
        int idx = Arrays.binarySearch(arr, 10); // binary search (sorted) O(log n)
        int[] copy = Arrays.copyOf(arr, arr.length); // copy O(n)
        System.out.println("Array get: " + x);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ArrayList  (dynamic array)
    // Access      O(1)
    // Search      O(n)
    // Add (end)   O(1) amortized
    // Add (mid)   O(n)  shift
    // Remove      O(n)  shift
    // Contains    O(n)
    // ─────────────────────────────────────────────────────────────────────────
    static void arrayListDemo() {
        List<Integer> list = new ArrayList<>();
        list.add(1);                          // add end     O(1) amortized
        list.add(0, 99);                      // add at idx  O(n)
        list.get(0);                          // access      O(1)
        list.set(0, 100);                     // update      O(1)
        list.remove(Integer.valueOf(100));    // remove val  O(n)
        list.remove(0);                       // remove idx  O(n)
        list.contains(1);                     // search      O(n)
        Collections.sort(list);              // sort        O(n log n)
        System.out.println("ArrayList: " + list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LinkedList  (doubly linked)
    // Access         O(n)
    // Add (head/tail) O(1)
    // Add (mid)      O(n)  find position first
    // Remove (head/tail) O(1)
    // Remove (mid)   O(n)
    // Contains       O(n)
    // ─────────────────────────────────────────────────────────────────────────
    static void linkedListDemo() {
        LinkedList<Integer> ll = new LinkedList<>();
        ll.addFirst(1);                       // add head    O(1)
        ll.addLast(2);                        // add tail    O(1)
        ll.add(1, 99);                        // add at idx  O(n)
        ll.getFirst();                        // peek head   O(1)
        ll.getLast();                         // peek tail   O(1)
        ll.get(1);                            // access idx  O(n)
        ll.removeFirst();                     // remove head O(1)
        ll.removeLast();                      // remove tail O(1)
        System.out.println("LinkedList: " + ll);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Stack  (LIFO)
    // Push   O(1)
    // Pop    O(1)
    // Peek   O(1)
    // Search O(n)
    //
    // Prefer Deque<> over Stack<> (Stack is legacy + synchronized)
    // ─────────────────────────────────────────────────────────────────────────
    static void stackDemo() {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);                        // push  O(1)
        stack.push(2);
        stack.push(3);
        stack.peek();                         // peek  O(1)  does not remove
        stack.pop();                          // pop   O(1)
        stack.isEmpty();                      // empty O(1)
        System.out.println("Stack top: " + stack.peek());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Queue  (FIFO)
    // Offer (enqueue)  O(1)
    // Poll  (dequeue)  O(1)
    // Peek             O(1)
    //
    // Prefer ArrayDeque over LinkedList for Queue
    // ─────────────────────────────────────────────────────────────────────────
    static void queueDemo() {
        Queue<Integer> q = new ArrayDeque<>();
        q.offer(1);                           // enqueue  O(1)
        q.offer(2);
        q.offer(3);
        q.peek();                             // front, no remove  O(1)
        q.poll();                             // dequeue           O(1)
        q.isEmpty();                          // O(1)
        System.out.println("Queue front: " + q.peek());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Deque  (double-ended queue — stack + queue combined)
    // addFirst / addLast   O(1)
    // removeFirst / removeLast  O(1)
    // peekFirst / peekLast O(1)
    // ─────────────────────────────────────────────────────────────────────────
    static void dequeDemo() {
        Deque<Integer> dq = new ArrayDeque<>();
        dq.addFirst(1);                       // add front  O(1)
        dq.addLast(2);                        // add back   O(1)
        dq.addFirst(0);
        dq.peekFirst();                       // peek front O(1)
        dq.peekLast();                        // peek back  O(1)
        dq.removeFirst();                     // remove front O(1)
        dq.removeLast();                      // remove back  O(1)
        System.out.println("Deque: " + dq);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PriorityQueue  (min-heap by default)
    // Offer (insert)   O(log n)
    // Poll  (min)      O(log n)
    // Peek  (min)      O(1)
    // Contains         O(n)
    // Remove arbitrary O(n)
    // ─────────────────────────────────────────────────────────────────────────
    static void priorityQueueDemo() {
        // min-heap
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.offer(5);                     // insert  O(log n)
        minHeap.offer(1);
        minHeap.offer(3);
        minHeap.peek();                       // min     O(1)
        minHeap.poll();                       // remove min  O(log n)

        // max-heap
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        maxHeap.offer(5);
        maxHeap.offer(1);
        maxHeap.offer(3);
        System.out.println("Min-heap peek: " + minHeap.peek()); // 3
        System.out.println("Max-heap peek: " + maxHeap.peek()); // 5

        // custom comparator (e.g. sort by second element of int[])
        PriorityQueue<int[]> custom = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        custom.offer(new int[]{1, 10});
        custom.offer(new int[]{2, 5});
        System.out.println("Custom heap min val: " + custom.peek()[1]); // 5
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HashMap  (key → value, unordered)
    // Put      O(1) average
    // Get      O(1) average
    // Remove   O(1) average
    // Contains O(1) average
    // Iteration O(n)
    // Worst case O(n) on hash collision
    // ─────────────────────────────────────────────────────────────────────────
    static void hashMapDemo() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);                      // insert      O(1)
        map.put("b", 2);
        map.get("a");                         // lookup      O(1)
        map.getOrDefault("z", 0);            // safe lookup O(1)
        map.containsKey("a");                 // O(1)
        map.containsValue(2);                 // O(n)
        map.remove("b");                      // O(1)
        map.putIfAbsent("c", 3);             // O(1)
        map.merge("a", 10, Integer::sum);    // add 10 to existing value
        map.getOrDefault("a", 0);
        for (Map.Entry<String, Integer> e : map.entrySet()) { // iterate O(n)
            String k = e.getKey(); int v = e.getValue();
        }
        System.out.println("HashMap: " + map);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LinkedHashMap  (insertion-order or access-order HashMap)
    // All operations same as HashMap: O(1)
    // Iteration preserves insertion order
    // Use for: LRU cache (accessOrder=true), ordered map
    // ─────────────────────────────────────────────────────────────────────────
    static void linkedHashMapDemo() {
        // insertion-order
        Map<String, Integer> lhm = new LinkedHashMap<>();
        lhm.put("b", 2);
        lhm.put("a", 1);
        lhm.put("c", 3);
        System.out.println("LinkedHashMap (insertion order): " + lhm); // b, a, c

        // access-order — LRU cache pattern
        Map<Integer, Integer> lruCache = new LinkedHashMap<>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> e) {
                return size() > 3; // evict when size exceeds 3
            }
        };
        lruCache.put(1, 1);
        lruCache.put(2, 2);
        lruCache.put(3, 3);
        lruCache.get(1);      // 1 becomes most recently used
        lruCache.put(4, 4);   // evicts LRU (key 2)
        System.out.println("LRU cache: " + lruCache); // {3,1,4}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TreeMap  (sorted by key, Red-Black tree)
    // Put      O(log n)
    // Get      O(log n)
    // Remove   O(log n)
    // Min/Max  O(log n)
    // Floor/Ceiling O(log n)
    // ─────────────────────────────────────────────────────────────────────────
    static void treeMapDemo() {
        TreeMap<Integer, String> tm = new TreeMap<>();
        tm.put(5, "five");                    // O(log n)
        tm.put(2, "two");
        tm.put(8, "eight");
        tm.put(1, "one");
        tm.firstKey();                        // min key  O(log n)
        tm.lastKey();                         // max key  O(log n)
        tm.floorKey(6);                       // largest key <= 6  O(log n)
        tm.ceilingKey(6);                     // smallest key >= 6 O(log n)
        tm.higherKey(5);                      // strictly > 5      O(log n)
        tm.lowerKey(5);                       // strictly < 5      O(log n)
        tm.subMap(2, 8);                      // keys in [2, 8)    O(log n)
        tm.headMap(5);                        // keys < 5
        tm.tailMap(5);                        // keys >= 5
        System.out.println("TreeMap: " + tm);
        System.out.println("Floor of 6: " + tm.floorKey(6)); // 5
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HashSet  (unordered unique elements)
    // Add      O(1) average
    // Remove   O(1) average
    // Contains O(1) average
    // ─────────────────────────────────────────────────────────────────────────
    static void hashSetDemo() {
        Set<String> set = new HashSet<>();
        set.add("a");                         // O(1)
        set.add("b");
        set.add("a");                         // duplicate ignored
        set.contains("a");                    // O(1)
        set.remove("b");                      // O(1)
        set.size();                           // O(1)
        System.out.println("HashSet: " + set);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LinkedHashSet  (insertion-order HashSet)
    // All operations same as HashSet: O(1)
    // Iteration preserves insertion order
    // ─────────────────────────────────────────────────────────────────────────
    static void linkedHashSetDemo() {
        Set<String> lhs = new LinkedHashSet<>();
        lhs.add("c");
        lhs.add("a");
        lhs.add("b");
        System.out.println("LinkedHashSet (insertion order): " + lhs); // c, a, b
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TreeSet  (sorted unique elements, Red-Black tree)
    // Add      O(log n)
    // Remove   O(log n)
    // Contains O(log n)
    // Min/Max  O(log n)
    // Floor/Ceiling O(log n)
    // ─────────────────────────────────────────────────────────────────────────
    static void treeSetDemo() {
        TreeSet<Integer> ts = new TreeSet<>();
        ts.add(5);                            // O(log n)
        ts.add(2);
        ts.add(8);
        ts.add(1);
        ts.first();                           // min  O(log n)
        ts.last();                            // max  O(log n)
        ts.floor(6);                          // largest <= 6   O(log n)
        ts.ceiling(6);                        // smallest >= 6  O(log n)
        ts.higher(5);                         // strictly > 5   O(log n)
        ts.lower(5);                          // strictly < 5   O(log n)
        ts.subSet(2, 8);                      // [2, 8)  O(log n)
        ts.headSet(5);                        // < 5
        ts.tailSet(5);                        // >= 5
        System.out.println("TreeSet: " + ts);
        System.out.println("Floor of 6: " + ts.floor(6)); // 5
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ArrayDeque  (resizable array deque — fastest stack and queue)
    // addFirst / addLast   O(1) amortized
    // removeFirst / removeLast O(1)
    // peekFirst / peekLast O(1)
    // No null elements allowed
    // ─────────────────────────────────────────────────────────────────────────
    static void arrayDequeDemo() {
        ArrayDeque<Integer> dq = new ArrayDeque<>();

        // used as stack (LIFO)
        dq.push(1);                           // addFirst  O(1)
        dq.push(2);
        dq.pop();                             // removeFirst O(1)

        // used as queue (FIFO)
        dq.offer(10);                         // addLast   O(1)
        dq.offer(20);
        dq.poll();                            // removeFirst O(1)

        // used as sliding window
        dq.addLast(100);
        dq.addLast(200);
        dq.peekFirst();                       // O(1)
        dq.peekLast();                        // O(1)
        dq.removeFirst();
        dq.removeLast();
        System.out.println("ArrayDeque: " + dq);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Binary Tree  (generic, not sorted)
    // Insert (level-order)  O(n)
    // Search                O(n)
    // Height                O(n)
    // BFS traversal         O(n)
    // DFS traversal         O(n)  (inorder / preorder / postorder)
    // Space                 O(n)
    // ─────────────────────────────────────────────────────────────────────────
    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    static TreeNode buildTree(int[] vals) {
        if (vals.length == 0) return null;
        TreeNode root = new TreeNode(vals[0]);
        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        int i = 1;
        while (i < vals.length) {
            TreeNode node = q.poll();
            if (i < vals.length) { node.left  = new TreeNode(vals[i++]); q.offer(node.left);  }
            if (i < vals.length) { node.right = new TreeNode(vals[i++]); q.offer(node.right); }
        }
        return root;
    }

    static void binaryTreeDemo() {
        //        1
        //       / \
        //      2   3
        //     / \   \
        //    4   5   6
        TreeNode root = buildTree(new int[]{1, 2, 3, 4, 5, 6});

        // BFS (level-order) — O(n)
        List<List<Integer>> levels = new ArrayList<>();
        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int size = q.size();
            List<Integer> level = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                level.add(node.val);
                if (node.left  != null) q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
            levels.add(level);
        }
        System.out.println("BFS levels: " + levels); // [[1],[2,3],[4,5,6]]

        // DFS inorder (left → root → right) — O(n)  gives sorted order in BST
        List<Integer> inorder = new ArrayList<>();
        inorderDfs(root, inorder);
        System.out.println("Inorder: " + inorder);   // [4,2,5,1,3,6]

        // DFS preorder (root → left → right) — O(n)  useful for copying tree
        List<Integer> preorder = new ArrayList<>();
        preorderDfs(root, preorder);
        System.out.println("Preorder: " + preorder); // [1,2,4,5,3,6]

        // DFS postorder (left → right → root) — O(n)  useful for deleting tree
        List<Integer> postorder = new ArrayList<>();
        postorderDfs(root, postorder);
        System.out.println("Postorder: " + postorder); // [4,5,2,6,3,1]

        // Height — O(n)
        System.out.println("Height: " + height(root)); // 3
    }

    static void inorderDfs(TreeNode node, List<Integer> res) {
        if (node == null) return;
        inorderDfs(node.left, res);
        res.add(node.val);
        inorderDfs(node.right, res);
    }
    static void preorderDfs(TreeNode node, List<Integer> res) {
        if (node == null) return;
        res.add(node.val);
        preorderDfs(node.left, res);
        preorderDfs(node.right, res);
    }
    static void postorderDfs(TreeNode node, List<Integer> res) {
        if (node == null) return;
        postorderDfs(node.left, res);
        postorderDfs(node.right, res);
        res.add(node.val);
    }
    static int height(TreeNode node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Binary Search Tree  (left < root < right)
    // Insert    O(log n) average  O(n) worst (skewed)
    // Search    O(log n) average  O(n) worst
    // Delete    O(log n) average  O(n) worst
    // Min/Max   O(log n) average
    // Inorder   O(n)  — yields sorted sequence
    // ─────────────────────────────────────────────────────────────────────────
    static class BST {
        TreeNode root;

        void insert(int val) { root = insertRec(root, val); }  // O(log n) avg
        private TreeNode insertRec(TreeNode node, int val) {
            if (node == null) return new TreeNode(val);
            if (val < node.val) node.left  = insertRec(node.left,  val);
            else if (val > node.val) node.right = insertRec(node.right, val);
            return node;
        }

        boolean search(int val) { return searchRec(root, val); } // O(log n) avg
        private boolean searchRec(TreeNode node, int val) {
            if (node == null) return false;
            if (val == node.val) return true;
            return val < node.val ? searchRec(node.left, val) : searchRec(node.right, val);
        }

        void delete(int val) { root = deleteRec(root, val); }   // O(log n) avg
        private TreeNode deleteRec(TreeNode node, int val) {
            if (node == null) return null;
            if (val < node.val) { node.left  = deleteRec(node.left,  val); }
            else if (val > node.val) { node.right = deleteRec(node.right, val); }
            else {
                if (node.left  == null) return node.right; // no left child
                if (node.right == null) return node.left;  // no right child
                // two children: replace with inorder successor (min of right subtree)
                TreeNode succ = node.right;
                while (succ.left != null) succ = succ.left;
                node.val = succ.val;
                node.right = deleteRec(node.right, succ.val);
            }
            return node;
        }

        int min() { TreeNode n = root; while (n.left  != null) n = n.left;  return n.val; } // O(log n)
        int max() { TreeNode n = root; while (n.right != null) n = n.right; return n.val; } // O(log n)

        List<Integer> inorder() { List<Integer> r = new ArrayList<>(); inorderDfs(root, r); return r; }
    }

    static void bstDemo() {
        BST bst = new BST();
        bst.insert(5); bst.insert(3); bst.insert(7);
        bst.insert(1); bst.insert(4); bst.insert(6); bst.insert(8);
        System.out.println("BST inorder: " + bst.inorder()); // [1,3,4,5,6,7,8]
        System.out.println("BST search 4: " + bst.search(4)); // true
        System.out.println("BST min: " + bst.min() + "  max: " + bst.max()); // 1  8
        bst.delete(3);
        System.out.println("BST after delete 3: " + bst.inorder()); // [1,4,5,6,7,8]
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Trie  (prefix tree)
    // Insert    O(m)  m = word length
    // Search    O(m)
    // StartsWith O(m)
    // Space     O(alphabet * m * n)
    // ─────────────────────────────────────────────────────────────────────────
    static class Trie {
        private static class TrieNode {
            TrieNode[] children = new TrieNode[26];
            boolean isEnd;
        }
        private final TrieNode root = new TrieNode();

        void insert(String word) {                          // O(m)
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                int i = c - 'a';
                if (node.children[i] == null) node.children[i] = new TrieNode();
                node = node.children[i];
            }
            node.isEnd = true;
        }

        boolean search(String word) {                       // O(m)
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                int i = c - 'a';
                if (node.children[i] == null) return false;
                node = node.children[i];
            }
            return node.isEnd;
        }

        boolean startsWith(String prefix) {                 // O(m)
            TrieNode node = root;
            for (char c : prefix.toCharArray()) {
                int i = c - 'a';
                if (node.children[i] == null) return false;
                node = node.children[i];
            }
            return true;
        }
    }

    static void trieDemo() {
        Trie trie = new Trie();
        trie.insert("apple");
        trie.insert("app");
        System.out.println("Trie search apple: "      + trie.search("apple"));    // true
        System.out.println("Trie search app: "        + trie.search("app"));      // true
        System.out.println("Trie search ap: "         + trie.search("ap"));       // false
        System.out.println("Trie startsWith ap: "     + trie.startsWith("ap"));   // true
        System.out.println("Trie startsWith ban: "    + trie.startsWith("ban"));  // false
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Graph — Adjacency List
    // Build     O(V + E)
    // BFS       O(V + E)  — shortest path in unweighted graph
    // DFS       O(V + E)  — cycle detection, topological sort, connected components
    // Space     O(V + E)
    // ─────────────────────────────────────────────────────────────────────────
    static Map<Integer, List<Integer>> buildGraph(int[][] edges, boolean directed) {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int[] e : edges) {
            graph.computeIfAbsent(e[0], k -> new ArrayList<>()).add(e[1]);
            if (!directed)
                graph.computeIfAbsent(e[1], k -> new ArrayList<>()).add(e[0]);
        }
        return graph;
    }

    static void graphBfsDemo() {
        // Undirected graph:
        // 0 -- 1 -- 3
        // |    |
        // 2    4
        int[][] edges = {{0,1},{0,2},{1,3},{1,4}};
        Map<Integer, List<Integer>> graph = buildGraph(edges, false);

        // BFS from node 0 — O(V + E)
        // Use case: shortest path (unweighted), level-order traversal
        List<Integer> bfsOrder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> q = new ArrayDeque<>();
        q.offer(0); visited.add(0);
        while (!q.isEmpty()) {
            int node = q.poll();
            bfsOrder.add(node);
            for (int neighbor : graph.getOrDefault(node, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    q.offer(neighbor);
                }
            }
        }
        System.out.println("BFS order: " + bfsOrder); // [0,1,2,3,4]

        // BFS shortest path from 0 to 3
        int[] dist = new int[5];
        Arrays.fill(dist, -1);
        dist[0] = 0;
        Queue<Integer> q2 = new ArrayDeque<>();
        q2.offer(0);
        while (!q2.isEmpty()) {
            int node = q2.poll();
            for (int nb : graph.getOrDefault(node, Collections.emptyList())) {
                if (dist[nb] == -1) { dist[nb] = dist[node] + 1; q2.offer(nb); }
            }
        }
        System.out.println("Shortest dist 0→3: " + dist[3]); // 2
    }

    static void graphDfsDemo() {
        // Directed graph (for topological sort):
        // 5 → 2, 5 → 0, 4 → 0, 4 → 1, 2 → 3, 3 → 1
        int[][] edges = {{5,2},{5,0},{4,0},{4,1},{2,3},{3,1}};
        Map<Integer, List<Integer>> graph = buildGraph(edges, true);

        // DFS iterative — O(V + E)
        List<Integer> dfsOrder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(5);
        while (!stack.isEmpty()) {
            int node = stack.pop();
            if (visited.contains(node)) continue;
            visited.add(node);
            dfsOrder.add(node);
            for (int nb : graph.getOrDefault(node, Collections.emptyList()))
                if (!visited.contains(nb)) stack.push(nb);
        }
        System.out.println("DFS order from 5: " + dfsOrder);

        // Topological Sort (Kahn's BFS / indegree) — O(V + E)
        // Use case: task scheduling, build order, course prerequisites
        int[] indegree = new int[6];
        for (int[] e : edges) indegree[e[1]]++;
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < 6; i++) if (indegree[i] == 0) q.offer(i);
        List<Integer> topoOrder = new ArrayList<>();
        while (!q.isEmpty()) {
            int node = q.poll();
            topoOrder.add(node);
            for (int nb : graph.getOrDefault(node, Collections.emptyList()))
                if (--indegree[nb] == 0) q.offer(nb);
        }
        System.out.println("Topological order: " + topoOrder); // [4,5,0,2,3,1]

        // Detect cycle in directed graph (DFS with color: 0=unvisited 1=in-stack 2=done)
        int[] color = new int[6];
        boolean[] hasCycle = {false};
        for (int i = 0; i < 6; i++) if (color[i] == 0) dfsCycle(i, graph, color, hasCycle);
        System.out.println("Has cycle: " + hasCycle[0]); // false
    }

    static void dfsCycle(int node, Map<Integer, List<Integer>> graph, int[] color, boolean[] hasCycle) {
        color[node] = 1; // in stack
        for (int nb : graph.getOrDefault(node, Collections.emptyList())) {
            if (color[nb] == 1) { hasCycle[0] = true; return; }
            if (color[nb] == 0) dfsCycle(nb, graph, color, hasCycle);
        }
        color[node] = 2; // done
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sorting
    //
    // Arrays.sort (int[])        — Dual-Pivot Quicksort   O(n log n) avg  O(1) space
    // Arrays.sort (Object[])     — TimSort                O(n log n)      O(n) space
    // Collections.sort           — TimSort                O(n log n)      O(n) space
    //
    // Manual implementations:
    // BubbleSort    O(n²)   O(1)   stable    — teaching only
    // SelectionSort O(n²)   O(1)   unstable  — teaching only
    // InsertionSort O(n²)   O(1)   stable    — good for nearly sorted / small n
    // MergeSort     O(n log n) O(n) stable   — guaranteed, used in TimSort
    // QuickSort     O(n log n) avg O(n²) worst O(log n) space  — fastest in practice
    // ─────────────────────────────────────────────────────────────────────────
    static void sortingDemo() {
        // --- Built-in ---
        int[] arr = {5, 2, 8, 1, 9, 3};
        Arrays.sort(arr);                                         // O(n log n)
        System.out.println("Arrays.sort: " + Arrays.toString(arr));

        int[] arr2 = {5, 2, 8, 1, 9, 3};
        Arrays.sort(arr2, 1, 4);                                  // sort subarray [1,4)
        System.out.println("Partial sort [1,4): " + Arrays.toString(arr2));

        List<Integer> list = new ArrayList<>(Arrays.asList(5,2,8,1,9,3));
        Collections.sort(list);                                   // O(n log n)
        System.out.println("Collections.sort: " + list);

        list.sort((a, b) -> b - a);                               // descending
        System.out.println("Descending: " + list);

        // sort by string length, then lex
        List<String> words = new ArrayList<>(Arrays.asList("banana","fig","apple","kiwi"));
        words.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("Sort by length then lex: " + words);

        // --- Bubble Sort  O(n²) ---
        int[] b = {5,3,8,1,2};
        for (int i = 0; i < b.length - 1; i++)
            for (int j = 0; j < b.length - 1 - i; j++)
                if (b[j] > b[j+1]) { int t=b[j]; b[j]=b[j+1]; b[j+1]=t; }
        System.out.println("BubbleSort: " + Arrays.toString(b));

        // --- Insertion Sort  O(n²), great for nearly sorted ---
        int[] ins = {5,3,8,1,2};
        for (int i = 1; i < ins.length; i++) {
            int key = ins[i], j = i - 1;
            while (j >= 0 && ins[j] > key) { ins[j+1] = ins[j]; j--; }
            ins[j+1] = key;
        }
        System.out.println("InsertionSort: " + Arrays.toString(ins));

        // --- Merge Sort  O(n log n), stable, guaranteed ---
        int[] ms = {5,3,8,1,2};
        mergeSort(ms, 0, ms.length - 1);
        System.out.println("MergeSort: " + Arrays.toString(ms));

        // --- Quick Sort  O(n log n) avg, in-place ---
        int[] qs = {5,3,8,1,2};
        quickSort(qs, 0, qs.length - 1);
        System.out.println("QuickSort: " + Arrays.toString(qs));
    }

    static void mergeSort(int[] arr, int l, int r) {
        if (l >= r) return;
        int mid = l + (r - l) / 2;
        mergeSort(arr, l, mid);
        mergeSort(arr, mid + 1, r);
        merge(arr, l, mid, r);
    }
    static void merge(int[] arr, int l, int mid, int r) {
        int[] tmp = Arrays.copyOfRange(arr, l, r + 1);
        int i = 0, j = mid - l + 1, k = l;
        while (i <= mid - l && j <= r - l)
            arr[k++] = tmp[i] <= tmp[j] ? tmp[i++] : tmp[j++];
        while (i <= mid - l) arr[k++] = tmp[i++];
        while (j <= r  - l) arr[k++] = tmp[j++];
    }

    static void quickSort(int[] arr, int l, int r) {
        if (l >= r) return;
        int pivot = arr[r], i = l - 1;
        for (int j = l; j < r; j++) if (arr[j] <= pivot) { i++; int t=arr[i];arr[i]=arr[j];arr[j]=t; }
        int t=arr[i+1];arr[i+1]=arr[r];arr[r]=t;
        int p = i + 1;
        quickSort(arr, l, p - 1);
        quickSort(arr, p + 1, r);
    }
}
