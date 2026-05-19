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

---

## Binary Search

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Capacity to Ship Packages in D Days | 1011 | Medium | SDE1 R2 (2+×) |
| Split Array Largest Sum | 410 | Hard | SDE2 onsite |
| Missing Element in Sorted Array | 1060 | Medium | SDE2 Seattle |
| Find Single Element in Sorted Array | 540 | Medium | SDE1 R2 |
| IP Range Lookup (binary search on intervals) | ~981 | Medium | SDE2 Bar Raiser Bangalore |

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

---

## Backtracking

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| All Permutations of a String | 46/47 | Medium | SDE1 L4 Poland R2 |
| Word Break II | 140 | Hard | SDE2 Bar Raiser (3+×) |
| Word Search | 79 | Medium | SDE2 onsite (2+×) |
| Path Sum II | 113 | Medium | SDE2 collection |
| Backtracking Medium (unnamed) | — | Med-Hard | SDE2 Bar Raiser (2+×) |

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
- Graph: BFS (LC 200, 994, 127, 841), Topological Sort (LC 210), Dijkstra (LC 743)
- Tree: DFS post-order (LC 124, 337, 2385), BFS level-order (LC 103, 199, 1161)
- Design LLD: Locker System, Unix Find, LRU Cache, Task Scheduler
- String: Word Break (LC 139/140), 3-page sequence (LC 1152), String formatter

**Phase 2 — High yield for SDE2:**
- DP: House Robber (LC 198), Frog Jump (LC 403), Split Array (LC 410)
- Intervals: Merge Intervals (LC 56), Meeting Rooms (LC 253), Capacity to Ship (LC 1011)
- Heap: Top K stream (LC 460-style), LFU Cache (LC 460), Merge N Lists (LC 23)
- Graph Hard: Making Large Island (LC 827), Word Ladder II (LC 126), Find All People (LC 2092)

**Phase 3 — Bar Raiser prep:**
- Word Break II (LC 140) — appears in nearly every Bar Raiser report
- Integer to Roman (LC 12) — appeared in 2 separate Bar Raiser rounds
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
