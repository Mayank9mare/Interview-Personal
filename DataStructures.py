# DataStructures.py — Python built-in and standard-library data structures
# Run: python DataStructures.py

import heapq
import bisect
import array
import queue
from collections import deque, defaultdict, Counter, OrderedDict, namedtuple
from dataclasses import dataclass, field
from typing import List

# ─────────────────────────────────────────────────────────────────────────────
# 1. list — dynamic array (like Java ArrayList)
#    O(1) append / pop from end
#    O(n) insert / remove at arbitrary index (shifts elements)
#    O(1) index access
#    Slicing creates a new list: lst[start:stop:step]
# ─────────────────────────────────────────────────────────────────────────────
def list_demo():
    lst = [3, 1, 4, 1, 5, 9, 2, 6]

    lst.append(7)            # O(1) — add to end
    lst.insert(0, 0)         # O(n) — add at index 0
    lst.pop()                # O(1) — remove last
    lst.pop(0)               # O(n) — remove at index 0
    lst.remove(1)            # O(n) — remove first occurrence of value

    print(f"  sorted: {sorted(lst)}")           # new list, O(n log n)
    lst.sort(reverse=True)                       # in-place
    print(f"  after sort desc: {lst}")

    # list comprehension — idiomatic Python
    squares = [x * x for x in range(10) if x % 2 == 0]
    print(f"  even squares: {squares}")

    # slicing
    print(f"  first 3: {lst[:3]}")
    print(f"  reversed: {lst[::-1]}")

    # stack (LIFO) — use list's append/pop
    stack = []
    stack.append(1); stack.append(2); stack.append(3)
    print(f"  stack pop: {stack.pop()}")  # 3

    # common patterns
    print(f"  min={min(lst)}, max={max(lst)}, sum={sum(lst)}")
    print(f"  count of 5: {lst.count(5)}")
    print(f"  index of 5: {lst.index(5)}")


# ─────────────────────────────────────────────────────────────────────────────
# 2. dict — hash map (like Java HashMap; ordered by insertion since Python 3.7)
#    O(1) average get / set / delete
#    O(n) worst-case (hash collision)
#    Keys must be hashable (immutable: str, int, tuple — not list/set)
# ─────────────────────────────────────────────────────────────────────────────
def dict_demo():
    d = {"a": 1, "b": 2, "c": 3}

    d["d"] = 4                        # insert / update O(1)
    del d["a"]                        # delete O(1)
    val = d.get("z", 0)              # safe get with default; avoids KeyError
    print(f"  get missing: {val}")

    # iteration
    for k, v in d.items():
        print(f"  {k}: {v}")

    # dict comprehension
    squared = {k: v ** 2 for k, v in d.items()}
    print(f"  squared: {squared}")

    # setdefault — insert only if key absent
    d.setdefault("e", 99)
    print(f"  setdefault: {d['e']}")

    # merge (Python 3.9+)
    extra = {"f": 6, "g": 7}
    merged = d | extra              # new dict
    d |= extra                      # in-place merge
    print(f"  merged keys: {list(d.keys())}")

    # pop with default
    removed = d.pop("z", None)
    print(f"  pop missing: {removed}")


# ─────────────────────────────────────────────────────────────────────────────
# 3. set / frozenset — hash set
#    O(1) average add / remove / contains
#    Unordered; no duplicate elements
#    frozenset — immutable set (hashable; can be used as dict key)
# ─────────────────────────────────────────────────────────────────────────────
def set_demo():
    s = {1, 2, 3, 4}
    s.add(5)
    s.discard(10)    # remove if present; no error if absent (remove() raises KeyError)
    print(f"  set: {s}")
    print(f"  3 in s: {3 in s}")   # O(1)

    a, b = {1, 2, 3}, {2, 3, 4}
    print(f"  union: {a | b}")
    print(f"  intersection: {a & b}")
    print(f"  difference (a-b): {a - b}")
    print(f"  symmetric diff: {a ^ b}")
    print(f"  a subset of b: {a <= b}")

    # set comprehension
    evens = {x for x in range(10) if x % 2 == 0}
    print(f"  even set: {evens}")

    # frozenset as dict key
    fs = frozenset([1, 2, 3])
    lookup = {fs: "triangle"}
    print(f"  frozenset key: {lookup[fs]}")


# ─────────────────────────────────────────────────────────────────────────────
# 4. tuple — immutable ordered sequence
#    O(1) index access, O(n) search
#    Hashable (if all elements are) → can be dict key or set element
#    Use over list when data should not change (coordinates, RGB, DB row)
#    Named access via index or namedtuple
# ─────────────────────────────────────────────────────────────────────────────
def tuple_demo():
    t = (10, 20, 30)
    x, y, z = t         # unpacking
    print(f"  unpacked: {x}, {y}, {z}")

    # single-element tuple needs trailing comma
    single = (42,)
    print(f"  single: {single}, type: {type(single)}")

    # tuple as dict key
    coords = {(0, 0): "origin", (1, 0): "right"}
    print(f"  coord lookup: {coords[(0, 0)]}")

    # swap without temp — uses tuple packing/unpacking
    a, b = 1, 2
    a, b = b, a
    print(f"  swapped: a={a}, b={b}")


# ─────────────────────────────────────────────────────────────────────────────
# 5. collections.deque — double-ended queue (like Java ArrayDeque)
#    O(1) append / appendleft / pop / popleft from either end
#    O(n) random access by index
#    Use for: BFS queue, sliding window, LRU cache backing structure
#    maxlen — fixed-size; old elements auto-dropped from the opposite end
# ─────────────────────────────────────────────────────────────────────────────
def deque_demo():
    dq = deque([1, 2, 3])
    dq.append(4)          # add right
    dq.appendleft(0)      # add left
    dq.pop()              # remove right
    dq.popleft()          # remove left
    print(f"  deque: {dq}")

    dq.rotate(1)          # rotate right by 1 (last goes to front)
    print(f"  after rotate: {dq}")

    # fixed-size sliding window — auto-evicts oldest
    window = deque(maxlen=3)
    for i in range(6):
        window.append(i)
        print(f"  window after {i}: {list(window)}")

    # BFS template
    from collections import deque as q
    bfs_queue = q([0])
    while bfs_queue:
        node = bfs_queue.popleft()
        # process node, then: bfs_queue.append(neighbor)
        break  # just showing the pattern


# ─────────────────────────────────────────────────────────────────────────────
# 6. collections.defaultdict — dict that auto-initialises missing keys
#    Avoids KeyError and explicit if-key-not-in-dict checks.
#    defaultdict(list)  → missing key gets []
#    defaultdict(int)   → missing key gets 0
#    defaultdict(set)   → missing key gets set()
# ─────────────────────────────────────────────────────────────────────────────
def defaultdict_demo():
    # group words by first letter
    words = ["apple", "ant", "bat", "bee", "cat"]
    groups: defaultdict = defaultdict(list)
    for w in words:
        groups[w[0]].append(w)
    print(f"  groups: {dict(groups)}")

    # frequency count with int (same as Counter but more flexible)
    freq: defaultdict = defaultdict(int)
    for w in words:
        freq[w[0]] += 1
    print(f"  freq: {dict(freq)}")

    # adjacency list for a graph
    graph: defaultdict = defaultdict(list)
    edges = [(0, 1), (0, 2), (1, 3)]
    for u, v in edges:
        graph[u].append(v)
    print(f"  adj list: {dict(graph)}")


# ─────────────────────────────────────────────────────────────────────────────
# 7. collections.Counter — multiset; counts occurrences
#    O(n) to build; O(1) per lookup
#    most_common(k)  — top k elements by count
#    Supports +, -, &, | between counters
# ─────────────────────────────────────────────────────────────────────────────
def counter_demo():
    text = "abracadabra"
    c = Counter(text)
    print(f"  counter: {c}")
    print(f"  most common 3: {c.most_common(3)}")

    # arithmetic
    c1 = Counter(a=3, b=2)
    c2 = Counter(a=1, b=4)
    print(f"  add: {c1 + c2}")
    print(f"  subtract: {c1 - c2}")     # drops non-positive counts
    print(f"  intersect (min): {c1 & c2}")
    print(f"  union (max): {c1 | c2}")

    # elements() — expand back to iterable
    print(f"  elements: {sorted(Counter(a=2, b=3).elements())}")

    # word frequency
    words = "to be or not to be".split()
    wc = Counter(words)
    print(f"  word count: {wc}")


# ─────────────────────────────────────────────────────────────────────────────
# 8. collections.namedtuple / dataclass
#    namedtuple — lightweight immutable record; fields by name or index
#    dataclass  — mutable by default; supports defaults, __post_init__, ordering
# ─────────────────────────────────────────────────────────────────────────────
def namedtuple_dataclass_demo():
    # namedtuple — like a tuple with named fields
    Point = namedtuple("Point", ["x", "y"])
    p = Point(3, 4)
    print(f"  namedtuple: {p}, x={p.x}, y={p.y}")
    print(f"  as dict: {p._asdict()}")
    p2 = p._replace(y=10)          # returns new instance (immutable)
    print(f"  replaced: {p2}")

    # dataclass — cleaner syntax for mutable records
    @dataclass
    class Employee:
        name: str
        dept: str
        salary: float = 0.0
        skills: List[str] = field(default_factory=list)  # mutable default

        def give_raise(self, pct):
            self.salary *= (1 + pct)

    emp = Employee("Alice", "Eng", 100_000)
    emp.skills.append("Python")
    emp.give_raise(0.1)
    print(f"  employee: {emp}")

    # frozen dataclass — immutable, hashable (like namedtuple but with type hints)
    @dataclass(frozen=True)
    class RGB:
        r: int; g: int; b: int

    color = RGB(255, 0, 0)
    lookup = {color: "red"}
    print(f"  frozen dc key: {lookup[color]}")


# ─────────────────────────────────────────────────────────────────────────────
# 9. heapq — min-heap (binary heap)
#    O(log n) push / pop; O(1) peek (heap[0])
#    Python only has min-heap; for max-heap: negate values on push, negate on pop
#    heappush(h, item)       — push
#    heappop(h)              — pop smallest
#    heapify(lst)            — in-place O(n) build
#    nlargest(k, iterable)   — top k largest O(n log k)
#    nsmallest(k, iterable)  — top k smallest
#    Push tuples for priority queues: heappush(h, (priority, item))
# ─────────────────────────────────────────────────────────────────────────────
def heapq_demo():
    h = []
    for v in [5, 1, 3, 2, 4]:
        heapq.heappush(h, v)

    print(f"  heap peek (min): {h[0]}")
    while h:
        print(f"  pop: {heapq.heappop(h)}", end=" ")
    print()

    # heapify — convert list in-place
    lst = [9, 4, 7, 1, 5]
    heapq.heapify(lst)
    print(f"  heapified: {lst}")

    # max-heap trick — negate
    max_h = []
    for v in [5, 1, 3, 2, 4]:
        heapq.heappush(max_h, -v)
    print(f"  max-heap pop: {-heapq.heappop(max_h)}")  # 5

    # priority queue with (priority, item) tuples
    tasks = []
    heapq.heappush(tasks, (3, "low"))
    heapq.heappush(tasks, (1, "high"))
    heapq.heappush(tasks, (2, "medium"))
    while tasks:
        pri, task = heapq.heappop(tasks)
        print(f"  task ({pri}): {task}")

    # nlargest / nsmallest
    data = [3, 1, 4, 1, 5, 9, 2, 6]
    print(f"  3 largest: {heapq.nlargest(3, data)}")
    print(f"  3 smallest: {heapq.nsmallest(3, data)}")


# ─────────────────────────────────────────────────────────────────────────────
# 10. bisect — binary search on a sorted list
#     O(log n) search; O(n) insert (due to list shifting)
#     bisect_left(lst, x)   — index of leftmost position to insert x
#     bisect_right(lst, x)  — index of rightmost position to insert x
#     insort_left / insort_right — insert and maintain sorted order
#     Use to replace a sorted set / multiset when order and rank queries matter.
# ─────────────────────────────────────────────────────────────────────────────
def bisect_demo():
    lst = [1, 2, 4, 4, 5, 7, 9]

    # search
    idx = bisect.bisect_left(lst, 4)    # 2 — first position of 4
    idx_r = bisect.bisect_right(lst, 4) # 4 — after last 4
    print(f"  bisect_left(4)={idx}, bisect_right(4)={idx_r}")

    # count occurrences in O(log n)
    count = bisect.bisect_right(lst, 4) - bisect.bisect_left(lst, 4)
    print(f"  count of 4: {count}")

    # insert maintaining order
    bisect.insort(lst, 6)
    print(f"  after insort(6): {lst}")

    # rank of element (how many elements are strictly less)
    rank = bisect.bisect_left(lst, 5)
    print(f"  rank of 5: {rank}")

    # floor / ceiling
    def floor_val(lst, x):
        i = bisect.bisect_right(lst, x)
        return lst[i - 1] if i > 0 else None

    def ceil_val(lst, x):
        i = bisect.bisect_left(lst, x)
        return lst[i] if i < len(lst) else None

    print(f"  floor(6)={floor_val(lst, 6)}, ceil(6)={ceil_val(lst, 6)}")


# ─────────────────────────────────────────────────────────────────────────────
# 11. queue module — thread-safe queues (unlike deque)
#     queue.Queue(maxsize)   — FIFO; blocks on put/get when full/empty
#     queue.LifoQueue        — stack (LIFO)
#     queue.PriorityQueue    — min-heap; items must be comparable
#     get(block, timeout)    — timeout raises queue.Empty
#     put(block, timeout)    — timeout raises queue.Full
#     task_done() + join()   — coordination: join() blocks until all items processed
# ─────────────────────────────────────────────────────────────────────────────
def queue_module_demo():
    # FIFO
    q = queue.Queue(maxsize=3)
    for i in range(3): q.put(i)
    print(f"  FIFO: {[q.get() for _ in range(3)]}")

    # LIFO
    lq = queue.LifoQueue()
    for i in range(3): lq.put(i)
    print(f"  LIFO: {[lq.get() for _ in range(3)]}")

    # PriorityQueue
    pq = queue.PriorityQueue()
    pq.put((2, "medium"))
    pq.put((1, "high"))
    pq.put((3, "low"))
    print(f"  PQ: {[pq.get() for _ in range(3)]}")

    # non-blocking get
    eq = queue.Queue()
    try:
        eq.get_nowait()
    except queue.Empty:
        print("  Queue was empty (get_nowait raised Empty)")


# ─────────────────────────────────────────────────────────────────────────────
# 12. array.array — typed, memory-efficient array (like Java primitive array)
#     Much more memory-efficient than list for large numeric data.
#     Type codes: 'i'=int, 'f'=float, 'd'=double, 'b'=signed byte, etc.
#     Same O(1) indexing as list; slower for mixed-type work.
#     For heavy numeric work, prefer numpy arrays.
# ─────────────────────────────────────────────────────────────────────────────
def array_demo():
    arr = array.array('i', [1, 2, 3, 4, 5])  # signed int array
    arr.append(6)
    arr.insert(0, 0)
    print(f"  array: {arr}")
    print(f"  index 3: {arr[3]}")

    # memory comparison (rough)
    import sys
    lst = list(range(1000))
    arr_big = array.array('i', range(1000))
    print(f"  list size: {sys.getsizeof(lst)} bytes")
    print(f"  array size: {sys.getsizeof(arr_big)} bytes")


if __name__ == "__main__":
    print("=== list ===");             list_demo()
    print("\n=== dict ===");            dict_demo()
    print("\n=== set / frozenset ==="); set_demo()
    print("\n=== tuple ===");           tuple_demo()
    print("\n=== deque ===");           deque_demo()
    print("\n=== defaultdict ===");     defaultdict_demo()
    print("\n=== Counter ===");         counter_demo()
    print("\n=== namedtuple / dataclass ==="); namedtuple_dataclass_demo()
    print("\n=== heapq ===");           heapq_demo()
    print("\n=== bisect ===");          bisect_demo()
    print("\n=== queue module ===");    queue_module_demo()
    print("\n=== array ===");           array_demo()
