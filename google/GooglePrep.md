# Google Interview Prep — Problem Catalog

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

---

## Backtracking / Recursion

| Problem | LC# | Difficulty | Seen At |
|---|---|---|---|
| 24 Game | 679 | Hard | L4 US onsite |
| Card game 3-card pattern (backtracking on 12 cards) | — | Medium | L5 onsite |
| Regular expression tree isMatch | — | Hard | Onsite |
| Word search palindrome variant | 79-like | Medium | Onsite |

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

---

## Study Priority Order

**Phase 1 — Core Patterns (do these first):**
- Graphs: BFS, multi-source BFS, Dijkstra, Union-Find
- Trees: DFS, LCA, level-order, leaf operations
- Heap: streaming k-largest/smallest, room/task assignment
- Trie: insert, prefix search, top-k autocomplete

**Phase 2 — Less Common but High Yield:**
- Intervals: insert, merge, sweep (LC 57, 715, 732, 2158)
- DP: coin change variants, knapsack, bitmask DP
- Sliding window + monotonic deque
- Backtracking: expression evaluation, game theory

**Phase 3 — Design rounds (L4+):**
- LRU Cache, rate limiter, calendar scheduler
- Trie-backed autocomplete
- Parking lot, restaurant waitlist

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
