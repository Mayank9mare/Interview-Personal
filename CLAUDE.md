# Interview Prep Repo — Claude Context

## Repo Purpose
Personal interview preparation. Contains DSA problems, LLD (Low-Level Design) problems, system design notes, and company-specific problem sets.

## Language Convention
| Problem type | Language |
|---|---|
| DSA (algorithms, data structures) | **C++** |
| LLD (object-oriented design, system design coding) | **Java** |
| SQL | `.sql` |
| System design notes | Markdown |

Java LLD files are standalone (no Maven/Gradle). Compile with `javac FileName.java` and run with `java FileName`.

## Compiler Constraint — CRITICAL
The local GCC is **MinGW GCC 6.3.0 on Windows**, which only supports up to **C++14**.
- Always compile with `-std=c++14`, never `-std=c++17` or later.
- **C++17 structured bindings (`auto& [a, b]`) are NOT supported** — GCC 6.3 errors with "expected unqualified-id before '[' token".
- Replace all structured bindings:
  - `pair`: use `.first` / `.second`
  - `tuple`: use `get<0>(t)`, `get<1>(t)`, `get<2>(t)`
  - Loop over edges: `for (auto& e : edges) { int u = get<0>(e), v = get<1>(e); ... }`

## File Structure
```
/                           ← DSA C++ files (flat, one file per topic)
  Algorithms.cpp            — sliding window, two pointers, binary search, DP, etc.
  GraphAlgorithms.cpp       — Dijkstra, Bellman-Ford, Floyd-Warshall, MST, SCC, etc.
  ArrayProblems.cpp
  DynamicProgramming.cpp
  HeapProblems.cpp
  LinkedList.cpp
  StringProblems.cpp
  StackQueueProblems.cpp
  TreeProblems.cpp
  BitManipulation.cpp

/rippling/                  ← Rippling-specific LLD problems (Java)
  music-analytics/          MusicAnalytics.java
  key-value-store/          KeyValueStore.java
  employee-access/          EmployeeAccess.java, OrgTree.java
  excel-sheet/              ExcelSheet.java
  delivery-cost/            DeliveryCost.java
  expense-rule-engine/      ExpenseRuleEngine.java
  currency-arbitrage/       CurrencyArbitrage.cpp  ← DSA, so C++

/google/                    ← Google-specific LLD problems (Java)
  logger/                   LoggerMessagePrinter.java
  lru-cache-ttl/            LRUCacheWithTTL.java
  search-autocomplete/      SearchAutocomplete.java
  restaurant-waitlist/      RestaurantWaitlist.java

/amazon/                    ← Amazon-specific LLD problems (Java) + DSA (C++)
  AmazonDSA.cpp             — Arrays/Sliding Window, Heap/Intervals, Graphs, Stack/Design
  unix-find/                FileSearch.java
  pizza-pricing/            PizzaPricing.java
  hit-counter/              HitCounter.java
  parking-lot/              ParkingLot.java
  locker-system/            LockerSystem.java

/rubrik/                    ← Rubrik-specific LLD/concurrency problems (Java)
  job-scheduler/            JobScheduler.java
  task-dependency/          TaskDependencyExecutor.java
  memory-block-tracker/     MemoryBlockTracker.java
  dems-and-reps/            DemsAndReps.java

/slice/                     ← Slice-specific LLD problems (Java) + DSA (C++)
  SliceDSA.cpp              — Linked List, Stack, Binary Tree, Arrays/Greedy, Graph
  expense-sharing/          ExpenseSharing.java
  task-scheduler/           TaskScheduler.java

/<other-lld-folders>/       ← LLD problems (Java), one folder per problem

SystemDesign.md             ← System design reference notes
SQL.sql                     ← SQL practice
questions.md                ← Problem list / interview question log
```

## Key Design Notes (for quick recall)

### Rippling — Music Analytics
- `LinkedHashSet` with remove-then-add for O(1) LRU-ordered unique history.
- `recentlyPlayed(userId, k)`: iterate from tail of LinkedHashSet (index `size-1` down).

### Rippling — Key-Value Store with Transactions
- Undo-log strategy: writes applied immediately; pre-image recorded only on **first touch per key per txn frame**.
- `ABSENT` sentinel marks "key didn't exist before this txn".
- `rollback()`: replay undo log in reverse insertion order.
- `commit()`: O(1) pop of txn frame.

### Rippling — Employee Access Management
- RBAC with role inheritance DAG.
- `get(employeeId)` = direct grants ∪ BFS over role DAG (visited-set prevents cycles).
- Wildcard resource matching: `"data/*"` covers any resource with prefix `"data/"`.
- `extendRole(childRole, parentRole)` builds inheritance DAG.

### Rippling — Org Tree Height Cap
- `flattenToHeight(h)`: DFS; any node at `depth > h` is reparented directly to CEO (depth resets to 1).
- Children of reparented nodes continue DFS from new depth, minimising CEO direct reports.

### Rippling — Excel Sheet
- Recursive descent parser: `parseExpr → parseTerm → parseFactor`.
- Memoised `eval(cell, visiting, cache)` per `print()` call; DFS visiting-set catches circular refs (`#CIRC`).
- Multi-letter column labels (AA, AB…) handled by `colLabel()` / `cellToRowCol()`.

### Rippling — Delivery Cost
- Use `BigDecimal` for money (`0.1 + 0.2 = 0.30000000000000004` in `double`).
- Duration: parse `"HH:MM"` or `"HH:MM:SS"` to seconds; cost = `rate × seconds / 3600`.

### Rippling — Expense Rule Engine
- `@FunctionalInterface Rule` → lambdas or named classes both work.
- `validate()` = fail-fast; `validateAll()` = collect all violations; `validateBatch(list)` = per-expense map.

### Google — Interview Round Structure
- Typically 4–5 rounds: 2–3 DSA coding, 1 system design, 1 behavioural/Googleyness.
- DSA: LeetCode Medium–Hard; DP, Graph, Line Sweep are important topics.
- One round may ask you to implement a class with multiple methods (LLD-lite) rather than a single function.
- Interviewers rate SH / H / LH / LNH / NH / SNH. Trade-offs + edge cases can bump a H to SH.
- Codezym (`https://www.codezym.com/lld/google`) has **both** LLD and DSA problems for Google, mixed in one table. Rows tagged `Google DS & Algo` are DSA; rows tagged `Google LLD` (with a Design Pattern column) are LLD.

### Google — Logger Message Printer
- **Variant A** (rate limiter, LeetCode 359): `HashMap<msg, lastTimestamp>`; print only if `timestamp - last >= 10`.
- **Variant B** (request tracker, onsite confirmed): `startReq(id, t)` / `finishReq(id)` / `printFinished()`.
  - Min-heap on `startTime`; drain while `heap.peek().finished == true` — guarantees no gap in start-time order.
  - Classic out-of-order completion: D finishes first but can't print until A (earliest) finishes.

### Google — LRU Cache with TTL
- Standard DLL + HashMap LRU, extended with `expireAt = currentTimeMillis() + ttlMs` per node.
- `get`: if expired → evict node, return -1 (lazy expiry — no background thread).
- `put`: on capacity overflow, evict LRU tail (whether or not it's expired — it's the least recently used).
- `cleanup()`: explicit full sweep to purge all stale entries (call proactively if needed).
- TTL refreshed on re-`put` of existing key.

### Google — Search Autocomplete (Trie)
- Trie where **each TrieNode stores `Map<sentence, freq>`** for all sentences passing through it.
  - Avoids DFS to leaves on every query; top-k sort happens at the prefix node directly.
- `addSentence(s, count)`: walk each character, `node.freq.merge(s, count, Integer::sum)` at each node.
- `input('#')`: records current typed string, resets buffer, returns `[]`.
- Ranking: frequency desc, then lexicographic asc; `stream().sorted().limit(3)`.

### Google — Restaurant Waitlist
- `LinkedList<Party>` FIFO + `HashMap<name, Party>` for O(1) lookup and duplicate detection.
- `seatNextFitting(capacity)`: linear scan from front, seat first party whose `size ≤ capacity` (first-fit, skips oversized parties).
- `removeParty(name)`: cancellation — O(n) `queue.remove(p)`; mention O(1) upgrade via `LinkedHashMap` if asked.
- `estimatedWaitTime(name, avgMin)`: `(position - 1) × avgMin`.

### Google — High-Frequency DSA Problems (seen 2+ times across 130+ posts)
Full catalog: `google/GooglePrep.md`
- **Trie autocomplete** (4+ ×): each TrieNode holds `Map<sentence,freq>`; top-k at prefix node. Fuzzy variant: BK-Tree (edit distance).
- **LC 1293** Shortest Path in Grid with Obstacles Elimination (3×): BFS + state = (row, col, removalsLeft).
- **LC 2402** Meeting Rooms III + **LC 1882** Process Tasks Using Servers (3×): two min-heaps (available by id, busy by endTime).
- **Restaurant Waitlist** (3×): FIFO `LinkedList` + `HashMap` for O(1) lookup; `seatNextFitting(cap)` linear scan.
- **BFS router/WiFi broadcast** (3×): standard BFS from source, mark visited, return reachable.
- **LC 818** Race Car (2×): BFS over (position, speed) state OR DP.
- **LC 239** Sliding Window Maximum (2×): monotonic deque, O(n).
- **LRU Cache / variant** LC 146 (2×): DLL + HashMap; recent-search variant moves duplicate to front.
- **Rate limiter** dual-window minute+hour (2×): two `Deque`s; pop expired entries before checking limits.
- **Huffman tree** construction/decoding (2×): min-heap by freq; leaf nodes at depth = code length.
- **File/folder compression** (2×): DFS; replace subtree with folder name if all children selected.
- **String % substitution** (2×): HashMap lookup between `%` delimiters; handle `%%` → literal `%`.

### Google — DSA Topics with Key LeetCode Numbers
- **Graphs**: LC 863, 947, 1293 (×3), 2050, 2115; novel: multi-source Dijkstra, BFS router broadcast, 3-island max value.
- **Trees**: LC 366, 872, 1666, 2096, 2265; novel: number of islands in tree, N-ary stored as array, Gmail label paths.
- **DP**: LC 312, 518, 679, 818 (×2), 875, 1048, 174; novel: coin change recovery, jump game max score, apartment hunting 2-pass.
- **Heap**: LC 215, 295, 1834, 1882, 2402 (×3), 2534; novel: patient queue min-id, insurance counter.
- **Intervals / Line Sweep**: LC 57, 715, 729, 732, 986, 2158; novel: APK SDK range partitioning, vertical cake cut.
- **Trie / String**: LC 2185; novel: arithmetic expression evaluator, formula parenthesis simplification, MergeDedupingIterator.
- **Sliding Window / Monotonic**: LC 239 (×2), 315, 1776, 853; novel: 1D array max-value pairs with constraint.
- **Design / LLD-lite**: LC 146 (×2), 348; novel: recent-search DS, rate limiter dual-window, parking lot insert-interval.

### Google — Follow-up Escalation Pattern
Almost every problem has a follow-up. Common escalation ladder:
1. Brute force → optimal (interviewer asks for better complexity)
2. Single source → multi-source (add more starting nodes)
3. Unweighted → weighted (switch BFS → Dijkstra)
4. One query → many queries (precompute / Segment Tree)
5. Single-threaded → thread-safe (add locks / concurrent DS)

### Rubrik — Interview Round Structure
- **Rubrik's system coding round explicitly bans** `BlockingQueue`, `ConcurrentHashMap`, and other concurrent collections. Build thread safety from raw primitives: `ReentrantLock`, `Condition`, `synchronized`, `wait`/`notifyAll`.
- Debugging round: given broken multithreaded code (Producer-Consumer or banking app), find race conditions and fix them.
- DSA round: LeetCode Medium (Google/MS style) — one round, nothing Rubrik-specific.
- Codezym has **zero** Rubrik-tagged problems (confirmed).

### Rubrik — Job Scheduler
- `PriorityQueue<ScheduledTask>` ordered by `triggerMs`; single dispatcher thread; fixed worker `ExecutorService`.
- `schedule(task, delayMs)` = one-shot; `scheduleAtFixedRate(task, initMs, periodMs)` = periodic.
- Dispatcher sleeps with `condition.awaitNanos(waitMs * 1_000_000L)`; a newly enqueued earlier task calls `signal()` to wake it.
- Periodic tasks re-enqueue themselves inside the worker callback after each run.
- Lock used: `ReentrantLock` + `Condition ready` (no `DelayQueue`).

### Rubrik — Task Dependency Executor
- DAG of tasks; each `TaskNode` has `volatile int pendingDeps` (= number of unfinished dependencies).
- On task completion: `synchronized(dep) { --dep.pendingDeps; }` then submit dep to pool if it hits 0.
- Shared `CountDownLatch(total tasks)` — `execute()` blocks on `await()`.
- Cycle detection via DFS before execution starts; throws `IllegalStateException` on cycle.

### Rubrik — Memory Block Tracker
- `TreeMap<Integer,Integer>` (start → end) always maintained in fully-merged form.
- `markChanged(s, e)`: walk map with `floorKey(end+1)` absorbing all overlapping/adjacent ranges, then insert merged range. Adjacent = gap of 0 between blocks (e.g. [1,4]+[5,7] → [1,7]).
- Thread safety: `ReentrantReadWriteLock` — many concurrent readers, exclusive writers.

### Rubrik — Democrats & Republicans (Shared Bathroom)
- Invariant: `democratsInside > 0 → republicansInside == 0` and vice versa.
- Two `Condition`s on one fair `ReentrantLock`: `demTurn`, `repTurn`.
- `enter()`: increment waiting count, `await()` while opposite party is inside, then decrement waiting and increment inside.
- `exit()`: decrement inside; if reaches 0 → `signalAll()` on opposite party's condition (if any waiting), else own.
- Fair lock (`new ReentrantLock(true)`) prevents starvation at the lock level.

### Rippling — Currency Arbitrage (C++)
- Transform: edge weight = `-ln(rate)`; profitable cycle (product > 1) = negative cycle (sum < 0).
- Bellman-Ford with super-source init (`dist[all] = 0`) detects cycles anywhere in graph.
- Cycle extraction: walk back V steps from `lastRelaxed` to land inside the cycle, then trace with visited-set.

### Amazon — Interview Round Structure
- Loop: 3–4 rounds of ~60 min each (virtual or onsite)
- DSA round: 1–2 LeetCode Medium/Hard; key topics: heap/greedy, graphs (Union-Find), sliding window, intervals, stack
- Machine coding (LLD) round: 60 min in an online IDE; expected working OOP code (not pseudocode)
  - Extensibility is graded: Strategy/Decorator patterns score well
  - Most repeated problems: Unix "find" (Strategy), Pizza Pricing (Decorator), Parking Lot, Hit Counter, Locker System
  - Common follow-up: "add a new filter type", "make it thread-safe", "support multi-floor"
- System design: 1 round, whiteboard-style HLD
- Behavioural: Leadership Principles (Customer Obsession is highest weight; prepare ≥1 story per LP)
- Bar Raiser: unannounced, any round; evaluates candidate against the bar for the entire company, not just the team

### Amazon — Unix Find (FileSearch)
- `HashMap<path, size>` for O(1) put/overwrite; linear scan for search (acceptable — find is naturally O(n)).
- Strategy pattern: `SearchCriteria` is a `@FunctionalInterface`; `search(id, dir, arg)` dispatches to a registered strategy.
- Directory prefix isolation: check `path.startsWith(dir + "/")` not `dir` to prevent `/data` matching `/dataset/...`.
- Extensibility: `registerCriteria(id, lambda)` adds new types at runtime — no modification to FileSearch.
- Complexity: O(1) put, O(n) search where n = total files.

### Amazon — Pizza Pricing (Decorator)
- Decorator pattern: `Pizza` interface → concrete base classes (Margherita, Farmhouse) → abstract `ToppingDecorator` wraps a `Pizza` → concrete toppings (ExtraCheese, Pepperoni, etc.).
- `cost()` and `description()` are recursive: each decorator delegates to `wrapped.cost()` and appends its own.
- Double-toppings are free: wrap `new ExtraCheese(new ExtraCheese(...))` — no special case needed.
- Adding a new topping: one new class, zero changes to existing code (Open-Closed Principle).
- Complexity: O(d) per cost/description call where d = decorator chain depth (number of toppings).

### Amazon — Hit Counter (Thread-Safe)
- Circular buffer of size `windowSeconds`: slot `t % windowSeconds` holds hits for second `t`.
- Stale-slot invalidation: `times[slot] != timestamp` means the slot belongs to a prior cycle — reset before incrementing.
- `ReentrantReadWriteLock`: concurrent reads (getHits) are non-blocking; only writes (hit) are exclusive.
- Why not `TreeMap<timestamp, count>`? Circular buffer is O(1) per op vs O(log n), and memory is bounded to exactly `windowSeconds` slots.
- Complexity: O(1) hit, O(windowSeconds) getHits (full buffer scan), O(windowSeconds) space.

### Amazon — Parking Lot
- OOP hierarchy: `Vehicle(type, plate)` + `ParkingSpot(spotId, size, floorNum)` + `ParkingFloor` + `ParkingTicket`.
- `ParkingFloor` maintains `EnumMap<SpotSize, Queue<ParkingSpot>>` available queues — O(1) assignment, no linear scan.
- Vehicle-to-spot preference (smallest first for utilisation): Motorcycle→Small,Medium,Large; Car→Medium,Large; Bus→Large.
- `park()` iterates fitting sizes × floors; first non-empty queue wins. Returns null if lot is full for that vehicle type.
- `unpark(ticket)` uses `ticket.spot.floorNum` to return the spot to the exact right floor queue — O(1).
- Follow-up "make it thread-safe": add `ReentrantReadWriteLock` per floor (reads are `getAvailableCount`, writes are `park`/`unpark`).
- Complexity: O(1) park/unpark (bounded iteration over ≤3 sizes × floors), O(floors) getAvailableCount.

### Amazon — Locker System
- `EnumMap<LockerSize, Queue<Locker>>` available queues — O(1) assignment of smallest fitting locker.
- `codeToLocker` (HashMap) for O(1) `openLocker(code)` lookup; `packageToLocker` for O(1) cancellation/expiry.
- Fitting sizes (smallest first): SMALL→S,M,L; MEDIUM→M,L; LARGE→L only.
- Expiry: `releaseExpired(currentDay)` sweeps `packageToLocker`; frees any locker where `currentDay - assignedDay >= maxHoldDays`.
- `freeLocker(locker)`: clears all fields and re-enqueues — used by both `openLocker` and `releaseExpired`.
- Follow-up "concurrent access": one `ReentrantLock` per locker size (finer grain than a single global lock).
- Complexity: O(1) assign/open, O(n) releaseExpired where n = currently assigned lockers.

### Amazon — High-Frequency DSA Problems (seen 3+ times across 92 posts)
Full catalog: `amazon/AmazonPrep.md`
- **3-page web visit sequence** (3+×): HashMap per user → sort by timestamp → sliding window of 3, count with frequency map. LC 1152.
- **Locker System** (3+×): `EnumMap<Size, Queue<Locker>>` for O(1) assign; `codeToLocker` HashMap for O(1) retrieve. Already in `amazon/locker-system/`.
- **Unix File Search** (3+×): Strategy pattern `SearchCriteria` @FunctionalInterface; `registerCriteria(id, lambda)`. Already in `amazon/unix-find/`.
- **Word Break II** (3+×): Bar Raiser staple. Trie + backtracking or DP. LC 140.
- **Currency Exchange / Evaluate Division** (3+×): Build graph of currencies; BFS/DFS per query; mention Floyd-Warshall for all-pairs precompute. LC 399.
- **Task Scheduler Design** (3+×): `add(task, priority)` + `fetch()` → min-heap ordered by priority; follow-up: periodic tasks (re-enqueue after run).
- **Delivery Centers OA** (3+×): Sort centers; for each candidate location use prefix sums to count how many centers are within distance d. Real Amazon OA.
- **Remove Smallest + Replace with Neighbor Sum** (3+×): Real Amazon OA; greedy / indexed heap simulation.
- **Top K Items in Stream** (3+×): `(event_id, item_id, timestamp)` → HashMap count + min-heap for top K; follow-up: sliding window for last X hours.
- **String Formatter** (3+×): Replace `{0}`, `{1}` with args list; validate index, throw `IllegalArgumentException` on out-of-range.
- **Number of Islands** (3+×): BFS/DFS; Bar Raiser follow-up always asks for list of cluster sizes. LC 200.
- **Word Ladder** (3+×): BFS with word-bank set; each neighbour = 1 char diff. LC 127.
- **Currency Discount in String** (3+×): Parse string for `$\d+(\.\d+)?`, apply 20% off, reconstruct string. Business-wrapped easy.
- **Multiply Large Strings** (3+×): Grade-school multiply; result array of size `m+n`; no BigInteger. LC 43.

### Amazon — DSA Topics with Key LeetCode Numbers
- **Graphs**: LC 127/126 (×3), 200 (×3), 210 (×3), 841, 994, 399 (×3), 827, 417, 2092, 743; novel: delivery station topo sort, currency exchange.
- **Trees**: LC 124 (×3), 2385 (×2), 337, 1161, 103, 199, 333/1373, 1650/1740, 545; novel: burn tree, parent-pointer distance (linked-list intersection trick).
- **DP**: LC 140 (×3 Bar Raiser), 198 (×3), 403, 410, 416, 45, 516; novel: 4D→3D matrix DP.
- **Heap**: LC 460, 23, 295, 767 (×2); novel: top-K stream sliding window, task scheduler priority design.
- **Intervals**: LC 56 (×3), 1011 (×2), 253/2402, 134; novel: minimum platforms, movie scheduling.
- **String**: LC 140 (×3), 1152 (×3), 43 (×3), 438, 472, 165; novel: string formatter, currency discount, 3-page sequence.
- **Design / LLD**: Locker (×3), Unix Find (×3), LRU 146 (×3), Task Scheduler, LFU 460, Insert-Delete-GetRandom 380.
- **Array**: LC 239, 42, 152, 962, 1167, 410; novel: delivery centers OA, remove-smallest OA.

### Amazon — OA Patterns (two problems, 90 min)
Real Amazon OA problems that appear repeatedly — solve these cold before the interview:
- Delivery centers (prefix sum + binary search on sorted centres array)
- Remove smallest element, replace with sum of neighbours (greedy heap simulation)
- Minimum subarray containing two target series (sliding window, like LC 76)
- Maximum product subarray variation (LC 152)
- Seats allocation (greedy)
- Shipment imbalance (custom array/string)

### Uber — Interview Round Structure
- Rounds: OA (CodeSignal) → Phone Screen (1 hard DSA) → Algorithms DSA → Depth-in-Specialization / Machine Coding → HLD → HM / Bar Raiser.
- Machine Coding round is the hardest: production-quality code with thread safety, run live in IDE. Java preferred for backend; TypeScript for frontend.
- OA: CodeSignal platform, 3–4 problems (1 easy + 1–2 medium + 1 hard), 90–105 min. Scoring 600/600 is achievable.
- Concurrency is explicitly tested at SDE2+: condition variables, synchronized blocks, or async Promises (frontend). Not banned like Rubrik.
- Follow-up culture is very strong: each problem has 2–3 escalating follow-ups (O(N²) → O(N log N) → O(N)).
- Full catalog: `UberPrep.md`

### Uber — High-Frequency DSA Problems (seen 3+ times)
- **Currency Exchange / Evaluate Division** (5+×, #1 most asked): LC 399. Build weighted graph; BFS from source currency. Follow-ups: disconnected → return -1; all-pairs → Floyd-Warshall; arbitrage → Bellman-Ford negative cycle.
- **Bus Routes** (3+×): LC 815. BFS where each node is a bus route (set of stops); expand to all stops in a route. Follow-up: add transfer costs → Dijkstra.
- **Meeting Room Scheduler** (4+×, #1 LLD): LC 729/253. N rooms; book events; follow-ups: capacity constraints, spillage minimization (prefer tightest-fit room), audit logs with expiry, concurrent requests.
- **Parking Lot** (3+×): Multi-floor, bike + car spots; busy-time 4-bikes-in-car-spot rule. OOP hierarchy + EnumMap queues.
- **Autocomplete with Top-K weighted completions** (3+×): LC 642. Trie + DFS + min-heap; update weights when user types a complete word.

### Uber — DSA Topics with Key LeetCode Numbers
- **Graphs**: LC 399 (×5+), 815 (×3+), 200, 210, 212, 236; novel: multi-source BFS with thief-radius, longest DAG path (Uber cab scenario), haunted house segment tree.
- **Trees**: LC 114 (flatten, ×2), 98; novel: Morris traversal (O(1) space BST), tree two-player game DP (L5), version compatibility chain.
- **DP**: LC 465 (×2), 295 (×2), 91/639, 174; novel: haunted house [L,R] constraints (segment tree), max K words/hr from stream.
- **Arrays / Binary Search**: LC 977 + kth-smallest-square follow-up (×2), 767 (×2), 994 (OA), 2009; novel: next smallest palindrome (×2), sliding window min-of-max.
- **LLD / Machine Coding**: Meeting Scheduler (×4), Parking Lot (×3), Cache+TTL+concurrency (×2), Text Editor Undo/Redo DLL (×2), Version Compatibility (×2, Uber-specific), Leaderboard cache+thread-safe (×2).

### Uber — Version Compatibility (Uber-specific machine coding)
- `addVersion(versionNum, isCompatibleWithPrev)` + `isCompatible(v1, v2)`.
- Key insight: track a `groupId` counter; increment on each **incompatible** release. All versions in the same group are mutually compatible.
- `isCompatible(v1, v2)` = `O(1)`: just compare `groupId[v1] == groupId[v2]`.
- Seen in 2+ independent reports (2021 + 2022); confirmed Uber-specific, not on LeetCode.

### Slice — Interview Round Structure
- Online Assessment: HackerRank, ~105 min — 15 MCQs (aptitude/quant) + 2 medium-hard coding problems.
- Technical Round 1 (~1h): DSA fundamentals — linked list, stack, tree problems + project discussion.
- Technical Round 2 (~1h20m): Harder DSA (rotated search, topological sort) + CS concepts (B-Tree, ACID, OOP pillars, indexing).
- Machine Coding Round (SDE-2, 90 min virtual): design + implement a full working application; must run and be tested.
  - Most repeated: Expense Sharing (EQUAL/EXACT/PERCENT), Extended Task Scheduler (TICK/UNDO/TASK AT).
  - Expectation: runnable code; interviewer asks you to justify DB choice and walk through design.
- DSA commonly asked from Striver's SDE sheet: Reverse LL, Cycle Detection, LCA, Max Path Sum, Search Rotated Array, Remove K Digits.
- HR/Managerial: sometimes includes a coding puzzle (e.g. Task Scheduler) alongside behavioural questions.

### Slice — Expense Sharing
- `Map<String, Double> netBalance` per user: positive = owed to them, negative = they owe.
- Balance update rule: for all participants debit their share; then credit payer the full amount. Works whether payer is or isn't a participant.
- EQUAL rounding: `base = floor(amount*100/n)/100`; first participant gets `amount - base*(n-1)` (the remainder cent).
- EXACT: validate that provided amounts sum to total (within $0.01); throw otherwise.
- PERCENT: validate percentages sum to 100; multiply each by amount/100 and round to 2dp.
- Complexity: O(p) per addExpense where p = number of participants; O(u) showAllBalances.

### Slice — Task Scheduler (Extended)
- `TreeMap<Integer, List<String>> schedule` keeps pending tasks sorted by time: O(log n) add, O(1) tick execution (just `remove(currentTime)`).
- `Deque<String[]> history` acts as undo stack: each entry is [taskId, scheduledTime].
- UNDO: peek history; if scheduledTime <= currentTime the task was already executed — report and skip. Otherwise remove from schedule.
- TICK: increment time; remove and execute schedule.get(currentTime) if present.
- Complexity: O(log n) addTask, O(k) tick where k = tasks due at that time, O(log n) undo.

## Git
- Remote: `https://github.com/Mayank9mare/Interview-Personal.git`
- Branch: `main`
- Push after every meaningful addition or conversion.
- Commit style: short imperative subject, body explains *why* not *what*.
- Remote: `https://github.com/Mayank9mare/Interview-Personal.git`
- Branch: `main`
- Push after every meaningful addition or conversion.
- Commit style: short imperative subject, body explains *why* not *what*.
