+# Google Interview Prep — Problem Catalog

Sourced from 130+ real Google interview experience posts (LeetCode discuss, 2021–2024).
Covers L3/L4/L5 onsite + phone screen rounds. Excludes internship posts.

---

## Round Structure (Reminder)

| Round | Format | What to Expect |
|---|---|---|
| Technical Phone Screen (TPS) | 1–2 problems, 45 min | LC Medium; one follow-up always |
| Onsite Coding × 2–3 | 1–2 problems each | Medium → Hard; edge cases + complexity |
| Googleyness | Behavioural | STAR stories: conflict, ambiguity, tradeoffs |
| System Design (L4+) | Whiteboard HLD | Pick one topic + follow distributed extension |

HC (Hiring Committee) sees all feedback. One `No Hire` in a coding round usually blocks offer even if others are `Hire`.

---

## Graphs

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Shortest path in grid with obstacles elimination | 1293 | Hard | L3/L4 onsite (3× appearances) |
| All nodes distance K in binary tree | 863 | Medium | L4 India phone |
| Most stones removed same row/column (Union-Find) | 947 | Medium-Hard | L4 India phone |
| Parallel courses II (DP + bitmask) | 2050 | Hard | L4 Bangalore onsite |
| Find all possible recipes (topological sort) | 2115 | Medium | L5 onsite |
| BFS router broadcast (n routers, signal range d) | — | Medium | Multiple phone screens |
| WiFi router message broadcast (coordinate BFS) | — | Medium | L4 phone screen |
| Farthest distance from multiple source cities (multi-source Dijkstra) | — | Medium | L3 phone screen |
| Shortest routes between nodes in given order | — | Medium | L4 Bangalore phone |
| Graph with type-based adjacency constraints | — | Medium | L3/L4 onsite |
| 3 directly-connected islands with max value sum | — | Medium | L3 onsite |
| Currency arbitrage (Bellman-Ford negative cycle) | — | Hard | Onsite |
| Generic language translator (BFS on translations) | — | Medium | Onsite |
| Flow water to two oceans | 417 | Medium | Onsite |
| BFS grid — consecutive vacant seats for a group | — | Medium | L4 Bangalore (follow-up: Segment Tree) |
| Count overlapping intervals | — | Medium | L4 onsite |
| Balance graph (distribute value to neighbours equally) | — | Medium | L3 Munich phone |
| Directed graph: count nodes reachable from destination | — | Easy-Medium | L4 India onsite |
| Minimum airports to connect all (MST / connected components) | — | Medium | L4 India phone |
| Network Delay Time (Dijkstra on directed weighted graph) | 743 | Medium | L3/L4 phone warmup |
| Cheapest Flights Within K Stops (Bellman-Ford / modified BFS) | 787 | Medium | L4 India onsite |
| Min Cost to Connect All Points (Prim's / Kruskal MST) | 1584 | Medium | L4 phone / 2025 prep |
| Redundant Connection (Union-Find cycle detection) | 684 | Medium | L4 2025 prep |
| Redundant Connection II (directed graph, Union-Find) | 685 | Hard | L5 onsite |
| Is Graph Bipartite? (BFS 2-coloring) | 785 | Medium | L4 screening 2025 |
| Number of Ways to Arrive at Destination (Dijkstra + count DP) | 1976 | Medium | L4 phone 2025 |
| Second Minimum Time to Reach Destination (BFS + multi-state) | 2045 | Hard | L4 California onsite 2025 |

**Key follow-up pattern:** Start with BFS/DFS → interviewer asks to handle weighted edges (Dijkstra) → then handle multiple sources → then handle queries efficiently (Segment Tree / binary search).

---

## Trees (Binary + N-ary)

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Find leaves of binary tree | 366 | Medium | L4 Bangalore (Strong Hire) |
| Leaf-similar trees | 872 | Easy | L4 India onsite |
| Count nodes equal to average of subtree | 2265 | Medium | L4 Bangalore extra TPS |
| All nodes distance K in binary tree | 863 | Medium | L4 India phone |
| House Robber III | 337 | Medium | 2024 prep |
| Step-by-step directions node to node | 2096 | Medium | L3/L4 Bangalore |
| Operations on tree (lock/unlock, n-ary) | 1666 | Medium | L3 Bangalore |
| Build tree from parent-child pairs | — | Medium | Onsite |
| Number of islands in a tree (DFS on tree) | — | Medium | L4 onsite, phone screen |
| N-ary tree stored as array; remove leaf / subtree | — | Medium | L4 India onsite |
| Huffman decoding tree construction | — | Medium | L5 phone screen |
| Leaf nodes with maximum root-to-leaf path value | — | Easy | L3 onsite |
| Card suit → folder to path (tree + HashMap) | — | Medium | L3 phone screen (Gmail Labels) |
| Compress file/folder selection (tree DFS) | — | Medium | Phone screen (2× appearances) |
| Product category + region coupon compatibility tree | — | Medium | L4 onsite |
| Tree operations at same levels (BFS + min-heap) | — | Medium | L5 onsite |
| Find Nodes problem (tree traversal) | — | Medium | L5 onsite |
| Longest valid word (word ladder DP on dictionary) | — | Medium | L3 onsite |
| Binary tree leaf values with max path to root | — | Easy | L3 onsite |
| Binary Tree Maximum Path Sum (any-to-any path) | 124 | Hard | L4/L5 India onsite |
| Serialize and Deserialize Binary Tree | 297 | Hard | L4/L5 onsite |
| Delete Nodes and Return Forest | 1110 | Medium | L4 phone 2025 |
| Maximum Width of Binary Tree (BFS with indexed positions) | 662 | Medium | L4 India onsite 2025 |
| Minimum Time to Collect All Apples in a Tree | 1443 | Medium | L4 2025 prep |
| Diameter of Binary Tree | 543 | Easy-Med | L3/L4 warmup |
| Binary Tree Cameras (greedy post-order) | 968 | Hard | L5 onsite 2025 |

**Pattern:** Google loves tree problems with interesting constraints — leaf operations, level-by-level processing, and tree → interval transformations.

---

## Dynamic Programming

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Coin Change II (count ways) | 518 | Medium | L4 India phone |
| Coin change variant — recover coins from output array | — | Hard | L4 onsite |
| Jump game max score variant (O(n²) → O(n)) | — | Hard | L4 onsite |
| Burst Balloons | 312 | Hard | Onsite |
| Dungeon Game | 174 | Hard | 2024 prep |
| Apartment hunting (2-pass DP) | — | Hard | Onsite |
| Elevator DP (knapsack variant) | — | Hard | Onsite |
| Tiling 4×N area with 4×1 tiles | 790-like | Medium | Phone screen |
| Maximum earnings switching two cities | — | Medium | Phone screen |
| Koko eating bananas | 875 | Medium | L3/L4 Bangalore phone |
| Longest string chain | 1048 | Medium | L4 California onsite |
| Race Car | 818 | Hard | Phone screen (2× appearances) |
| Triplets with equal distinct chars (bitmask DP) | — | Medium | 2024 experience |
| 12hr time conversion → coin change follow-up | — | Medium | L3 telephonic |
| Sum of all subarrays forming AP | — | Medium | L4 India onsite |
| 24 Game | 679 | Hard | L4 US onsite |
| Strobogrammatic Number II | 247 | Medium | L4 US onsite |
| Longest Increasing Subsequence (also O(n log n) patience sort) | 300 | Medium | L4 India phone |
| Longest Common Subsequence | 1143 | Medium | L4 2025 prep |
| Edit Distance | 72 | Medium | L4 US onsite |
| Word Break (DP + Trie) | 139 | Medium | L4 phone screen |
| Word Break II (memo backtracking, return all sentences) | 140 | Hard | L4/L5 onsite |
| Minimum Difficulty of a Job Schedule (DP + interval) | 1335 | Hard | L5 onsite 2025 |
| Minimum Cost to Cut a Stick (interval DP) | 1547 | Hard | L5 onsite |
| Palindrome Partitioning II (min cuts DP) | 132 | Hard | L4/L5 onsite |

---

## Heap / Priority Queue

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Kth largest element in array | 215 | Medium | L4 India phone |
| Kth largest in a stream | — | Medium | L4 India onsite |
| Find median from data stream | 295 | Medium | 2024 prep |
| Meeting Rooms III | 2402 | Hard | L4 Bangalore onsite (missed edge case), L4 India onsite |
| Process tasks using servers | 1882 | Medium | L4 India onsite, phone screen |
| Patient queue (allocate min-id available room) | — | Medium | Multiple appearances |
| CPU task scheduler | 1834 | Medium | Onsite |
| Time taken to cross the door | 2534 | Hard | L3 phone screen |
| Job sequencing (max profit within deadlines) | 1235-like | Medium | Phone screen |
| Insurance counter / time for turn (min-id heap) | — | Medium | Phone screen |
| BFS + min-heap combination | — | Medium | L5 onsite |
| Merge K Sorted Lists (min-heap) | 23 | Hard | L4 India phone |
| The Skyline Problem (heap + line sweep) | 218 | Hard | L5 onsite |
| Sliding Window Median (two heaps) | 480 | Hard | L4/L5 phone screen |
| Smallest Range Covering Elements from K Lists | 632 | Hard | L5 onsite 2025 |

---

## Intervals / Line Sweep

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Interval list intersections | 986 | Medium | L4 US phone |
| My Calendar III | 732 | Hard | L4 US onsite |
| My Calendar I | 729 | Medium | 2024 prep |
| Amount of new area painted each day | 2158 | Hard | L4 California onsite |
| Range module | 715 | Hard | L4 India onsite |
| Insert interval | 57 | Medium | L4 India onsite (parking follow-up) |
| Detect squares | 2013 | Medium | 2024 prep |
| Range point overlap query (sorted intervals + binary search) | — | Medium | Phone screen |
| Count overlapping intervals | — | Medium | L4 onsite |
| APK SDK version matching (integer range partitioning) | — | Medium | Phone screen |
| Vertical cake cut not destroying toppings (line sweep) | — | Medium | Phone screen |
| Design calendar (available time slots) | — | Medium | L4 California onsite |
| Summary ranges with K-distance merge | — | Medium | 2024 prep |
| Interval splitting/loading data | — | Medium | L5 phone screen |
| Non-overlapping Intervals (greedy: sort by end, min removals) | 435 | Medium | L4 India onsite |
| Minimum Number of Arrows to Burst Balloons | 452 | Medium | L4 2025 prep |
| Employee Free Time (merge N sorted interval lists) | 759 | Hard | L5 onsite |
| Meeting Scheduler (find common free slot) | 1229 | Medium | L4 phone screen |

---

## Trie / String

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Search autocomplete (Trie with top-k) | — | Medium | Multiple onsite + phone (very common) |
| Trie with fuzzy/prefix autocomplete + thread-safe | — | Medium | L4 Bangalore extra TPS |
| Top K most talkative people in chat log | 692-like | Medium | Phone screen |
| String substitutor with % delimiters | — | Medium | Phone screen (2× appearances) |
| Remove parentheses and simplify algebraic formula | — | Medium | L4 phone screen |
| Arithmetic expression evaluator (add/sub/mul/pow nested) | — | Medium | Phone screen |
| Isomorphic strings variant | 205 | Easy | 2024 prep |
| Balanced parentheses via minimum operations | — | Medium | Phone screen |
| String palindrome concat (find first index) | — | Medium | L4 Bangalore onsite |
| In-place remove all A's and double all B's | — | Medium | L4 Bangalore onsite |
| BK-Tree for edit-distance autocomplete | — | Hard | L4 Bangalore extra TPS |
| MergeDedupingIterator (merge 2 unsorted iterators, dedup) | — | Medium | L4 California onsite |
| Word Search II (Trie + DFS backtracking) | 212 | Hard | L4 phone screen |
| Search Suggestions System (Trie prefix → top-3 sorted) | 1268 | Medium | L4 Bangalore phone |
| Longest Palindromic Substring (expand-around-center / Manacher) | 5 | Medium | L3/L4 warmup |
| Palindromic Substrings (count all) | 647 | Medium | L4 2025 prep |
| Encode and Decode Strings | 271 | Medium | L4 onsite |
| Design Add and Search Words Data Structure (Trie + regex) | 211 | Medium | L4 phone 2025 |

---

## Sliding Window / Binary Search / Monotonic Stack

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Sliding window maximum | 239 | Hard | L4 Warsaw phone |
| Car Fleet | 853 | Medium | 2024 prep |
| Car Fleet II | 1776 | Hard | 2024 prep |
| Count of smaller numbers after self | 315 | Hard | 2024 prep |
| Query sparse bit array (D&C binary search) | — | Hard | 2024 prep |
| 1D array max value pairs (monotonic queue, constrained variant) | — | Medium-Hard | Phone screen |
| Remove common elements from K-window prefix | — | Medium | Phone screen |
| First bad version variant (recursion) | 278-like | Medium | L3 Munich onsite |
| Contained numbers in chunked stream | — | Medium | Phone screen |
| Find indexes of 1s in hidden binary array | — | Hard | Phone screen |
| Minimum Window Substring | 76 | Hard | L4 India onsite (very common) |
| Longest Repeating Character Replacement | 424 | Medium | L4 phone / 2025 prep |
| Permutation in String (anagram in substring) | 567 | Medium | L3/L4 warmup |
| Max Consecutive Ones III (binary flip budget) | 1004 | Medium | L4 phone 2025 |
| Sliding Window Median | 480 | Hard | L4/L5 phone screen |
| Fruit Into Baskets (at most 2 distinct) | 904 | Medium | L3/L4 screening |

---

## Backtracking / Recursion

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| 24 Game | 679 | Hard | L4 US onsite |
| Card game 3-card pattern (backtracking on 12 cards) | — | Medium | L5 onsite |
| Regular expression tree isMatch | — | Hard | Onsite |
| Word search palindrome variant | 79-like | Medium | Onsite |
| Permutations I / II (duplicates) | 46/47 | Medium | L3/L4 onsite |
| Subsets I / II | 78/90 | Medium | L3/L4 onsite |
| Combination Sum I / II | 39/40 | Medium | L3/L4 onsite |
| Palindrome Partitioning (all valid partitions) | 131 | Medium | L4 India onsite 2025 |
| Generate Parentheses | 22 | Medium | L3/L4 warmup |
| Letter Combinations of a Phone Number | 17 | Medium | L3 phone warmup |
| N-Queens | 51 | Hard | L5 onsite |

---

## Design / LLD-lite

| Problem | Category | Difficulty | Seen At |
|---|---|---|---|
| Search data structure (LRU variant: recent searches dedup) | LRU Cache | Medium | L4 Seattle phone |
| LRU Cache | 146 | Medium | L4 India onsite |
| Rate limiter (minute + hour limits, 2-deque) | Design | Medium | L4 India onsite |
| Restaurant waitlist (FIFO + O(log N) seat assignment) | Design | Medium | Multiple (2× strong signal) |
| Design parking lot (canPark/park as insert-interval) | Design | Medium | L4 India onsite |
| Design Tic-Tac-Toe streaming on N×N board | 348 | Medium | L5 onsite |
| User model + friendship recommendation | Social Graph | Medium | L3 onsite |
| Design stock DS (MIN/MAX/current with historical updates) | Design | Hard | L4 India onsite |
| Closest nodes queries in BST | 2476 | Medium | L4 phone |
| Insert Delete GetRandom O(1) | 380 | Medium | L4 India onsite |
| LFU Cache | 460 | Hard | L5 onsite |
| All O'one Data Structure (O(1) inc/dec/getMax/getMin) | 432 | Hard | L5 onsite |
| Design In-Memory File System | 588 | Hard | L5 onsite |
| Flatten Nested List Iterator | 341 | Medium | L4 phone screen |

---

## Arrays / Math / Misc

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| First missing positive | 41 | Hard | Onsite |
| Queue reconstruction by height | 406 | Medium | L5 onsite |
| 1D 2048 game (array simulation) | — | Medium | L4 Bangalore onsite |
| Dice game counting wins | — | Medium | 2024 prep |
| Tournament ranking (topological sort) | — | Medium | 2024 prep |
| Water and jug problem (generalized) | 365 | Medium | L4 US phone |
| Binary matrix row/column flip to all zeros | 2128 | Medium | Phone screen |
| Multi-source BFS (two diseases spreading) | — | Medium | 2024 prep |
| Flatten graph to array | — | Easy | Phone screen |
| Number theory — lucky numbers | — | Medium | L3 Munich onsite |
| Count houses in a circle | — | Medium | L3 Bangalore onsite |
| Paint a line problem | — | Medium | L3 Bangalore onsite |
| Minimum weight to disconnect all leaf nodes (greedy) | — | Medium | Phone screen |
| Flip byte bitmap horizontally (non-byte-aligned) | — | Medium | L5 London onsite |
| Memory safety: implement malloc/free/memmove | — | Hard | L5 London onsite |
| Prefix count in sorted string array (two binary searches) | 2185-like | Medium | L5 London onsite |
| Huffman tree from char:depth map | — | Medium | Phone screen |
| Card suit grouping (counting sort O(n)) | — | Easy | L4 India onsite |
| Median of Two Sorted Arrays (binary search O(log min(m,n))) | 4 | Hard | L4/L5 phone / onsite |
| Product of Array Except Self (prefix + suffix, no division) | 238 | Medium | L3/L4 onsite |
| Subarray Sum Equals K (prefix sum + HashMap) | 560 | Medium | L4 India onsite |
| Trapping Rain Water (two-pointer / monotonic stack) | 42 | Hard | L4 2025 prep |
| Find Peak Element (binary search on unsorted) | 162 | Medium | L4 phone screen |
| Jump Game II (greedy, min jumps) | 45 | Medium | L3/L4 onsite |
| Largest Rectangle in Histogram (monotonic stack) | 84 | Hard | L5 onsite |
| Basic Calculator II (stack-based expression) | 227 | Medium | L4 phone screen |

---

## Most Frequently Appearing Problems (High Priority)

These appeared in 2+ independent reports — highest ROI to prepare:

| Problem | LC# | Times Seen |
|---|---|---|
| Shortest path in grid with obstacles elimination | 1293 | 3× |
| Meeting Rooms III / Process Tasks Using Servers | 2402/1882 | 3× |
| Race Car | 818 | 2× |
| Trie autocomplete (various flavors) | — | 4+ × |
| Restaurant Waitlist design | — | 3× |
| BFS router/WiFi broadcast | — | 3× |
| Patient queue (min-id room assignment) | — | 3× |
| Sliding window maximum | 239 | 2× |
| Huffman tree construction/decoding | — | 2× |
| File/folder compression selection | — | 2× |
| String % substitution | — | 2× |
| Rate limiter (dual-window) | — | 2× |
| LRU Cache or variant | 146 | 2× |
| Minimum Window Substring | 76 | High-freq |
| Merge K Sorted Lists | 23 | High-freq |
| Binary Tree Maximum Path Sum | 124 | High-freq |
| Serialize/Deserialize Binary Tree | 297 | High-freq |
| Word Break II | 140 | High-freq |

---

## Study Priority Order

**Phase 1 — Core Patterns (do these first):**
- Graphs: BFS, multi-source BFS, Dijkstra (LC 743, 787, 1584), Union-Find (LC 684), Bipartite (LC 785)
- Trees: DFS, LCA, max path sum (LC 124), serialize/deserialize (LC 297), leaf ops
- Heap: k-largest/smallest (LC 23, 215, 632), room/server assignment (LC 1882, 2402), skyline (LC 218)
- Trie: insert, prefix search, top-k autocomplete, Word Search II (LC 212)

**Phase 2 — Less Common but High Yield:**
- Intervals: insert/merge (LC 57, 435, 452), sweep (LC 715, 732, 2158), free time (LC 759)
- DP: LIS/LCS/edit distance (LC 300, 1143, 72), word break (LC 139/140), interval DP (LC 1335, 1547)
- Sliding window: min window (LC 76), repeating (LC 424), median (LC 480)
- Backtracking: permutations/subsets/combinations (LC 46/78/39), partitioning (LC 131), N-queens (LC 51)

**Phase 3 — Design rounds (L4+):**
- LRU (LC 146), LFU (LC 460), Insert-Delete-Random (LC 380), All-O'One (LC 432)
- Rate limiter, Trie-backed autocomplete, In-memory file system (LC 588)
- Parking lot, restaurant waitlist, calendar scheduler

---

## Behavioural — Googleyness Themes

Heard across nearly every experience:
- Handling ambiguous or changing requirements mid-project
- Disagreement with manager / different work styles  
- Adapting when a project direction changed
- Handling a situation where you pushed back on an unreasonable request
- How you define success for your work
- A time you influenced without authority

Format: STAR (Situation, Task, Action, Result). Prepare 4–5 strong stories reusable across these themes.
