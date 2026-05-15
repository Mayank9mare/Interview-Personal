# Google DSA Additions — Design Spec
**Date:** 2026-05-15  
**File:** `google/GoogleDSA.cpp`  
**Scope:** Append ~20 problems to the existing file across 5 filled sections and 4 new sections.

---

## Context

`google/GoogleDSA.cpp` already exists with 11 sections (Sections 1–11) covering Strings, Sliding Window, Binary Search, Monotonic Stack, Graph, Topological Sort, DP, Intervals, Matrix, Math/Simulation, and Google-Specific patterns. Some sections are thin (1–2 problems). Four topic areas are entirely missing.

This spec defines all additions. No existing code is modified — all new problems are appended to existing or new sections.

---

## Part 1 — Fill Thin Sections

### Section 2 — Sliding Window (add 2 problems)

**2C. Fruit Into Baskets (LC 904)**  
At most 2 distinct fruit types; find the longest subarray. Reduction of "longest subarray with at most K distinct" (K=2). Sliding window + frequency map.  
Complexity: O(n).

**2D. Max Consecutive Ones III (LC 1004)**  
Flip at most K zeros to get the longest subarray of 1s. Sliding window tracking zero count; shrink left when zeros > K.  
Complexity: O(n).

---

### Section 3 — Binary Search (add 2 problems)

**3C. Koko Eating Bananas (LC 875)**  
Binary search on answer: find minimum eating speed such that all piles can be eaten in H hours. Monotone predicate: if speed S works, S+1 works too.  
Complexity: O(n log maxPile).

**3D. Find Peak Element (LC 162)**  
Find any peak (element greater than both neighbours) in O(log n). Binary search: if `nums[mid] < nums[mid+1]`, a peak exists to the right.  
Complexity: O(log n).

---

### Section 4 — Monotonic Stack (add 2 problems)

**4B. Daily Temperatures (LC 739)**  
For each day, how many days until a warmer temperature? Monotonic decreasing stack of indices; pop when current temp > stack top.  
Complexity: O(n).

**4C. Largest Rectangle in Histogram (LC 84)**  
Find the largest rectangle in a histogram. Monotonic increasing stack; when a shorter bar is encountered, pop and compute area using the popped bar as height.  
Complexity: O(n).

---

### Section 9 — Matrix (add 2 problems)

**9B. Spiral Matrix (LC 54)**  
Traverse an m×n matrix in spiral order. Shrink boundary (top/bottom/left/right) after each pass.  
Complexity: O(m·n).

**9C. Number of Submatrices That Sum to Target (LC 1074)**  
Count submatrices summing to target. Prefix sums per row + 1D "subarray sum equals K" (LC 560) inner loop using a hash map.  
Complexity: O(m²·n).

---

### Section 10 — Math/Simulation (add 1 problem)

**10C. Robot Bounded in Circle (LC 1041)**  
Given a sequence of moves (G/L/R), does the robot stay in a bounded circle forever? After one cycle: if robot is back at origin OR not facing north → bounded.  
Complexity: O(n).

---

## Part 2 — New Sections

### Section 12 — Two Pointers

**12A. 3Sum (LC 15)**  
Find all unique triplets summing to zero. Sort, then for each element fix it and use two pointers on the rest. Skip duplicates carefully.  
Complexity: O(n²).

**12B. Container With Most Water (LC 11)**  
Two pointers from both ends; always move the pointer with the shorter height (the shorter side is the bottleneck).  
Complexity: O(n).

**12C. Trapping Rain Water (LC 42)**  
Two pointers + running max from each side. Water at position i = `min(maxLeft, maxRight) - height[i]`.  
Complexity: O(n) time, O(1) space.

---

### Section 13 — Tries

**13A. Implement Trie (LC 208)**  
`TrieNode` with `children[26]` and `isEnd` flag. `insert`, `search`, `startsWith` — all O(L) where L = word length.

**13B. Add and Search Words (LC 211)**  
Trie with wildcard `.` matching any character. `search` does DFS when it encounters `.`; otherwise standard trie walk.  
Complexity: O(26^L) worst case for a query of all `.`s.

**13C. Word Search II (LC 212)**  
Find all words from a dictionary that exist in a 2D grid. Build a Trie from the dictionary; DFS on the grid using the Trie to prune early. Mark visited cells to avoid reuse. Prune leaf nodes from the Trie after finding a word.  
Complexity: O(m·n·4^L) bounded by trie depth.

---

### Section 14 — Heap / Priority Queue

**14A. K Closest Points to Origin (LC 973)**  
Max-heap of size K on squared distance; push each point, pop when size > K. Or `nth_element` for O(n) average.  
Complexity: O(n log K).

**14B. Find Median from Data Stream (LC 295)**  
Two heaps: `maxHeap` (lower half) and `minHeap` (upper half). Maintain size invariant: `|maxHeap| == |minHeap|` or `|maxHeap| == |minHeap| + 1`. Median = top of `maxHeap` or average of both tops.  
Complexity: O(log n) per insert, O(1) median.

**14C. Merge K Sorted Lists (LC 23)**  
Min-heap of `(value, listIndex, nodePointer)`. Pop minimum, advance that list's pointer, push next node.  
Complexity: O(N log K) where N = total nodes.

---

### Section 15 — Bit Manipulation

**15A. Single Number (LC 136)**  
XOR all elements; duplicates cancel out, leaving the single element.  
Complexity: O(n) time, O(1) space.

**15B. Counting Bits (LC 338)**  
`dp[i] = dp[i >> 1] + (i & 1)`. The number of 1-bits in i equals bits in i/2 plus the lowest bit.  
Complexity: O(n).

**15C. Number of 1 Bits + Reverse Bits (LC 191 / LC 190)**  
191: Count set bits using `n & (n-1)` to clear lowest set bit in a loop.  
190: Reverse 32-bit unsigned integer bit by bit, shifting result left and input right.  
Both: O(32) = O(1).

---

## Implementation Notes

- All code in C++14 (`-std=c++14`). No structured bindings, no `auto& [a,b]`.
- `pair`: use `.first`/`.second`. `tuple`: use `get<0>()` etc.
- Section headers use the existing `═══` banner style.
- Problem headers use the existing `── NX. Title (LC ###) ───` style.
- Each problem gets: one-line description comment, approach comment, complexity comment, then the function(s).
- `main()` smoke tests extended with one representative test per new function.
- File compiles clean with: `g++ -std=c++14 -O2 -o GoogleDSA GoogleDSA.cpp`

---

## Success Criteria

1. `GoogleDSA.cpp` compiles without warnings under `g++ -std=c++14`.
2. All new `main()` tests print expected output.
3. Each new problem has approach + complexity comments.
4. No changes to existing Sections 1–11 code.
