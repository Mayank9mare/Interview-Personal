# Uber Interview Prep — Problem Catalog

Sourced from 102 real Uber SDE/SSE interview experience posts (LeetCode discuss, 2021–2025)
plus two LLD collection posts. Covers SDE1 (L3), SDE2 (L4), SSE (L5), and Staff (L5B).

---

## Round Structure

| Round | Format | What to Expect |
|---|---|---|
| OA | CodeSignal, 3–4 problems, 90–105 min | 1 easy + 1–2 medium + 1 hard; auto-scored |
| Phone Screen | 1 hard DSA, 45–60 min | No hints; elimination round |
| Algorithms & Data Structures | 1–2 LC medium/hard | Graphs, DP, binary search most common |
| Depth in Specialization / Machine Coding | Full OOP/LLD in IDE, 60–90 min | Must compile, must be thread-safe for SDE2+ |
| Design & Architecture (HLD) | Whiteboard/verbal, 45–60 min | API design + DB schema expected |
| Bar Raiser | Deep-dive past project + new design | Evaluates scope and seniority; custom problems |
| Hiring Manager | Behavioural | Conflict, ownership, "why Uber", estimation |

**Key Uber quirk:** The Machine Coding round is the hardest. Interviewers expect production-quality
running code with thread safety, completed in 60–90 min in a real IDE. Language: Java preferred
for backend; TypeScript/React for frontend tracks. Concurrency questions are common at SDE2+.

---

## Most Repeated Problems (High Priority — 3+ independent posts)

| Problem | LC# | Level | Context |
|---|---|---|---|
| **Currency conversion via graph** — given exchange-rate pairs, convert any pair; follow-ups: Dijkstra, disconnected graph, arbitrage | 399 | Medium-Hard | SDE2 DSA (every experience — #1 most asked) |
| **Meeting Room Scheduler (LLD)** — N rooms, book events, follow-ups: capacity, spillage minimization, audit logs, concurrency | 729/253 | Medium-Hard | SSE/SDE2 Machine Coding (4+ posts) |
| **Bus Routes** — minimum bus transfers to reach destination; follow-up: add transfer costs → Dijkstra | 815 | Hard | SDE2/SSE DSA (3+ posts) |
| **Parking Lot (LLD)** — multi-floor, bike + car spots, busy-time bike-in-car-spot rule | — | Medium | SDE2 Machine Coding (3+ posts) |
| **Autocomplete with Top-K weighted completions** — Trie + DFS + min-heap; update weights on user type | 642 | Medium-Hard | SDE2/L4 NYC (3+ posts) |

---

## Graphs / BFS / DFS

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Currency exchange / evaluate division | 399 | Medium-Hard | SDE2 DSA (every loop — 5+ appearances) |
| Bus Routes (BFS on hypergraph) | 815 | Hard | SDE2/SSE DSA (3+ appearances) |
| Bus Routes with transfer costs (Dijkstra follow-up) | 815 variant | Hard | SDE2 DSA follow-up |
| Multi-source BFS with thief-avoidance radius | — | Medium-Hard | SDE1/L3 coding round |
| Color trapped in matrix (k-degree BFS surroundings) | ~130 | Medium | SDE2 onsite 2024 |
| Shortest path with forbidden zones | — | Medium | SDE1 new grad |
| Max sum from N queues, pick K from fronts only | — | Medium-Hard | SDE1 on-campus R1 |
| Synonym query deduplication (Union-Find) | ~721 | Medium | SDE1/SDE2 phone |
| Longest path in DAG (Uber cab scenario) | ~329 | Hard | SDE2 India |
| Word Search | 79 | Medium | SDE2 depth-in-spec round |
| Word Search II (Trie + backtracking) | 212 | Hard | SDE2 phone |
| Number of Islands (variants) | 200 | Medium | MLE/SDE2 screening |
| Course Schedule II (topological sort) | 210 | Medium | SDE2/SSE onsite |
| N-ary tree boundary traversal | ~545 | Medium | SSE DSA round |
| Lowest Common Ancestor | 236 | Medium | SWE intern round |
| Tree value collection (two-player A↓ B↑ game) | — | Hard | L5 custom round |
| Manager hierarchy CRUD (org tree) | — | Medium | L5 custom round |
| Network Delay Time (Dijkstra on directed weighted graph) | 743 | Medium | SDE2 warmup / phone screen |
| Cheapest Flights Within K Stops (Bellman-Ford / modified Dijkstra) | 787 | Medium | SDE2 DSA 2025 |
| Min Cost to Connect All Points (Prim's / Kruskal MST) | 1584 | Medium | SDE2 phone 2025 |
| Redundant Connection (Union-Find cycle detection) | 684 | Medium | SDE2 2025 prep |
| Is Graph Bipartite? (BFS 2-coloring) | 785 | Medium | SDE1/SDE2 screening |
| Clone Graph (BFS + HashMap) | 133 | Medium | SDE2 phone warmup |
| Surrounded Regions (BFS flood fill from edges) | 130 | Medium | SDE2 onsite |

**Currency conversion is essentially guaranteed for SDE2 DSA.** Know LC 399 cold, including:
- Disconnected graph → return -1
- All-pairs precomputation → Floyd-Warshall
- Arbitrage detection → Bellman-Ford negative cycle

---

## Trees / BST

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Flatten binary tree to linked list (in-place) | 114 | Medium | SDE1 phone screen (2+ posts) |
| Validate BST | 98 | Easy | SDE2 screening |
| BST in-order without recursion → Morris traversal follow-up | 94 | Hard | SSE depth-in-spec |
| Version compatibility chain (addVersion/isCompatible) | — | Medium | SDE2 Machine Coding (2+ posts; unique to Uber) |
| Binary Tree Maximum Path Sum (any-to-any path, post-order) | 124 | Hard | SDE2 India onsite |
| Serialize and Deserialize Binary Tree | 297 | Hard | SDE2/SSE onsite |
| Diameter of Binary Tree | 543 | Easy | SDE1 phone warmup |
| Count Good Nodes in Binary Tree (root-to-node max) | 1448 | Medium | SDE1/SDE2 2025 prep |
| Path Sum III (prefix sum on tree) | 437 | Medium | SDE2 phone 2025 |
| Construct Binary Tree from Preorder and Inorder | 105 | Medium | SDE2 phone screen |

**Version Compatibility** is a recurring Uber-specific machine coding problem: track compatibility
groups — increment group ID on each incompatible release; `isCompatible(v1, v2)` = same group ID.

---

## Dynamic Programming

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Minimise transactions / Optimal Account Balancing | 465 | Hard | SDE2 DSA (2+ posts) |
| Haunted House — max group satisfying [L,R] buddy constraints | — | Hard | SDE2 DSA (2+ posts; segment tree optimal) |
| Climbing stairs + follow-up variants | 70 | Easy-Med | SSE phone screen |
| Decode Ways / Decode Ways II | 91/639 | Med/Hard | Intern rounds |
| Dungeon Game | 174 | Hard | Intern OA |
| 2D DP with multiple follow-ups | — | Hard | L4 fullstack coding |
| Max K words per hour from infinite stream | ~692 | Hard | SDE2 DSA R1 |
| Max substrings with per-character length constraints | — | Hard | SDE2 DSA R2 |
| Coin Change (bottom-up DP) | 322 | Medium | SDE1/SDE2 warmup |
| Longest Increasing Subsequence (also O(n log n) binary search) | 300 | Medium | SDE2 phone |
| Longest Common Subsequence | 1143 | Medium | SDE2 2025 prep |
| Edit Distance | 72 | Medium | SDE2 onsite |
| Jump Game VI (sliding window DP / monotonic deque, O(N log N)→O(N)) | 1696 | Medium | SDE2 DSA follow-up |
| Word Break (DP + Trie) | 139 | Medium | SDE2 2025 prep |
| Burst Balloons (interval DP) | 312 | Hard | SSE onsite 2025 |
| Unique Paths II (DP with obstacles) | 63 | Medium | SDE1 OA |

---

## Arrays / Sliding Window / Binary Search

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Sort squares of sorted array (two-pointer) + Kth smallest square (binary search follow-up) | 977 | Med + Hard | SDE2 phone (2+ posts; follow-up is the hard part) |
| Sliding window minimum-then-maximum | ~239 | Medium | SDE2 L4 |
| Rotten Oranges | 994 | Medium | SDE2 OA (CodeSignal, 2+ posts) |
| No adjacent same characters (Reorganize String) | 767 | Medium | SDE2 DSA (2+ posts) |
| Next Smallest Palindrome | ~564 | Medium | SDE2/SSE phone (2+ posts) |
| Weighted random pick (Random Pick with Weight) | 528 | Medium | SDE2/SSE DSA |
| Pacific Atlantic Water Flow | 417 | Medium | SDE2 DSA |
| Minimum operations to make array continuous | 2009 | Hard | SDE1 OA |
| Max length K equal ropes from N ropes (binary search on answer) | — | Medium | SDE1 OA |
| Minimum absolute sum difference | 1826 | Medium | SSE OA CodeSignal |
| Longest repeating character replacement | 424 | Medium | Intern OA |
| Valid Sudoku | 36 | Medium | SDE2 Android DSA |
| Minesweeper board fill | 529 | Medium | Intern onsite |
| Lines intersecting rectangles (2D, binary search + OOP) | — | Hard | SDE2 DSA R2 |
| Maze robot distance (4-directional distance arrays) | — | Medium | SDE2 SF phone |
| Evaluate arithmetic expression string (nested add/sub/mul/div) | — | Medium | SDE2 India onsite |
| Breaking Bad (longest matching chemical symbol in names) | — | Medium | SDE2 phone (Trie approach) |
| Array comparison score (a[i]<a[i+1]→+1, a[i]>a[i+1]→+2) | — | Easy | SDE2 OA CodeSignal |
| Minimum Window Substring | 76 | Hard | SDE2 DSA round 2025 |
| Sliding Window Median (two heaps, O(n log n)) | 480 | Hard | SDE2/SSE phone screen |
| Container With Most Water (two pointers) | 11 | Medium | SDE1/SDE2 warmup |
| Subarray Sum Equals K (prefix sum + HashMap) | 560 | Medium | SDE2 2025 prep |
| Median of Two Sorted Arrays (binary search O(log min(m,n))) | 4 | Hard | SSE phone screen |
| Trapping Rain Water | 42 | Hard | SDE2 2025 prep |
| 3Sum (two-pointer after sort) | 15 | Medium | SDE2 screening |
| Maximum Product Subarray | 152 | Medium | SDE2 OA 2025 |
| Find the Duplicate Number (Floyd's cycle detection) | 287 | Medium | SDE2 phone |

---

## Heap / Priority Queue

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Find Median from Data Stream + follow-up (power-of-2 loose median) | 295 | Hard | SDE2 DSA (novel follow-up) |
| Top K elements from N queues (front-pop only) | ~632 | Medium-Hard | SDE1 on-campus |
| Random Pick with Weight | 528 | Medium | SSE onsite |
| Top K frequent elements | 347 | Medium | SDE2 India |
| Merge K Sorted Lists (min-heap) | 23 | Hard | SDE2 India onsite |
| The Skyline Problem (heap + line sweep events) | 218 | Hard | SSE onsite 2025 |
| Kth Largest Element in an Array (quickselect / heap) | 215 | Medium | SDE2 phone |
| Task Scheduler (heap + greedy idle insertion) | 621 | Medium | SDE2 2025 prep |

---

## Backtracking / Recursion

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| Permutations I / II | 46/47 | Medium | SDE1/SDE2 coding warmup |
| Combination Sum I / II | 39/40 | Medium | SDE1/SDE2 warmup |
| Subsets I / II | 78/90 | Medium | SDE1 phone |
| Palindrome Partitioning | 131 | Medium | SDE2 onsite 2025 |
| Generate Parentheses | 22 | Medium | SDE1 OA / warmup |
| N-Queens | 51 | Hard | SSE onsite |
| Sudoku Solver | 37 | Hard | SDE2/SSE onsite |
| Word Break II (return all sentences) | 140 | Hard | SDE2 Bar Raiser |

---

## Design / LLD (Machine Coding — must be coded + thread-safe)

| Problem | Pattern | Difficulty | Seen At |
|---|---|---|---|
| Meeting Room Scheduler (book, audit logs, capacity, spillage min, concurrency) | Interval + heap | Hard | SSE/SDE2 (4+ posts — #1 LLD) |
| Parking Lot (multi-floor, bike/car, busy-time 4-bikes-in-car-spot) | OOP + queues | Medium | SDE2 (3+ posts) |
| Cache with TTL + concurrency (get/set/expireAfterX, lazy cleanup) | HashMap + heap | Medium-Hard | SDE2 (2+ posts) |
| Text Editor (Append, Backspace, Undo, Redo all O(1)) | DLL + stacks | Medium-Hard | SDE1/SDE2 (2+ posts) |
| Version Compatibility Management (addVersion/isCompatible, group IDs) | HashMap | Medium | SDE2 (2+ posts; Uber-specific) |
| Leaderboard (basic → add cache → make thread-safe with keyed executor) | HashMap + sorting | Medium-Hard | SDE2 (2+ posts) |
| Autocomplete engine (Trie + Top-K by weight, update on user input) | Trie + heap | Medium-Hard | SDE2/L4 (2+ posts) |
| Multi-threaded Job Scheduler (tasks with prerequisites + parallelism) | DAG + thread pool | Hard | SDE2 Machine Coding |
| Multi-threaded Pub-Sub Message Queue | Observer + locks | Hard | SDE2 Machine Coding |
| Rate Limiter (count+timestamp array → per-call interface) | Sliding window | Hard | SSE Machine Coding |
| Key-Value Store with getRandom + concurrency | HashMap + array | Medium | SDE2 DSA/LLD |
| Design Facebook (3 core features, multithreaded, running code) | OOP + concurrency | Hard | SDE2 Machine Coding |
| Org Tree / Reporting Hierarchy (count all reports, add/change/delete) | HashMap + DFS | Medium | L5 round |
| Car Reservation System (allocate N cars, maximize customers served) | Greedy + OOP | Medium | SDE1 L3 coding |
| Restaurant Service LLD (SOLID, OOP design) | OOP patterns | Medium | SDE2 LLD |
| Bank Settlement / Clearing House (GetSettlement APIs + DB design) | Graph + HashMap | Hard | SSE R2 |
| Observable Pattern (subscribe/notify/unsubscribe) | Observer | Easy | Frontend SDE2 |
| JS Task Runner (topological sort + async Promises) | Graph + async | Hard | L4 Fullstack (2+ posts) |
| mapLimit (async throttle, N concurrent, preserve order) | Async/Promise | Hard | SDE2 Frontend (2+ posts) |
| Recursive Comment System (React, arbitrary depth) | React + recursion | Medium | L4 Fullstack |

---

## HLD / System Design

| Problem | Difficulty | Seen At |
|---|---|---|
| Uber Eats restaurant feed (quad tree / geohashing, serialization) | Hard | SDE2/SSE (2+ posts) |
| Stock price subscription + alert system (Observer + pub-sub at scale) | Medium-Hard | SDE2 (2+ posts) |
| Top 10 movies dashboard per hour (Kafka + S3 + batch, merge intervals) | Hard | SDE2 Data Eng |
| Webhook delivery system | Medium-Hard | SDE2 2024 |
| Calendar app backend (load balancer, DB, pagination) | Medium | L4 NYC |
| Log search / autocomplete (Trie + inverted index, 1GB memory limit) | Hard | SDE2 Bar Raiser |
| Social / news feed system | Medium | SDE2 |
| Android SDK system design | Hard | SDE2 Android |

---

## OA Patterns (CodeSignal — confirmed real problems)

CodeSignal format: 3–4 problems, 90–105 min. Scoring: 600/600 is possible.

| Problem | Approach |
|---|---|
| Array comparison score (a[i] comparisons → +1 or +2) | Simple scan, O(N) |
| Complex string matching with index-position logic | Implementation heavy |
| Rotten Oranges (BFS) | LC 994 |
| Max length K ropes (binary search on answer) | Binary search, O(N log MaxLen) |
| Minimum operations to make array continuous | LC 2009, sliding window |
| Longest repeating character replacement | LC 424 |
| Minimum absolute sum difference | LC 1826, sorted + binary search |
| Dungeon Game | LC 174 |
| Decode Ways | LC 91 |

---

## Study Priority Order

**Phase 1 — Must-solve before any Uber interview:**
- **LC 399** Currency Exchange / Evaluate Division — know cold, practice all follow-ups
- **LC 815** Bus Routes — BFS on sets of stops
- **LC 295** Find Median from Data Stream — plus power-of-2 follow-up
- **LC 76** Minimum Window Substring — classic hard sliding window
- **LLD: Meeting Room Scheduler** — thread-safe, with interval tree or TreeMap
- **LLD: Parking Lot** — multi-floor, OOP hierarchy, EnumMap queues

**Phase 2 — High yield:**
- Graphs: LC 200, 210, 212, 130, 743, 787, 1584; multi-source BFS patterns
- Trees: LC 114 (flatten), 98 (validate BST), 124 (max path sum), 297 (serialize)
- Arrays: LC 977 + binary search follow-up, LC 767, LC 480 (sliding window median)
- DP: LC 322, 300, 1143, 72 (edit distance), 1696 (Jump Game VI)
- Backtracking: LC 46/78/39 warmup; LC 140 Word Break II for Bar Raiser
- LLD: Cache with TTL, Text Editor (DLL + stacks), Version Compatibility

**Phase 3 — SSE/L5 prep:**
- Haunted House (segment tree Hard)
- Tree two-player game DP
- Bank Settlement APIs + DB design
- Concurrency: condition variables, keyed executor, producer-consumer
- LC 218 Skyline Problem; LC 480 Sliding Window Median; LC 51 N-Queens

---

## Uber-Specific Observations

- **Currency conversion is near-guaranteed** for SDE2 DSA. Every 2024 experience mentions it.
- **Machine Coding round is the hardest** — code runs live in your IDE; interviewers run test cases.
- **Concurrency is tested explicitly** — condition variables, synchronized blocks, or Promise-based async (frontend). Java `BlockingQueue` is allowed unlike Rubrik.
- **Follow-up culture is strong**: expect 2–3 follow-ups per problem pushing you from O(N²) → O(N log N) → O(N).
- **Kth smallest square** (binary search follow-up to LC 977) has eliminated multiple candidates — practice it.
- **Version Compatibility** is a unique Uber machine coding problem — O(1) solution using group ID counters is expected.
