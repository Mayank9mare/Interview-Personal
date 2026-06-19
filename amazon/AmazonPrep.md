# Amazon Interview Prep — Problem Catalog

Sourced from 92 real Amazon SDE interview experience posts (LeetCode discuss, 2022–2025)
plus the community-compiled "50 posts in 1" collection. Covers SDE1/SDE2/SDE3 OA, phone
screen, and onsite (including Bar Raiser). Excludes LP/system design narration.

---

## Round Structure (Reminder)

| Round | Format | What to Expect |
|---|---|---|
| OA | 2 coding problems, 90 min | LC Medium + Medium-Hard; auto-graded |
| Phone Screen | 1–2 problems, 45–60 min | LC Medium; LP story mixed in |
| Onsite (Loop) | 4–5 rounds, 60 min each | 1 LLD round + 3–4 DSA/coding + 1 LP |
| Bar Raiser | Any round, unannounced | Harder problem than usual; evaluates bar for whole org |

Bar Raiser tends to give LC Hard or an ambiguous problem. One `Strong Hire` from BR can
compensate for a weaker coding round. LP stories are mandatory in every round — prepare
≥1 story per Leadership Principle; Customer Obsession carries highest weight.

---

## Most Repeated Problems (High Priority — 3+ independent posts)

| Problem | LC# | Level | Context |
|---|---|---|---|
| **3-page web visit sequence** — find most frequent 3-page path per user | 1152 | Medium | SDE1 R1, SDE2 R1 (3+×) |
| **Locker System (LLD)** — assign smallest fitting locker, retrieve by code | — | Medium | SDE2 onsite, Bar Raiser (3+×) |
| **Unix File Search / find command (LLD)** — Strategy pattern, filter chain | 1166 | Medium | SDE2 onsite, SDE3 (3+×) |
| **Word Break II variant** — hard string + Trie; Bar Raiser staple | 140 | Hard | SDE2 Bar Raiser (3+×) |
| **Currency exchange / Evaluate Division** — graph BFS/DFS between currencies | 399 | Medium | SDE2 onsite R3 (3+×) |
| **Task Scheduler (Design)** — `add(task)` / `fetch()` by priority; heap-based | ~621 | Medium | SDE2 phone + onsite (3+×) |
| **Delivery centers OA** — given centers+distance d, count suitable locations | — | Medium | OA (3+×; confirmed real Amazon OA) |
| **Remove smallest + replace with sum of neighbors** — maximize final element | — | Medium | OA (3+×; confirmed real Amazon OA) |
| **Top K most popular items in stream** — (event, item, timestamp); sliding window follow-up | ~460 | Medium-Hard | SDE2 onsite (3+×) |
| **String formatter** — replace `{0}`, `{1}` placeholders; throw on invalid index | — | Medium | SDE2 onsite R2 (3+×) |
| **Number of Islands** — follow-up: return size of each cluster | 200 | Medium | SDE2 Bar Raiser (3+×) |
| **Course Schedule II** — Kahn's topological sort | 210 | Medium | SDE1/SDE2 onsite (3+×) |
| **LRU Cache** | 146 | Medium | SDE2 onsite (3+×) |
| **Word Ladder** — BFS shortest word transformation | 127 | Medium | SDE2 Bar Raiser (3+×) |
| **Robot in N×M grid** — right/down only, obstacles, return actual path or -1 | ~63 | Medium | SDE onsite (3+×) |
| **Currency discount in string** — parse `"shirt for $40"`, return with 20% off | — | Easy-Med | SDE onsite (3+×; business-wrapped easy) |
| **Multiply two large integers as strings** | 43 | Hard | SDE onsite (3+×) |
| **Merge Intervals variant** | 56 | Medium | SDE2 onsite (3+×) |
| **House Robber** | 198 | Medium | SDE2 onsite (3+×) |
| **Reorganize String** | 767 | Medium | SDE1 L4 Poland + SDE3 phone (2+×) |
| **Making a Large Island** — flip one 0 to maximize island | 827 | Hard | SDE2 AWS Seattle (2+×) |
| **Time to Burn Binary Tree from a given node** | 2385 | Hard | SDE2 onsite (2+×) |
| **Distance between two nodes with parent pointers** — O(1) space: linked-list intersection trick | ~1650 | Medium | SDE2 onsite, Bar Raiser (2+×) |

---

## Graphs

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Course Schedule II (Kahn's topological sort) | 210 | Medium | SDE1/SDE2 (3+×) |
| Word Ladder | 127 | Medium | SDE2 Bar Raiser (3+×) |
| Word Ladder II | 126 | Hard | SDE2 onsite |
| Number of Islands | 200 | Medium | SDE2 Bar Raiser (3+×) |
| Making a Large Island | 827 | Hard | SDE2 AWS Seattle (2+×) |
| Rotten Oranges (multi-source BFS spread) | 994 | Medium | SDE1 onsite (2+×) |
| Word Search | 79 | Medium | SDE2 onsite (2+×) |
| Pacific Atlantic Water Flow | 417 | Medium | SDE2 Bangalore |
| Find All People With Secret (transitive exposure) | 2092 | Hard | SDE2 Austin/Seattle |
| Currency Exchange / Evaluate Division | 399 | Medium | SDE2 onsite R3 (3+×) |
| Alien Dictionary | 269 | Hard | SDE2/SDE3 onsite |
| Detonate Maximum Bombs | 2101 | Medium | SDE2 Seattle |
| Shortest Path Visiting All Nodes (bitmask DP) | 847 | Hard | SDE1 US Bar Raiser |
| Delivery stations topological sort (business-wrapped) | — | Medium | SDE1 R2 (2+×) |
| Keys and Rooms | 841 | Medium | SDE1 onsite (2+×) |
| Network Delay Time (Dijkstra) | 743 | Medium | SDE2 onsite |
| Robot in N×M grid (obstacles, return actual path) | ~63 | Medium | SDE onsite (3+×) |
| Redundant Connection (Union-Find cycle detection) | 684 | Medium | SDE2 2025 prep |
| Redundant Connection II (directed, Union-Find) | 685 | Hard | SDE3 onsite |
| Is Graph Bipartite? (BFS 2-coloring) | 785 | Medium | SDE2 screening 2025 |
| Cheapest Flights Within K Stops (Bellman-Ford / BFS DP) | 787 | Medium | SDE2 onsite 2025 |
| Min Cost to Connect All Points (Prim's / Kruskal MST) | 1584 | Medium | SDE2 phone 2025 |
| Number of Ways to Arrive at Destination (Dijkstra + count) | 1976 | Medium | SDE2 2025 prep |
| Clone Graph (BFS + HashMap) | 133 | Medium | SDE2 phone warmup |

**Follow-up pattern:** BFS → weighted Dijkstra → precompute all-pairs (Floyd-Warshall).

---

## Trees

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Binary Tree Maximum Path Sum | 124 | Hard | SDE2 R1 (3+×) |
| Time to Burn Binary Tree from a given node | 2385 | Hard | SDE2 onsite (2+×) |
| Maximum Sum of Non-Adjacent Nodes (House Robber III) | 337 | Medium | SDE2 Bar Raiser |
| Largest BST in a Binary Tree | 333/1373 | Hard | SDE1/SDE2 (2+×) |
| Distance between nodes w/ parent pointers | ~1650 | Medium | SDE2 (2+×) |
| Distance between two nodes in binary tree (LCA) | 1740 | Medium | SDE2 R2 |
| Burn tree from node (fire BFS) | 2385 | Hard | SDE2 onsite |
| Zigzag Level Order Traversal | 103 | Medium | SDE1 R1 |
| Right View of Binary Tree | 199 | Medium | SDE2 onsite |
| Maximum Sum Level in Binary Tree | 1161 | Medium | SDE1 R1 |
| Bottom View of Binary Tree | — | Medium | SDE1 R3 (2+×) |
| Print Boundary of Binary Tree | 545 | Medium | SDE1 R1 |
| Diameter of Binary Tree | 543 | Easy | SDE1 phone |
| Path Sum II | 113 | Medium | SDE2 onsite |
| Lowest Common Ancestor (with parent pointers) | 1650 | Medium | SDE2 onsite |
| Delete Node in BST | 450 | Medium | SDE2 OA (2+×) |
| Construct Tree from Inorder + Preorder | 105 | Medium | SDE interview R3 |
| Serialize and Deserialize Binary Tree | 297 | Hard | SDE2/SDE3 onsite |
| Maximum Width of Binary Tree (BFS with indexed positions) | 662 | Medium | SDE2 onsite 2025 |
| Validate BST | 98 | Easy-Med | SDE2 phone warmup |
| Balanced Binary Tree | 110 | Easy | SDE1 phone |
| Minimum Time to Collect All Apples in a Tree | 1443 | Medium | SDE2 2025 prep |
| Binary Tree Cameras (greedy post-order DP) | 968 | Hard | SDE3 onsite 2025 |
| Recover Binary Search Tree (inorder swap detection) | 99 | Medium | SDE2 Bar Raiser 2025 |

---

## Dynamic Programming

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| House Robber | 198 | Medium | SDE2 (3+×) |
| Word Break / Word Break II | 139/140 | Med/Hard | SDE2 Bar Raiser (3+×) |
| Frog Jump | 403 | Hard | SDE2 onsite (2+×) |
| Split Array Largest Sum (binary search on answer) | 410 | Hard | SDE2 onsite |
| Partition Equal Subset Sum | 416 | Medium | SDE1 phone screen |
| Jump Game II | 45 | Medium | SDE2 Seattle R1 |
| Minimum Jump to End | 45 | Medium | SDE interview OA |
| Number of Ways to Reach endPos in k Steps | 2400 | Medium | SDE2 collection |
| Longest Palindromic Subsequence | 516 | Medium | SDE2 collection |
| 4D Matrix DP → optimize to 3D | — | Hard | SDE2 R1/Bar Raiser (2+×) |
| Scheduling: max points staying or travelling between cities | — | Medium-Hard | SDE2 Berlin phone |
| Recursion + memoization (phone screen) | — | Medium-Hard | SDE2 phone (2+×) |
| Longest Increasing Subsequence (O(n log n) binary search variant) | 300 | Medium | SDE2 onsite 2025 |
| Longest Common Subsequence | 1143 | Medium | SDE2 2025 prep |
| Edit Distance | 72 | Medium | SDE2/SDE3 onsite |
| Coin Change | 322 | Medium | SDE1/SDE2 warmup |
| Coin Change II (count ways) | 518 | Medium | SDE2 phone screen |
| Decode Ways | 91 | Medium | SDE2 OA / phone |
| Unique Paths II (obstacles grid) | 63 | Medium | SDE1 R1 |
| Minimum Difficulty of a Job Schedule (interval DP) | 1335 | Hard | SDE3 Bar Raiser 2025 |
| Palindrome Partitioning II (min cuts DP) | 132 | Hard | SDE2 onsite 2025 |
| Jump Game III (BFS/DFS reachability) | 1306 | Medium | SDE2 2025 prep |

---

## Heap / Priority Queue

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Task Scheduler Design (`add`/`fetch` by priority) | ~621 | Medium | SDE2 phone + onsite (3+×) |
| Top K popular items in stream (sliding window follow-up) | ~460 | Medium-Hard | SDE2 onsite (3+×) |
| Top K logs on same date | ~347 | Medium | SDE1 L4 Poland (2+×) |
| Find Median from Data Stream | 295 | Hard | SDE1 R1 |
| Merge N Sorted Lists | 23 | Hard | SDE2 collection |
| LFU Cache | 460 | Hard | SDE2 collection |
| 2D matrix problem with Priority Queue (Bar Raiser) | — | Hard | SDE2 Bar Raiser (2+×) |
| Reorganize String | 767 | Medium | SDE1 L4 Poland + SDE3 (2+×) |
| Minimum platforms / Meeting Rooms II | 253/2402 | Medium | SDE1/SDE2 (2+×) |
| The Skyline Problem (heap + line sweep events) | 218 | Hard | SDE3 onsite 2025 |
| Sliding Window Median (two heaps) | 480 | Hard | SDE2/SDE3 phone screen |
| Smallest Range Covering Elements from K Lists | 632 | Hard | SDE3 Bar Raiser 2025 |
| Kth Largest Element in an Array (quickselect / heap) | 215 | Medium | SDE2 phone warmup |

---

## Arrays / Sliding Window / Prefix Sum

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Delivery centers OA (prefix sum + binary search) | — | Medium | OA (3+×; real OA confirmed) |
| Remove smallest + replace with sum of neighbors | — | Medium | OA (3+×; real OA confirmed) |
| Minimum subarray containing two target series | ~76 | Medium | OA SDE2 Ireland (3+×) |
| Sliding Window Maximum | 239 | Hard | SDE2 collection |
| Maximum Product Subarray | 152 | Medium | OA (2+×) |
| Trapping Rain Water | 42 | Hard | SDE2 onsite (2+×) |
| Product of Array Except Self | 238 | Medium | SDE2 Bangalore (2+×) |
| House Robber | 198 | Medium | SDE2 (see DP) |
| Gas Station | 134 | Medium | SDE2 collection |
| Buy and Sell Stock | 121/122 | Easy-Med | SDE2 collection |
| Merge Intervals | 56 | Medium | SDE2 onsite (3+×) |
| Capacity to Ship Packages in D Days | 1011 | Medium | SDE1 R2 (2+×) |
| Max Width Ramp (monotonic stack) | 962 | Medium | SDE1 onsite (2+×) |
| Missing Element in Sorted Array | 1060 | Medium | SDE2 Seattle R2 |
| Split Array Largest Sum | 410 | Hard | SDE2 onsite |
| Minimum Cost to Merge Sticks | 1167 | Medium | SDE2 India onsite |
| Row with Maximum Ones | 2643 | Easy | SDE2 collection |
| Minimum Window Substring | 76 | Hard | SDE2 onsite 2025 |
| Permutation in String (anagram in window) | 567 | Medium | SDE1/SDE2 warmup |
| Longest Repeating Character Replacement | 424 | Medium | SDE2 OA 2025 |
| Max Consecutive Ones III (binary flip budget) | 1004 | Medium | SDE2 phone 2025 |
| Container With Most Water (two pointers) | 11 | Medium | SDE1/SDE2 warmup |
| Subarray Sum Equals K (prefix sum + HashMap) | 560 | Medium | SDE2 onsite 2025 |
| Median of Two Sorted Arrays | 4 | Hard | SDE3 Bar Raiser |
| 3Sum (two-pointer after sort) | 15 | Medium | SDE2 screening |
| Longest Subarray with Sum K (sliding window) | 325 | Medium | SDE2 phone 2025 |

---

## String

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| 3-page web visit sequence (most frequent) | 1152 | Medium | SDE1 R1, SDE2 R1 (3+×) |
| String formatter (`{0}`, `{1}` → throw on invalid) | — | Medium | SDE2 onsite R2 (3+×) |
| Currency discount in string (parse price, apply 20% off) | — | Easy-Med | SDE onsite (3+×) |
| Multiply two large integers as strings | 43 | Hard | SDE onsite (3+×) |
| Word Break II | 140 | Hard | SDE2 Bar Raiser (3+×) |
| Concatenated Words | 472 | Hard | SDE2 collection |
| All Anagrams in a String | 438 | Medium | SDE2 collection |
| Maximum Frequency Substring of Size N | — | Medium | SDE1 R2 (sliding window + hash) |
| Find All Permutations of a String | 46/47 | Medium | SDE1 L4 Poland R2 |
| Compare Version Numbers | 165 | Medium | SDE2 collection |
| Integer to Roman | 12 | Medium | SDE2 Bar Raiser Seattle (2+×) |
| Reorganize String | 767 | Medium | SDE1 + SDE3 (2+×) |
| Rabin-Karp / rolling hash OA | — | Medium | SDE1 OA (2+×) |
| Convert Postfix Expression to Infix | — | Medium | SDE2 collection |
| Longest Palindromic Substring (expand-around-center) | 5 | Medium | SDE2 onsite 2025 |
| Palindromic Substrings (count all) | 647 | Medium | SDE2 2025 prep |
| Longest Substring Without Repeating Characters | 3 | Medium | SDE1/SDE2 warmup |
| Longest Common Prefix | 14 | Easy | SDE1 phone warmup |
| Implement Trie (Prefix Tree) | 208 | Medium | SDE2 onsite 2025 |

---

## Linked List

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Clone Linked List with Random Pointer | 138 | Medium | SDE1 R1 (interleaving trick for O(1) space) |
| Reverse Linked List II | 92 | Medium | SDE2 collection |
| Remove Consecutive Nodes with Sum Zero | 1171 | Medium | SDE1 R2 |
| Merge Two Sorted LL at Common Nodes (max sum path) | — | Medium | SDE1 Bar Raiser (2+×) |
| K Sorted Linked Lists Max Sum Path (heap follow-up) | — | Hard | SDE1 Bar Raiser follow-up |
| Add Two Numbers (different sizes, negative follow-up) | 2/445 | Medium | SDE2 Bar Raiser |
| DLL insertions + merge sort | — | Medium | SDE1 R2 |
| Linked List Cycle Detection (Floyd's) | 141 | Easy | SDE1 phone (very common) |
| Linked List Cycle II (find entry of cycle) | 142 | Medium | SDE2 phone |
| Reverse Linked List | 206 | Easy | SDE1 phone warmup |
| Reverse Nodes in K-Group | 25 | Hard | SDE2/SDE3 Bar Raiser |
| Merge Two Sorted Linked Lists | 21 | Easy | SDE1 phone warmup |
| Palindrome Linked List | 234 | Easy | SDE1 R1 |
| LRU Cache (DLL + HashMap) — implementation focus | 146 | Medium | SDE2 (already in Design; worth coding separately) |

---

## Binary Search

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Capacity to Ship Packages in D Days | 1011 | Medium | SDE1 R2 (2+×) |
| Split Array Largest Sum | 410 | Hard | SDE2 onsite |
| Missing Element in Sorted Array | 1060 | Medium | SDE2 Seattle |
| Find Single Element in Sorted Array | 540 | Medium | SDE1 R2 |
| IP Range Lookup (binary search on intervals) | ~981 | Medium | SDE2 Bar Raiser Bangalore |
| Search in Rotated Sorted Array | 33 | Medium | SDE2 onsite (2+×) |
| Find First and Last Position of Element in Sorted Array | 34 | Medium | SDE1/SDE2 warmup |
| Koko Eating Bananas (binary search on answer) | 875 | Medium | SDE2 phone 2025 |
| Find Peak Element | 162 | Medium | SDE2 phone screen |
| Find Minimum in Rotated Sorted Array | 153 | Medium | SDE1 R1 |
| Time Based Key-Value Store (binary search on timestamps) | 981 | Medium | SDE2 Bar Raiser |

---

## Design / LLD (Coded in the round)

| Problem | Pattern | Difficulty | Seen At |
|---|---|---|---|
| Unix File Search (`find` command) | Strategy | Medium | SDE2/SDE3 (3+×) |
| Amazon Locker System | Enum queues, HashMap | Medium | SDE2 Bar Raiser (3+×) |
| Parking Lot | EnumMap queues | Medium | SDE2 onsite (2+×) |
| Task Scheduler (priority-based add/fetch) | Heap | Medium | SDE2 phone/onsite (3+×) |
| LRU Cache | DLL + HashMap | Medium | SDE2 onsite (3+×) |
| LFU Cache | Heap/HashMap | Hard | SDE2 collection |
| Insert Delete GetRandom O(1) | HashMap + List | Medium | SDE2 phone screen |
| Design HashMap (custom follow-ups) | Hashing | Medium | SDE2 R1 (2+×) |
| In-memory Key-Value Store | HashMap | Medium | SDE3 Seattle |
| Search Autocomplete System | Trie | Hard | SDE3 Seattle |
| Design File System | Trie/HashMap | Medium | SDE2 onsite |
| Live Sale Discount System | Strategy+Observer | Hard | SDE2 onsite |
| Salary Aggregation by Manager ID | Tree DFS | Medium | SDE2 onsite |
| Track User Visits (first unique, first N unique) | HashMap, OrderedSet | Medium | SDE2 collection |
| Card Game LLD | OOP | Medium | SDE2 onsite |
| Multi-Car Elevator System | OOP | Medium | SDE2 Bar Raiser |
| Page Visit Sequence (rolling hash O(N)) | Rolling Hash | Hard | SDE2 onsite |
| All O'one Data Structure (O(1) inc/dec/getMax/getMin) | DLL + HashMap | Hard | SDE3 onsite 2025 |
| Design In-Memory File System (Trie + OOP) | Trie | Hard | SDE3 Seattle 2025 |
| Flatten Nested List Iterator | Stack | Medium | SDE2 phone screen |
| Time Based Key-Value Store | Binary Search + HashMap | Medium | SDE2 Bar Raiser |

---

## Backtracking

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| All Permutations of a String | 46/47 | Medium | SDE1 L4 Poland R2 |
| Word Break II | 140 | Hard | SDE2 Bar Raiser (3+×) |
| Word Search | 79 | Medium | SDE2 onsite (2+×) |
| Path Sum II | 113 | Medium | SDE2 collection |
| Backtracking Medium (unnamed) | — | Med-Hard | SDE2 Bar Raiser (2+×) |
| Combination Sum I / II | 39/40 | Medium | SDE1/SDE2 warmup |
| Subsets I / II | 78/90 | Medium | SDE1/SDE2 warmup |
| Palindrome Partitioning (all valid partitions) | 131 | Medium | SDE2 onsite 2025 |
| Generate Parentheses | 22 | Medium | SDE1/SDE2 warmup |
| Letter Combinations of a Phone Number | 17 | Medium | SDE1 phone |
| N-Queens | 51 | Hard | SDE3 Bar Raiser |
| Sudoku Solver | 37 | Hard | SDE2/SDE3 onsite 2025 |

---

## Stack

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Max Width Ramp (monotonic stack) | 962 | Medium | SDE1 (2+×) |
| Asteroid Collision | 735 | Medium | SDE2 collection |
| Min Stack | 155 | Medium | SDE2 collection |
| Trapping Rain Water | 42 | Hard | SDE2 onsite (2+×) |
| Implement Queue Using Stack | 232 | Easy | SDE2 (suspicious round) |
| Convert Postfix to Infix | — | Medium | SDE2 collection |
| Largest Rectangle in Histogram (monotonic stack) | 84 | Hard | SDE2/SDE3 onsite 2025 |
| Basic Calculator II (stack-based expression) | 227 | Medium | SDE2 phone screen |
| Next Greater Element I / II | 496/503 | Medium | SDE2 2025 prep |
| Daily Temperatures (monotonic stack) | 739 | Medium | SDE2 phone 2025 |
| Valid Parentheses | 20 | Easy | SDE1 warmup |

---

## OA-Specific Patterns (online assessment only)

Both OA problems always appear; candidates who solve 1.5/2 (partial solution on hard) typically advance.

| Problem | Type | LC Analogy |
|---|---|---|
| Delivery centers (prefix sum + binary search) | Custom Amazon | — |
| Remove smallest + replace with neighbor sum | Custom Amazon | — |
| Minimum subarray containing two target elements | Sliding Window | LC 76 |
| Maximum product subarray variation | DP | LC 152 |
| Minimum parcels to fill truck (set-based) | Greedy | — |
| Shipment Imbalance | Custom Amazon | — |
| Seats Allocation (greedy) | Greedy | — |
| Longest Decreasing Subsequence in Linked List | DP on LL | LC 300 variant |

---

## Study Priority Order

**Phase 1 — Must-solve before interview:**
- Graph: BFS (LC 200, 994, 127, 841), Topological Sort (LC 210), Dijkstra (LC 743, 787), Union-Find (LC 684)
- Tree: DFS post-order (LC 124, 337, 2385), serialize (LC 297), BFS level-order (LC 103, 199, 1161)
- Linked List: Cycle detection (LC 141/142), Reverse (LC 206), Merge sorted (LC 21)
- Design LLD: Locker System, Unix Find, LRU Cache, Task Scheduler
- String: Word Break (LC 139/140), 3-page sequence (LC 1152), Min Window Substring (LC 76)

**Phase 2 — High yield for SDE2:**
- DP: House Robber (LC 198), LIS/LCS/Edit Distance (LC 300/1143/72), Coin Change (LC 322/518), Frog Jump (LC 403)
- Intervals: Merge Intervals (LC 56), Meeting Rooms (LC 253), Non-overlapping (LC 435)
- Heap: Top K stream, LFU Cache (LC 460), Merge N Lists (LC 23), Skyline (LC 218)
- Arrays: Subarray Sum K (LC 560), Sliding Window (LC 76/424/567), Prefix sum patterns
- Binary Search: Rotated array (LC 33), search-on-answer (LC 410/875/1011)
- Stack: Monotonic (LC 84, 739), Expression (LC 227), Min Stack (LC 155)
- Backtracking: LC 39/78/131 warmups; N-Queens (LC 51) for Bar Raiser

**Phase 3 — Bar Raiser prep:**
- Word Break II (LC 140) — appears in nearly every Bar Raiser report
- Integer to Roman (LC 12) — appeared in 2 separate Bar Raiser rounds
- Median of Two Sorted Arrays (LC 4) — SDE3 Bar Raiser level
- Smallest Range from K Lists (LC 632) — heap Hard
- 2D matrix + priority queue (unnamed, Hard)
- K sorted linked lists max sum path (heap follow-up)

---

## Behavioural — Leadership Principles Themes

These LP themes appear in every round (often ≥2 per round):

| LP | Question Pattern |
|---|---|
| Customer Obsession | "Tell me about a time you went beyond the ask to serve a customer/user" |
| Ownership | "Tell me about a time you owned a problem end-to-end outside your direct scope" |
| Bias for Action | "Tell me about a time you made a decision with incomplete information" |
| Dive Deep | "Tell me about a time you found an unexpected root cause by going deep" |
| Deliver Results | "Tell me about a time you delivered under a tight deadline or obstacle" |
| Disagree and Commit | "Tell me about a time you disagreed with a decision but executed it anyway" |
| Invent and Simplify | "Tell me about a time you found a simpler solution to a complex problem" |

Prepare 5–6 STAR stories reusable across these themes. Customer Obsession is weighted highest.
