#include <bits/stdc++.h>
using namespace std;

// ---------------------------------------------------------------------------
// Stack / Queue Problems Reference - C++
// Covers: stack basics | queue basics | monotonic stack/queue | expression eval |
//         interval cleanup | parsing | BFS queue patterns | design questions
// ---------------------------------------------------------------------------

void printVec(const vector<int>& v) {
    for (int x : v) cout << x << " ";
    cout << endl;
}


// ---------------------------------------------------------------------------
// 1. Valid Parentheses
//    PROBLEM: Determine whether every opening bracket is closed by the same
//             type of bracket in the correct order.
//    MENTAL MODEL: "every closing bracket must match the most recent opener"
// ---------------------------------------------------------------------------
bool isValidParentheses(string s) {
    unordered_map<char, char> match{{')','('}, {']','['}, {'}','{'}};
    stack<char> st;

    for (char c : s) {
        if (c == '(' || c == '[' || c == '{') {
            st.push(c);
        } else {
            if (st.empty() || st.top() != match[c]) return false;
            st.pop();
        }
    }
    return st.empty();
}


// ---------------------------------------------------------------------------
// 2. Min Stack
//    PROBLEM: Design a stack that supports push, pop, top, and getMin in O(1).
//    MENTAL MODEL: "store current value and minimum so far together"
// ---------------------------------------------------------------------------
class MinStack {
    stack<pair<int, int>> st; // {value, min at this depth}

public:
    void push(int val) {
        int mn = st.empty() ? val : min(val, st.top().second);
        st.push({val, mn});
    }

    void pop() { st.pop(); }
    int top() { return st.top().first; }
    int getMin() { return st.top().second; }
};


// ---------------------------------------------------------------------------
// 3. Queue Using Stacks
//    PROBLEM: Implement FIFO queue operations using only stacks.
//    MENTAL MODEL: "inbox for pushes, outbox for pops; move only when needed"
// ---------------------------------------------------------------------------
class MyQueue {
    stack<int> in, out;

    void shift() {
        if (!out.empty()) return;
        while (!in.empty()) {
            out.push(in.top());
            in.pop();
        }
    }

public:
    void push(int x) { in.push(x); }

    int pop() {
        shift();
        int x = out.top();
        out.pop();
        return x;
    }

    int peek() {
        shift();
        return out.top();
    }

    bool empty() { return in.empty() && out.empty(); }
};


// ---------------------------------------------------------------------------
// 4. Stack Using Queue
//    PROBLEM: Implement LIFO stack operations using only a queue.
//    MENTAL MODEL: "rotate queue after push so newest item moves to front"
// ---------------------------------------------------------------------------
class MyStack {
    queue<int> q;

public:
    void push(int x) {
        q.push(x);
        for (int i = 0, n = q.size(); i < n - 1; i++) {
            q.push(q.front());
            q.pop();
        }
    }

    int pop() {
        int x = q.front();
        q.pop();
        return x;
    }

    int top() { return q.front(); }
    bool empty() { return q.empty(); }
};


// ---------------------------------------------------------------------------
// 5. Next Greater Element
//    PROBLEM: For each element, find the first greater element to its right;
//             return -1 when no such element exists.
//    MENTAL MODEL: "monotonic decreasing stack waits for a greater value"
// ---------------------------------------------------------------------------
vector<int> nextGreaterElements(vector<int>& nums) {
    vector<int> res(nums.size(), -1);
    stack<int> st; // indexes with no next greater yet

    for (int i = 0; i < (int)nums.size(); i++) {
        while (!st.empty() && nums[i] > nums[st.top()]) {
            res[st.top()] = nums[i];
            st.pop();
        }
        st.push(i);
    }
    return res;
}

vector<int> nextGreaterCircular(vector<int>& nums) {
    int n = nums.size();
    vector<int> res(n, -1);
    stack<int> st;

    for (int i = 0; i < 2 * n; i++) {
        int idx = i % n;
        while (!st.empty() && nums[idx] > nums[st.top()]) {
            res[st.top()] = nums[idx];
            st.pop();
        }
        if (i < n) st.push(idx);
    }
    return res;
}


// ---------------------------------------------------------------------------
// 6. Daily Temperatures
//    PROBLEM: For each day, return how many days must pass until a warmer
//             temperature occurs.
//    MENTAL MODEL: "same next-greater stack, but answer is distance"
// ---------------------------------------------------------------------------
vector<int> dailyTemperatures(vector<int>& temperatures) {
    vector<int> res(temperatures.size(), 0);
    stack<int> st;

    for (int i = 0; i < (int)temperatures.size(); i++) {
        while (!st.empty() && temperatures[i] > temperatures[st.top()]) {
            res[st.top()] = i - st.top();
            st.pop();
        }
        st.push(i);
    }
    return res;
}


// ---------------------------------------------------------------------------
// 7. Online Stock Span
//    PROBLEM: For each incoming stock price, return the number of consecutive
//             previous days with price less than or equal to today's price.
//    MENTAL MODEL: "compress previous smaller/equal prices into their span"
// ---------------------------------------------------------------------------
class StockSpanner {
    stack<pair<int, int>> st; // {price, span}

public:
    int next(int price) {
        int span = 1;
        while (!st.empty() && st.top().first <= price) {
            span += st.top().second;
            st.pop();
        }
        st.push({price, span});
        return span;
    }
};


// ---------------------------------------------------------------------------
// 8. Largest Rectangle in Histogram
//    PROBLEM: Given bar heights, return the largest rectangle area that can be
//             formed inside the histogram.
//    MENTAL MODEL: "increasing stack; when height drops, finalize rectangles"
// ---------------------------------------------------------------------------
int largestRectangleArea(vector<int>& heights) {
    stack<int> st;
    int best = 0;

    for (int i = 0; i <= (int)heights.size(); i++) {
        int h = (i == (int)heights.size()) ? 0 : heights[i];
        while (!st.empty() && h < heights[st.top()]) {
            int height = heights[st.top()];
            st.pop();
            int left = st.empty() ? -1 : st.top();
            int width = i - left - 1;
            best = max(best, height * width);
        }
        st.push(i);
    }
    return best;
}

int maximalRectangle(vector<vector<char>>& matrix) {
    if (matrix.empty()) return 0;

    int cols = matrix[0].size();
    vector<int> heights(cols, 0);
    int best = 0;

    for (auto& row : matrix) {
        for (int c = 0; c < cols; c++) {
            heights[c] = (row[c] == '1') ? heights[c] + 1 : 0;
        }
        best = max(best, largestRectangleArea(heights));
    }
    return best;
}


// ---------------------------------------------------------------------------
// 9. Remove K Digits
//    PROBLEM: Remove exactly k digits from a numeric string to make the smallest
//             possible non-negative number.
//    MENTAL MODEL: "remove previous bigger digits to make the number smaller"
// ---------------------------------------------------------------------------
string removeKdigits(string num, int k) {
    string st;

    for (char c : num) {
        while (!st.empty() && k > 0 && st.back() > c) {
            st.pop_back();
            k--;
        }
        st.push_back(c);
    }

    while (k-- > 0 && !st.empty()) st.pop_back();

    int i = 0;
    while (i < (int)st.size() && st[i] == '0') i++;
    string res = st.substr(i);
    return res.empty() ? "0" : res;
}


// ---------------------------------------------------------------------------
// 10. Decode String
//     PROBLEM: Decode strings like "3[a2[c]]" where numbers repeat bracketed
//              substrings.
//     MENTAL MODEL: "stack saves previous string and repeat count at '['"
// ---------------------------------------------------------------------------
string decodeString(string s) {
    stack<pair<string, int>> st;
    string cur;
    int num = 0;

    for (char c : s) {
        if (isdigit(c)) {
            num = num * 10 + (c - '0');
        } else if (c == '[') {
            st.push({cur, num});
            cur.clear();
            num = 0;
        } else if (c == ']') {
            auto top = st.top();
            st.pop();
            string expanded = top.first;
            while (top.second--) expanded += cur;
            cur = expanded;
        } else {
            cur += c;
        }
    }
    return cur;
}


// ---------------------------------------------------------------------------
// 11. Simplify Unix Path
//     PROBLEM: Convert an absolute Unix path into its canonical simplified form.
//     MENTAL MODEL: "stack keeps real directory names only"
// ---------------------------------------------------------------------------
string simplifyPath(string path) {
    vector<string> st;
    string token;
    stringstream ss(path);

    while (getline(ss, token, '/')) {
        if (token.empty() || token == ".") continue;
        if (token == "..") {
            if (!st.empty()) st.pop_back();
        } else {
            st.push_back(token);
        }
    }

    string res;
    for (string& dir : st) res += "/" + dir;
    return res.empty() ? "/" : res;
}


// ---------------------------------------------------------------------------
// 12. Evaluate Reverse Polish Notation
//     PROBLEM: Evaluate an arithmetic expression written in postfix notation.
//     MENTAL MODEL: "operator consumes the top two numbers"
// ---------------------------------------------------------------------------
int evalRPN(vector<string>& tokens) {
    stack<int> st;

    for (string& t : tokens) {
        if (t == "+" || t == "-" || t == "*" || t == "/") {
            int b = st.top(); st.pop();
            int a = st.top(); st.pop();
            if (t == "+") st.push(a + b);
            else if (t == "-") st.push(a - b);
            else if (t == "*") st.push(a * b);
            else st.push(a / b);
        } else {
            st.push(stoi(t));
        }
    }
    return st.top();
}


// ---------------------------------------------------------------------------
// 13. Basic Calculator II
//     PROBLEM: Evaluate a string expression containing non-negative integers and
//              +, -, *, / without parentheses.
//     MENTAL MODEL: "push signed terms; do * and / immediately"
// ---------------------------------------------------------------------------
int calculateII(string s) {
    vector<int> st;
    long num = 0;
    char op = '+';

    for (int i = 0; i <= (int)s.size(); i++) {
        char c = (i == (int)s.size()) ? '+' : s[i];
        if (c == ' ') continue;

        if (isdigit(c)) {
            num = num * 10 + (c - '0');
        } else {
            if (op == '+') st.push_back(num);
            else if (op == '-') st.push_back(-num);
            else if (op == '*') st.back() *= num;
            else if (op == '/') st.back() /= num;
            op = c;
            num = 0;
        }
    }

    return accumulate(st.begin(), st.end(), 0);
}


// ---------------------------------------------------------------------------
// 14. Basic Calculator I
//     PROBLEM: Evaluate a string expression containing +, -, spaces, and
//              parentheses.
//     MENTAL MODEL: "stack saves result and sign before '('"
// ---------------------------------------------------------------------------
int calculateI(string s) {
    stack<int> st;
    int result = 0;
    int sign = 1;
    int num = 0;

    for (int i = 0; i < (int)s.size(); i++) {
        char c = s[i];
        if (isdigit(c)) {
            num = num * 10 + (c - '0');
        } else if (c == '+') {
            result += sign * num;
            num = 0;
            sign = 1;
        } else if (c == '-') {
            result += sign * num;
            num = 0;
            sign = -1;
        } else if (c == '(') {
            st.push(result);
            st.push(sign);
            result = 0;
            sign = 1;
        } else if (c == ')') {
            result += sign * num;
            num = 0;
            result *= st.top(); st.pop(); // sign before '('
            result += st.top(); st.pop(); // result before '('
        }
    }
    return result + sign * num;
}


// ---------------------------------------------------------------------------
// 15. Asteroid Collision
//     PROBLEM: Simulate moving asteroids where positive values move right,
//              negative values move left, and smaller collisions explode.
//     MENTAL MODEL: "right-moving asteroids on stack can collide with left-moving"
// ---------------------------------------------------------------------------
vector<int> asteroidCollision(vector<int>& asteroids) {
    vector<int> st;

    for (int a : asteroids) {
        bool alive = true;
        while (alive && a < 0 && !st.empty() && st.back() > 0) {
            if (st.back() < -a) st.pop_back();
            else if (st.back() == -a) {
                st.pop_back();
                alive = false;
            } else {
                alive = false;
            }
        }
        if (alive) st.push_back(a);
    }
    return st;
}


// ---------------------------------------------------------------------------
// 16. Sliding Window Maximum
//     PROBLEM: Return the maximum value in every window of size k.
//     MENTAL MODEL: "deque stores candidate indexes in decreasing value order"
// ---------------------------------------------------------------------------
vector<int> maxSlidingWindow(vector<int>& nums, int k) {
    deque<int> dq;
    vector<int> res;

    for (int i = 0; i < (int)nums.size(); i++) {
        while (!dq.empty() && dq.front() <= i - k) dq.pop_front();
        while (!dq.empty() && nums[dq.back()] <= nums[i]) dq.pop_back();
        dq.push_back(i);

        if (i >= k - 1) res.push_back(nums[dq.front()]);
    }
    return res;
}


// ---------------------------------------------------------------------------
// 17. Shortest Subarray With Sum At Least K
//     PROBLEM: Find the length of the shortest non-empty subarray whose sum is
//              at least k; negative numbers are allowed.
//     MENTAL MODEL: "monotonic deque over prefix sums"
// ---------------------------------------------------------------------------
int shortestSubarray(vector<int>& nums, int k) {
    int n = nums.size();
    vector<long long> pref(n + 1, 0);
    for (int i = 0; i < n; i++) pref[i + 1] = pref[i] + nums[i];

    deque<int> dq;
    int best = n + 1;

    for (int i = 0; i <= n; i++) {
        while (!dq.empty() && pref[i] - pref[dq.front()] >= k) {
            best = min(best, i - dq.front());
            dq.pop_front();
        }
        while (!dq.empty() && pref[i] <= pref[dq.back()]) dq.pop_back();
        dq.push_back(i);
    }

    return best == n + 1 ? -1 : best;
}


// ---------------------------------------------------------------------------
// 18. First Non-Repeating Character in Stream
//     PROBLEM: After each incoming character, report the first character seen
//              so far that has appeared exactly once.
//     MENTAL MODEL: "queue keeps arrival order; counts remove repeated fronts"
// ---------------------------------------------------------------------------
string firstNonRepeatingStream(string s) {
    vector<int> freq(26, 0);
    queue<char> q;
    string res;

    for (char c : s) {
        freq[c - 'a']++;
        q.push(c);

        while (!q.empty() && freq[q.front() - 'a'] > 1) q.pop();
        res += q.empty() ? '#' : q.front();
    }
    return res;
}


// ---------------------------------------------------------------------------
// 19. Moving Average From Data Stream
//     PROBLEM: Maintain the average of the last size values in a stream.
//     MENTAL MODEL: "queue stores current window; subtract values that leave"
// ---------------------------------------------------------------------------
class MovingAverage {
    int size;
    long long sum = 0;
    queue<int> q;

public:
    MovingAverage(int size) : size(size) {}

    double next(int val) {
        q.push(val);
        sum += val;
        if ((int)q.size() > size) {
            sum -= q.front();
            q.pop();
        }
        return (double)sum / q.size();
    }
};


// ---------------------------------------------------------------------------
// 20. Circular Queue
//     PROBLEM: Design a fixed-size queue with wraparound indexing.
//     MENTAL MODEL: "fixed array plus head index and current count"
// ---------------------------------------------------------------------------
class MyCircularQueue {
    vector<int> data;
    int head = 0;
    int count = 0;

public:
    MyCircularQueue(int k) : data(k) {}

    bool enQueue(int value) {
        if (isFull()) return false;
        int tail = (head + count) % data.size();
        data[tail] = value;
        count++;
        return true;
    }

    bool deQueue() {
        if (isEmpty()) return false;
        head = (head + 1) % data.size();
        count--;
        return true;
    }

    int Front() { return isEmpty() ? -1 : data[head]; }

    int Rear() {
        if (isEmpty()) return -1;
        int tail = (head + count - 1) % data.size();
        return data[tail];
    }

    bool isEmpty() { return count == 0; }
    bool isFull() { return count == (int)data.size(); }
};


// ---------------------------------------------------------------------------
// 21. Rotting Oranges
//     PROBLEM: Given a grid of fresh and rotten oranges, return minutes until
//              all fresh oranges rot, or -1 if impossible.
//     MENTAL MODEL: "multi-source BFS; each queue layer is one minute"
// ---------------------------------------------------------------------------
int orangesRotting(vector<vector<int>>& grid) {
    int m = grid.size();
    int n = grid[0].size();
    queue<pair<int, int>> q;
    int fresh = 0;

    for (int r = 0; r < m; r++) {
        for (int c = 0; c < n; c++) {
            if (grid[r][c] == 2) q.push({r, c});
            if (grid[r][c] == 1) fresh++;
        }
    }

    int minutes = 0;
    int dirs[5] = {1, 0, -1, 0, 1};
    while (!q.empty() && fresh > 0) {
        int sz = q.size();
        minutes++;
        while (sz--) {
            auto cell = q.front();
            q.pop();
            int r = cell.first;
            int c = cell.second;

            for (int d = 0; d < 4; d++) {
                int nr = r + dirs[d];
                int nc = c + dirs[d + 1];
                if (nr < 0 || nc < 0 || nr >= m || nc >= n || grid[nr][nc] != 1) continue;
                grid[nr][nc] = 2;
                fresh--;
                q.push({nr, nc});
            }
        }
    }

    return fresh == 0 ? minutes : -1;
}


// ---------------------------------------------------------------------------
// 22. LRU Cache
//     PROBLEM: Design a cache with O(1) get/put that evicts the least recently
//              used key when capacity is exceeded.
//     MENTAL MODEL: "list stores recency order; hash map jumps to list nodes"
// ---------------------------------------------------------------------------
class LRUCache {
    int cap;
    list<pair<int, int>> items; // front = most recent, back = least recent
    unordered_map<int, list<pair<int, int>>::iterator> pos;

public:
    LRUCache(int capacity) : cap(capacity) {}

    int get(int key) {
        if (!pos.count(key)) return -1;
        items.splice(items.begin(), items, pos[key]);
        return pos[key]->second;
    }

    void put(int key, int value) {
        if (pos.count(key)) {
            pos[key]->second = value;
            items.splice(items.begin(), items, pos[key]);
            return;
        }

        if ((int)items.size() == cap) {
            pos.erase(items.back().first);
            items.pop_back();
        }

        items.push_front({key, value});
        pos[key] = items.begin();
    }
};


int main() {
    cout << "=== Stack Basics ===" << endl;
    cout << "Valid parentheses: " << boolalpha << isValidParentheses("({[]})") << endl;
    MinStack ms;
    ms.push(3); ms.push(1); ms.push(2);
    cout << "Min stack min: " << ms.getMin() << endl;

    cout << "\n=== Queue/Stack Design ===" << endl;
    MyQueue q;
    q.push(10); q.push(20);
    cout << "Queue pop: " << q.pop() << endl;
    MyStack st;
    st.push(10); st.push(20);
    cout << "Stack pop: " << st.pop() << endl;

    cout << "\n=== Monotonic Stack ===" << endl;
    vector<int> nge{2,1,2,4,3};
    printVec(nextGreaterElements(nge));
    vector<int> circ{1,2,1};
    printVec(nextGreaterCircular(circ));
    vector<int> temps{73,74,75,71,69,72,76,73};
    printVec(dailyTemperatures(temps));
    StockSpanner sp;
    for (int price : {100,80,60,70,60,75,85}) cout << sp.next(price) << " ";
    cout << endl;

    cout << "\n=== Rectangles / Greedy Stack ===" << endl;
    vector<int> hist{2,1,5,6,2,3};
    cout << "Largest rectangle: " << largestRectangleArea(hist) << endl;
    vector<vector<char>> matrix{{'1','0','1','0','0'}, {'1','0','1','1','1'}, {'1','1','1','1','1'}, {'1','0','0','1','0'}};
    cout << "Maximal rectangle: " << maximalRectangle(matrix) << endl;
    cout << "Remove k digits: " << removeKdigits("1432219", 3) << endl;

    cout << "\n=== Parsing / Expressions ===" << endl;
    cout << "Decode: " << decodeString("3[a2[c]]") << endl;
    cout << "Path: " << simplifyPath("/a/./b/../../c/") << endl;
    vector<string> rpn{"2","1","+","3","*"};
    cout << "RPN: " << evalRPN(rpn) << endl;
    cout << "Calc II: " << calculateII("3+2*2") << endl;
    cout << "Calc I: " << calculateI("(1+(4+5+2)-3)+(6+8)") << endl;

    cout << "\n=== Collision / Monotonic Queue ===" << endl;
    vector<int> ast{5,10,-5};
    printVec(asteroidCollision(ast));
    vector<int> sw{1,3,-1,-3,5,3,6,7};
    printVec(maxSlidingWindow(sw, 3));
    vector<int> shortest{2,-1,2};
    cout << "Shortest subarray >= 3: " << shortestSubarray(shortest, 3) << endl;

    cout << "\n=== Queue Streams ===" << endl;
    cout << "First non-repeating stream: " << firstNonRepeatingStream("aabc") << endl;
    MovingAverage ma(3);
    cout << ma.next(1) << " " << ma.next(10) << " " << ma.next(3) << " " << ma.next(5) << endl;

    cout << "\n=== Circular Queue / BFS ===" << endl;
    MyCircularQueue cq(3);
    cq.enQueue(1); cq.enQueue(2); cq.enQueue(3);
    cout << "Circular rear: " << cq.Rear() << endl;
    vector<vector<int>> oranges{{2,1,1},{1,1,0},{0,1,1}};
    cout << "Rotting oranges: " << orangesRotting(oranges) << endl;

    cout << "\n=== LRU Cache ===" << endl;
    LRUCache cache(2);
    cache.put(1, 1);
    cache.put(2, 2);
    cout << cache.get(1) << endl;
    cache.put(3, 3);
    cout << cache.get(2) << endl;

    return 0;
}
