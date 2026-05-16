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

/rubrik/                    ← Rubrik-specific LLD/concurrency problems (Java)
  job-scheduler/            JobScheduler.java
  task-dependency/          TaskDependencyExecutor.java
  memory-block-tracker/     MemoryBlockTracker.java
  dems-and-reps/            DemsAndReps.java

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

## Git
- Remote: `https://github.com/Mayank9mare/Interview-Personal.git`
- Branch: `main`
- Push after every meaningful addition or conversion.
- Commit style: short imperative subject, body explains *why* not *what*.
