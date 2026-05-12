#include <bits/stdc++.h>
using namespace std;

// ---------------------------------------------------------------------------
// Bit Manipulation Problems Reference - C++
// Covers: bit basics | masks | XOR tricks | counting bits | subsets |
//         missing/unique numbers | bit DP helpers | trie for max XOR
// ---------------------------------------------------------------------------

void printVec(const vector<int>& v) {
    for (int x : v) cout << x << " ";
    cout << endl;
}


// ---------------------------------------------------------------------------
// 1. Basic Bit Operations
//    PROBLEM: Check, set, clear, and toggle a specific bit in an integer.
//    MENTAL MODEL: "bit i is controlled by mask (1 << i)"
// ---------------------------------------------------------------------------
bool isBitSet(int n, int i) {
    return (n & (1 << i)) != 0;
}

int setBit(int n, int i) {
    return n | (1 << i);
}

int clearBit(int n, int i) {
    return n & ~(1 << i);
}

int toggleBit(int n, int i) {
    return n ^ (1 << i);
}


// ---------------------------------------------------------------------------
// 2. Power of Two
//    PROBLEM: Determine whether an integer is a positive power of two.
//    MENTAL MODEL: "powers of two have exactly one set bit"
//    n & (n - 1) removes the lowest set bit.
// ---------------------------------------------------------------------------
bool isPowerOfTwo(int n) {
    return n > 0 && (n & (n - 1)) == 0;
}


// ---------------------------------------------------------------------------
// 3. Count Set Bits
//    PROBLEM: Count how many 1 bits are present in an integer.
//    MENTAL MODEL: "remove one set bit at a time"
// ---------------------------------------------------------------------------
int countSetBits(int n) {
    int count = 0;
    while (n) {
        n &= (n - 1);
        count++;
    }
    return count;
}


// ---------------------------------------------------------------------------
// 4. Counting Bits From 0 to n
//    PROBLEM: For every number from 0 to n, return its count of set bits.
//    MENTAL MODEL: "bits[i] = bits[i without lowest bit] + 1"
// ---------------------------------------------------------------------------
vector<int> countBits(int n) {
    vector<int> bits(n + 1, 0);
    for (int i = 1; i <= n; i++) {
        bits[i] = bits[i & (i - 1)] + 1;
    }
    return bits;
}


// ---------------------------------------------------------------------------
// 5. Single Number
//    PROBLEM: Every number appears twice except one; return the unique number.
//    MENTAL MODEL: "x ^ x cancels to 0; 0 ^ x returns x"
// ---------------------------------------------------------------------------
int singleNumber(vector<int>& nums) {
    int ans = 0;
    for (int x : nums) ans ^= x;
    return ans;
}


// ---------------------------------------------------------------------------
// 6. Single Number II
//    PROBLEM: Every number appears three times except one; return the unique
//             number.
//    MENTAL MODEL: "count each bit modulo 3"
//    Every repeated value contributes 3 copies, so only the unique bits remain.
// ---------------------------------------------------------------------------
int singleNumberII(vector<int>& nums) {
    int ans = 0;

    for (int bit = 0; bit < 32; bit++) {
        int count = 0;
        for (int x : nums) {
            if ((x >> bit) & 1) count++;
        }
        if (count % 3) ans |= (1 << bit);
    }
    return ans;
}


// ---------------------------------------------------------------------------
// 7. Single Number III
//    PROBLEM: Exactly two numbers appear once and all others appear twice;
//             return the two unique numbers.
//    MENTAL MODEL: "xor of two unique numbers has at least one differing bit"
//    Use that bit to split numbers into two groups.
// ---------------------------------------------------------------------------
vector<int> singleNumberIII(vector<int>& nums) {
    int xr = 0;
    for (int x : nums) xr ^= x;

    int diffBit = xr & -xr; // lowest set bit where the two answers differ
    int a = 0, b = 0;

    for (int x : nums) {
        if (x & diffBit) a ^= x;
        else b ^= x;
    }
    return {a, b};
}


// ---------------------------------------------------------------------------
// 8. Missing Number
//    PROBLEM: Given n numbers from range [0, n] with one missing, return the
//             missing number.
//    MENTAL MODEL: "xor all indexes and values; duplicates cancel"
// ---------------------------------------------------------------------------
int missingNumberXor(vector<int>& nums) {
    int ans = nums.size();
    for (int i = 0; i < (int)nums.size(); i++) {
        ans ^= i;
        ans ^= nums[i];
    }
    return ans;
}


// ---------------------------------------------------------------------------
// 9. Find Duplicate and Missing
//    PROBLEM: Given numbers 1..n where one value is duplicated and one is
//             missing, return both values.
//    MENTAL MODEL: "xor gives duplicate ^ missing; split by a differing bit"
// ---------------------------------------------------------------------------
vector<int> findDuplicateAndMissing(vector<int>& nums) {
    int n = nums.size();
    int xr = 0;

    for (int i = 1; i <= n; i++) xr ^= i;
    for (int x : nums) xr ^= x;

    int diffBit = xr & -xr;
    int a = 0, b = 0;

    for (int i = 1; i <= n; i++) {
        if (i & diffBit) a ^= i;
        else b ^= i;
    }
    for (int x : nums) {
        if (x & diffBit) a ^= x;
        else b ^= x;
    }

    for (int x : nums) {
        if (x == a) return {a, b}; // {duplicate, missing}
    }
    return {b, a};
}


// ---------------------------------------------------------------------------
// 10. Reverse Bits
//     PROBLEM: Reverse the bit order of a 32-bit unsigned integer.
//     MENTAL MODEL: "shift result left, copy n's current lowest bit"
// ---------------------------------------------------------------------------
uint32_t reverseBits(uint32_t n) {
    uint32_t res = 0;
    for (int i = 0; i < 32; i++) {
        res <<= 1;
        res |= (n & 1);
        n >>= 1;
    }
    return res;
}


// ---------------------------------------------------------------------------
// 11. Hamming Distance
//     PROBLEM: Count how many bit positions differ between two integers.
//     MENTAL MODEL: "different bits are 1 in x ^ y"
// ---------------------------------------------------------------------------
int hammingDistance(int x, int y) {
    return countSetBits(x ^ y);
}


// ---------------------------------------------------------------------------
// 12. Total Hamming Distance
//     PROBLEM: Given an array, sum the Hamming distance across every pair.
//     MENTAL MODEL: "for each bit, zeros * ones pairs differ"
// ---------------------------------------------------------------------------
int totalHammingDistance(vector<int>& nums) {
    int n = nums.size();
    int total = 0;

    for (int bit = 0; bit < 32; bit++) {
        int ones = 0;
        for (int x : nums) ones += (x >> bit) & 1;
        total += ones * (n - ones);
    }
    return total;
}


// ---------------------------------------------------------------------------
// 13. Bitmask Subsets
//     PROBLEM: Generate all subsets of a list using bit masks.
//     MENTAL MODEL: "each bit decides whether to include one element"
// ---------------------------------------------------------------------------
vector<vector<int>> subsets(vector<int>& nums) {
    int n = nums.size();
    vector<vector<int>> res;

    for (int mask = 0; mask < (1 << n); mask++) {
        vector<int> cur;
        for (int i = 0; i < n; i++) {
            if (mask & (1 << i)) cur.push_back(nums[i]);
        }
        res.push_back(cur);
    }
    return res;
}


// ---------------------------------------------------------------------------
// 14. Submasks of a Mask
//     PROBLEM: Enumerate every submask contained inside a given bitmask.
//     MENTAL MODEL: "(sub - 1) & mask jumps to the next smaller submask"
// ---------------------------------------------------------------------------
vector<int> allSubmasks(int mask) {
    vector<int> res;
    for (int sub = mask; sub; sub = (sub - 1) & mask) {
        res.push_back(sub);
    }
    res.push_back(0);
    return res;
}


// ---------------------------------------------------------------------------
// 15. Maximum Product of Word Lengths
//     PROBLEM: Find the largest product of lengths of two words that share no
//              common letters.
//     MENTAL MODEL: "word mask stores which letters exist; no common letters if AND is 0"
// ---------------------------------------------------------------------------
int maxProductWords(vector<string>& words) {
    int n = words.size();
    vector<int> mask(n, 0);

    for (int i = 0; i < n; i++) {
        for (char c : words[i]) mask[i] |= 1 << (c - 'a');
    }

    int best = 0;
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if ((mask[i] & mask[j]) == 0) {
                best = max(best, (int)words[i].size() * (int)words[j].size());
            }
        }
    }
    return best;
}


// ---------------------------------------------------------------------------
// 16. Repeated DNA Sequences
//     PROBLEM: Return all 10-letter DNA substrings that appear more than once.
//     MENTAL MODEL: "encode each DNA char in 2 bits; sliding hash of 10 chars"
// ---------------------------------------------------------------------------
vector<string> findRepeatedDnaSequences(string s) {
    if (s.size() < 10) return {};

    unordered_map<char, int> val{{'A',0}, {'C',1}, {'G',2}, {'T',3}};
    unordered_set<int> seen, added;
    vector<string> res;
    int mask = 0;
    int keep20Bits = (1 << 20) - 1;

    for (int i = 0; i < (int)s.size(); i++) {
        mask = ((mask << 2) | val[s[i]]) & keep20Bits;
        if (i < 9) continue;

        if (seen.count(mask) && !added.count(mask)) {
            res.push_back(s.substr(i - 9, 10));
            added.insert(mask);
        }
        seen.insert(mask);
    }
    return res;
}


// ---------------------------------------------------------------------------
// 17. Bitwise AND of Numbers Range
//     PROBLEM: Compute the bitwise AND of every number in the inclusive range
//              [left, right].
//     MENTAL MODEL: "only the common left prefix survives"
// ---------------------------------------------------------------------------
int rangeBitwiseAnd(int left, int right) {
    int shifts = 0;
    while (left < right) {
        left >>= 1;
        right >>= 1;
        shifts++;
    }
    return left << shifts;
}


// ---------------------------------------------------------------------------
// 18. Sum of Two Integers
//     PROBLEM: Add two integers without using the + or - operators.
//     MENTAL MODEL: "xor adds without carry; and+shift is carry"
// ---------------------------------------------------------------------------
int getSum(int a, int b) {
    while (b != 0) {
        unsigned carry = (unsigned)(a & b) << 1;
        a = a ^ b;
        b = carry;
    }
    return a;
}


// ---------------------------------------------------------------------------
// 19. Divide Two Integers
//     PROBLEM: Divide two integers without using multiplication, division, or
//              modulo, truncating toward zero.
//     MENTAL MODEL: "subtract largest shifted divisor each time"
// ---------------------------------------------------------------------------
int divideIntegers(int dividend, int divisor) {
    if (dividend == INT_MIN && divisor == -1) return INT_MAX;

    long long a = llabs((long long)dividend);
    long long b = llabs((long long)divisor);
    int sign = ((dividend < 0) ^ (divisor < 0)) ? -1 : 1;
    long long ans = 0;

    for (int bit = 31; bit >= 0; bit--) {
        if ((a >> bit) >= b) {
            ans += 1LL << bit;
            a -= b << bit;
        }
    }

    return sign == 1 ? ans : -ans;
}


// ---------------------------------------------------------------------------
// 20. Gray Code
//     PROBLEM: Generate an n-bit Gray code sequence.
//     MENTAL MODEL: "gray(i) = i ^ (i >> 1); adjacent codes differ by one bit"
// ---------------------------------------------------------------------------
vector<int> grayCode(int n) {
    vector<int> res;
    for (int i = 0; i < (1 << n); i++) {
        res.push_back(i ^ (i >> 1));
    }
    return res;
}


// ---------------------------------------------------------------------------
// 21. UTF-8 Validation
//     PROBLEM: Determine whether a sequence of integers represents valid UTF-8
//              encoded bytes.
//     MENTAL MODEL: "leading byte tells how many continuation bytes must follow"
// ---------------------------------------------------------------------------
bool validUtf8(vector<int>& data) {
    int need = 0;

    for (int x : data) {
        if (need == 0) {
            if ((x >> 7) == 0) need = 0;
            else if ((x >> 5) == 0b110) need = 1;
            else if ((x >> 4) == 0b1110) need = 2;
            else if ((x >> 3) == 0b11110) need = 3;
            else return false;
        } else {
            if ((x >> 6) != 0b10) return false;
            need--;
        }
    }
    return need == 0;
}


// ---------------------------------------------------------------------------
// 22. Maximum XOR of Two Numbers
//     PROBLEM: Given an array, return the maximum XOR obtainable from any pair.
//     MENTAL MODEL: "binary trie greedily tries the opposite bit"
// ---------------------------------------------------------------------------
struct TrieNode {
    TrieNode* child[2];
    TrieNode() : child{nullptr, nullptr} {}
};

void insertTrie(TrieNode* root, int x) {
    TrieNode* cur = root;
    for (int bit = 31; bit >= 0; bit--) {
        int b = (x >> bit) & 1;
        if (!cur->child[b]) cur->child[b] = new TrieNode();
        cur = cur->child[b];
    }
}

int bestXorWith(TrieNode* root, int x) {
    TrieNode* cur = root;
    int ans = 0;

    for (int bit = 31; bit >= 0; bit--) {
        int b = (x >> bit) & 1;
        int want = 1 - b;
        if (cur->child[want]) {
            ans |= (1 << bit);
            cur = cur->child[want];
        } else {
            cur = cur->child[b];
        }
    }
    return ans;
}

int findMaximumXOR(vector<int>& nums) {
    TrieNode* root = new TrieNode();
    for (int x : nums) insertTrie(root, x);

    int best = 0;
    for (int x : nums) best = max(best, bestXorWith(root, x));
    return best;
}


int main() {
    cout << "=== Basic Bits ===" << endl;
    int n = 10; // binary 1010
    cout << "Bit 1 set: " << boolalpha << isBitSet(n, 1) << endl;
    cout << "Set bit 0: " << setBit(n, 0) << endl;
    cout << "Clear bit 1: " << clearBit(n, 1) << endl;
    cout << "Toggle bit 3: " << toggleBit(n, 3) << endl;
    cout << "16 power of two: " << isPowerOfTwo(16) << endl;
    cout << "Set bits in 29: " << countSetBits(29) << endl;
    printVec(countBits(5));

    cout << "\n=== XOR Tricks ===" << endl;
    vector<int> one{4,1,2,1,2};
    cout << "Single: " << singleNumber(one) << endl;
    vector<int> threeTimes{2,2,3,2};
    cout << "Single II: " << singleNumberII(threeTimes) << endl;
    vector<int> twoSingles{1,2,1,3,2,5};
    printVec(singleNumberIII(twoSingles));
    vector<int> miss{3,0,1};
    cout << "Missing xor: " << missingNumberXor(miss) << endl;
    vector<int> dupMiss{1,2,2,4};
    printVec(findDuplicateAndMissing(dupMiss));

    cout << "\n=== Distances / Ranges ===" << endl;
    cout << "Reverse bits of 43261596: " << reverseBits(43261596) << endl;
    cout << "Hamming distance 1,4: " << hammingDistance(1, 4) << endl;
    vector<int> hd{4,14,2};
    cout << "Total hamming: " << totalHammingDistance(hd) << endl;
    cout << "Range AND 5..7: " << rangeBitwiseAnd(5, 7) << endl;

    cout << "\n=== Masks ===" << endl;
    vector<int> subsetInput{1,2,3};
    for (auto& s : subsets(subsetInput)) printVec(s);
    printVec(allSubmasks(0b1011));
    vector<string> words{"abcw","baz","foo","bar","xtfn","abcdef"};
    cout << "Max product words: " << maxProductWords(words) << endl;
    for (string& s : findRepeatedDnaSequences("AAAAACCCCCAAAAACCCCCCAAAAAGGGTTT")) {
        cout << s << " ";
    }
    cout << endl;

    cout << "\n=== Arithmetic / Codes ===" << endl;
    cout << "Sum 5 + -2: " << getSum(5, -2) << endl;
    cout << "Divide 43 / -8: " << divideIntegers(43, -8) << endl;
    printVec(grayCode(3));
    vector<int> utf{197,130,1};
    cout << "Valid UTF-8: " << validUtf8(utf) << endl;

    cout << "\n=== XOR Trie ===" << endl;
    vector<int> xorInput{3,10,5,25,2,8};
    cout << "Max XOR: " << findMaximumXOR(xorInput) << endl;

    return 0;
}
