#include <bits/stdc++.h>
using namespace std;

// ─────────────────────────────────────────────────────────────────────────
// String Problems Reference — C++
// ─────────────────────────────────────────────────────────────────────────
//
// ═════════════════════════════════════════════════════════════════════════
// MEMORY SYSTEM — METHOD OF LOCI  (Memory Palace: Your House)
// ═════════════════════════════════════════════════════════════════════════
//
//  Walk through your house in this fixed order.
//  At each station, a vivid scene encodes the algorithm's core trick.
//  The stranger/funnier the image, the better it sticks.
//
//   Station 1  FRONT DOOR        → REVERSE
//   Station 2  WELCOME MAT       → PALINDROME
//   Station 3  HALLWAY MIRROR    → LONGEST PALINDROMIC SUBSTRING
//   Station 4  COAT RACK         → ANAGRAM
//   Station 5  STAIRCASE         → STRING COMPRESSION
//   Station 6  LIGHT SWITCH      → FIRST NON-REPEATING CHAR
//   Station 7  LIVING ROOM SOFA  → LONGEST COMMON PREFIX
//   Station 8  TV REMOTE         → ATOI
//   Station 9  KITCHEN DOOR      → VALID PARENTHESES
//   Station 10 KITCHEN STOVE     → KMP
//   Station 11 REFRIGERATOR      → RABIN-KARP
//   Station 12 KITCHEN WINDOW    → Z-ALGORITHM
//   Station 13 KITCHEN SINK      → MINIMUM WINDOW SUBSTRING
//   Station 14 CUTTING BOARD     → LONGEST NO-REPEAT
//   Station 15 MICROWAVE         → DECODE WAYS
//   Station 16 OVEN              → WILDCARD MATCHING
//   Station 17 DINING TABLE      → ROMAN NUMERALS
//   Station 18 BOOKSHELF         → TRIE
//   Station 19 BACK DOOR / SAFE  → STRING HASHING
//
//  VIVID SCENES (the actual memory hooks):
//   1  You push the FRONT DOOR — it spins 180° and hits you back.
//      Whole door reverses, then each panel reverses back. (reverse-whole, reverse-parts)
//   2  The WELCOME MAT reads "MADAM" — same forwards and backwards.
//      Two ants start from each end and crawl inward; they must never disagree.
//   3  The HALLWAY MIRROR: you stand at each floor tile and stretch your arms.
//      The tile with the widest wingspan = center of the longest palindrome.
//   4  COAT RACK: someone jumbled all coats. You sort them alphabetically.
//      Two coats that sort to the same key = anagrams.
//   5  STAIRCASE: a giant HAND squeezes all stairs together, counting each run.
//      "aabbb" → A squeezed to a2, B squeezed to b3.
//   6  LIGHT SWITCH flipped twice = useless. Flipped once = the one you want.
//      Two-pass: count flips, then find the first switch flipped exactly once.
//   7  LIVING ROOM SOFA: whole family sits; you propose a shared Wi-Fi password.
//      Keep trimming the end of the password until everyone agrees (pop_back loop).
//   8  TV REMOTE has exactly 4 buttons: SKIP (spaces) → SIGN (+/-) → DIGITS → STOP.
//      Press them in order. Use a long hand to catch overflow before clamping.
//   9  KITCHEN DOOR: every door that OPENS must CLOSE (push→pop).
//      Unmatched ')' = slamming shut with nothing open. Leftover stack = door left open.
//  10  KITCHEN STOVE: chef follows a recipe. Burns a step? LPS tells exactly
//      how far back the recipe restarts — never re-chop what's already done.
//  11  REFRIGERATOR has a fingerprint scanner on every shelf.
//      Slide a tray the same size as the pattern; compare fingerprints. Verify on match.
//  12  KITCHEN WINDOW frame is shaped like a Z.
//      Z[i] = "how much of the view through this Z-pane matches the street's start?"
//  13  KITCHEN SINK: turn tap RIGHT to fill (expand). When full, turn tap LEFT to drain
//      to the minimum needed (shrink). Record the level before draining.
//  14  CUTTING BOARD: you slide a knife across vegetables. Hit a DUPLICATE carrot?
//      Jump the board past the last carrot — don't inch forward one cell at a time.
//  15  MICROWAVE: two buttons — 1 min and 2 min. How many ways to heat for n minutes?
//      dp[i] = ways using 1-min button (dp[i-1]) + ways using 2-min button (dp[i-2]).
//  16  OVEN has a JOKER dial marked *. * means "any temperature" — it can match
//      no degrees (door still closed: dp[i][j-1]) or eat one more degree (dp[i-1][j]).
//  17  DINING TABLE: GRANDPA counts Roman coins. He SUBTRACTS a coin whenever a
//      smaller coin is placed in front of a larger one. Greedy largest-first to convert back.
//  18  BOOKSHELF: organized A→Z, then A→Z inside each letter's section.
//      It's literally a Trie — walk one letter at a time; isEnd marks a full word.
//  19  BACK DOOR SAFE: the combination is a large number built from the house's
//      street address prefix sums. Range query = (prefix[r+1] - prefix[l]) * inv(B^l).
//
// ═════════════════════════════════════════════════════════════════════════
// LINKING CHAIN  (each item's image triggers the next — read as a story)
// ═════════════════════════════════════════════════════════════════════════
//
//  You FLIP the FRONT DOOR (1-Reverse) and see your face in a MIRROR that
//  reads the same backwards — a PALINDROME mat (2). The mat BLOOMS a flower
//  from its CENTER — the LONGEST PALINDROME (3). The petals fall off and
//  SCRAMBLE into a pile of ANAGRAM letters (4). A giant hand SQUEEZES those
//  letters into a COMPRESSED zip (5). Inside the zip is ONE UNIQUE FILE
//  that appears only once — FIRST NON-REPEATING (6). All folders on the
//  drive share that file's name as their COMMON PREFIX (7). The prefix is
//  exactly 4 characters — matching the 4 STAGES of ATOI (8). The 4-digit
//  code unlocks a door whose bolts form BALANCED PARENTHESES (9). Inside
//  the kitchen a chef NEVER RE-READS previous steps — KMP forward-only (10).
//  Each dish has a FINGERPRINT sticker — RABIN-KARP hash (11). The sticker
//  is Z-SHAPED — Z-ALGORITHM (12). A Z-shaped tap fills the SINK — expand
//  right, shrink left — MINIMUM WINDOW (13). Draining the sink reveals a
//  CUTTING BOARD where each vegetable is UNIQUE — NO REPEAT (14). The recipe
//  on the board can be read in 1-step or 2-step chunks — DECODE WAYS (15).
//  The recipe is sealed with a JOKER stamp (*) — WILDCARD (16). The joker
//  has ROMAN NUMERALS on the back (17). The Roman soldier plants a TRIE of
//  swords forming a letter-tree (18). The hilt of each sword is engraved
//  with a HASH number — STRING HASHING (19).
//
// ═════════════════════════════════════════════════════════════════════════


// ─────────────────────────────────────────────────────────────────────────
// 1. Reverse
//    LOCI : FRONT DOOR — you push it, it spins 180° and smacks you back
//    LINK : spinning door → you see your reflection reading the same backwards (palindrome next)
//    MENTAL MODEL: "reverse whole, then reverse parts"
//    Reverse words in-place: flip entire string → each word is reversed → flip each word back.
//    Remember: two-step undo — global reverse makes words right-order but backwards internally.
// ─────────────────────────────────────────────────────────────────────────
string reverseStr(string s) {
    reverse(s.begin(), s.end());
    return s;
}

// Reverse words — "  hello   world  " → "world hello"
string reverseWords(const string& s) {
    istringstream iss(s);
    deque<string> words;
    string w;
    while (iss >> w) words.push_front(w);   // each word goes to front = reversed order
    string res;
    for (int i = 0; i < (int)words.size(); i++) {
        if (i) res += ' ';
        res += words[i];
    }
    return res;
}

// Reverse only the words in-place (O(1) extra)
string reverseWordsInPlace(string s) {
    reverse(s.begin(), s.end());
    int n = s.size(), start = 0;
    for (int i = 0; i <= n; i++) {
        if (i == n || s[i] == ' ') {
            reverse(s.begin() + start, s.begin() + i);
            start = i + 1;
        }
    }
    return s;
}


// ─────────────────────────────────────────────────────────────────────────
// 2. Palindrome
//    LOCI : WELCOME MAT — reads "MADAM" from both ends; two ants crawl inward and must agree
//    LINK : ants meet at the center seed → that seed grows outward (longest palindrome next)
//    MENTAL MODEL: "two pointers walking toward each other — they must never disagree"
//    Skip noise (non-alnum) from both ends before comparing.
//    One-delete variant: on first mismatch, try skipping left OR right — whichever makes the rest valid.
// ─────────────────────────────────────────────────────────────────────────
bool isPalindrome(const string& s) {
    int l = 0, r = (int)s.size() - 1;
    while (l < r) if (s[l++] != s[r--]) return false;
    return true;
}

// Valid palindrome — ignore non-alphanumeric, case-insensitive
bool isValidPalindrome(const string& s) {
    int l = 0, r = (int)s.size() - 1;
    while (l < r) {
        while (l < r && !isalnum(s[l])) l++;
        while (l < r && !isalnum(s[r])) r--;
        if (tolower(s[l++]) != tolower(s[r--])) return false;
    }
    return true;
}

// Can be palindrome with at most one deletion?
bool validPalindromeOneDelete(const string& s) {
    auto check = [&](int l, int r) {
        while (l < r) if (s[l++] != s[r--]) return false;
        return true;
    };
    int l = 0, r = (int)s.size() - 1;
    while (l < r) {
        if (s[l] != s[r]) return check(l+1, r) || check(l, r-1);
        l++; r--;
    }
    return true;
}


// ─────────────────────────────────────────────────────────────────────────
// 3. Longest Palindromic Substring — O(n²) expand-around-center
//    LOCI : HALLWAY MIRROR — stand at every floor tile, stretch arms wide; widest wingspan wins
//    LINK : widest wingspan has scrambled feathers falling off → anagram pile on coat rack next
//    MENTAL MODEL: "every palindrome has a center — plant a seed at every position and grow it"
//    Two center types: single char (odd-length: "aba") and gap (even-length: "abba").
//    Always call expand(i,i) AND expand(i,i+1) — never forget even-length case.
//    After expand, palindrome boundary is [l+1, r-1] (l,r overshot by 1 each).
// ─────────────────────────────────────────────────────────────────────────
string longestPalindrome(const string& s) {
    int n = s.size(), start = 0, maxLen = 1;
    auto expand = [&](int l, int r) {
        while (l >= 0 && r < n && s[l] == s[r]) { l--; r++; }
        if (r - l - 1 > maxLen) { maxLen = r - l - 1; start = l + 1; }
    };
    for (int i = 0; i < n; i++) {
        expand(i, i);       // odd-length center
        expand(i, i + 1);   // even-length center
    }
    return s.substr(start, maxLen);
}

// Count palindromic substrings — O(n²)
int countPalindromes(const string& s) {
    int n = s.size(), count = 0;
    auto expand = [&](int l, int r) {
        while (l >= 0 && r < n && s[l] == s[r]) { l--; r++; count++; }
    };
    for (int i = 0; i < n; i++) { expand(i, i); expand(i, i + 1); }
    return count;
}


// ─────────────────────────────────────────────────────────────────────────
// 4. Anagram
//    LOCI : COAT RACK — someone scrambled all coats; sort alphabetically to find matching pairs
//    LINK : sorted coats get squashed together by a giant hand (compression next)
//    MENTAL MODEL: "anagrams are the same multiset of characters — normalize to a canonical form"
//    Canonical form = sorted string (for grouping) OR freq[26] array (for comparison).
//    Permutation-in-string = fixed-size sliding window comparing freq arrays.
//    Key: use int[26] not map — faster and avoids hash overhead for lowercase-only input.
// ─────────────────────────────────────────────────────────────────────────
bool isAnagram(const string& s, const string& t) {
    if (s.size() != t.size()) return false;
    int freq[26] = {};
    for (char c : s) freq[c - 'a']++;
    for (char c : t) if (--freq[c - 'a'] < 0) return false;
    return true;
}

// Group anagrams — O(n * k log k)
vector<vector<string>> groupAnagrams(vector<string>& strs) {
    unordered_map<string, vector<string>> groups;
    for (auto& s : strs) {
        string key = s;
        sort(key.begin(), key.end());
        groups[key].push_back(s);
    }
    vector<vector<string>> res;
    for (auto& p : groups) res.push_back(p.second);
    return res;
}

// Permutation in string — does s2 contain any permutation of s1? — O(n)
bool permutationInString(const string& s1, const string& s2) {
    if (s1.size() > s2.size()) return false;
    int need[26] = {}, window[26] = {};
    for (char c : s1) need[c - 'a']++;
    int k = s1.size();
    for (int i = 0; i < (int)s2.size(); i++) {
        window[s2[i] - 'a']++;
        if (i >= k) window[s2[i - k] - 'a']--;
        if (equal(need, need + 26, window)) return true;
    }
    return false;
}


// ─────────────────────────────────────────────────────────────────────────
// 5. String Compression (Run-Length Encoding)
//    LOCI : STAIRCASE — a giant hand squeezes all stairs into runs: "aabbb" → a2b3
//    LINK : inside the zip file there is ONE unique file that never repeats (first non-repeat next)
//    MENTAL MODEL: "greedy run scanner — grab a char, count how many, write char+count, repeat"
//    Always check if compressed result is actually shorter before returning it.
//    "aabbbcccc" → "a2b3c4"   (return original if compressed is longer)
// ─────────────────────────────────────────────────────────────────────────
string compress(const string& s) {
    string res;
    int n = s.size(), i = 0;
    while (i < n) {
        char c = s[i];
        int count = 0;
        while (i < n && s[i] == c) { i++; count++; }
        res += c;
        if (count > 1) res += to_string(count);
    }
    return res.size() < s.size() ? res : s;
}


// ─────────────────────────────────────────────────────────────────────────
// 6. First Non-Repeating Character — O(n)
//    LOCI : LIGHT SWITCH — flipped twice = useless; flipped exactly once = the one you want
//    LINK : that unique switch controls the Wi-Fi — everyone must agree on the password (LCP next)
//    MENTAL MODEL: "two-pass — count everything, then walk left-to-right for first count=1"
//    Pass 1: build frequency map.  Pass 2: return first index with freq == 1.
//    Never try to do it in one pass — you can't know if a char repeats until you've seen the whole string.
// ─────────────────────────────────────────────────────────────────────────
int firstUnique(const string& s) {
    int freq[26] = {};
    for (char c : s) freq[c - 'a']++;
    for (int i = 0; i < (int)s.size(); i++)
        if (freq[s[i] - 'a'] == 1) return i;
    return -1;
}


// ─────────────────────────────────────────────────────────────────────────
// 7. Longest Common Prefix
//    LOCI : LIVING ROOM SOFA — family proposes a shared Wi-Fi password, trim end until ALL agree
//    LINK : the agreed password is exactly 4 characters (atoi's 4 stages next)
//    MENTAL MODEL: "shrink the candidate until every word agrees with it"
//    Start with words[0] as the full candidate prefix.
//    For each next word: keep trimming one char from the end until word starts with prefix.
//    Stops naturally at "" if no common prefix exists.
// ─────────────────────────────────────────────────────────────────────────
string longestCommonPrefix(vector<string>& words) {
    if (words.empty()) return "";
    string prefix = words[0];
    for (int i = 1; i < (int)words.size(); i++)
        while (words[i].find(prefix) != 0) prefix.pop_back();
    return prefix;
}


// ─────────────────────────────────────────────────────────────────────────
// 8. String to Integer (atoi) — handle whitespace, sign, overflow, junk
//    LOCI : TV REMOTE — exactly 4 buttons: SKIP(spaces) SIGN(+/-) DIGITS STOP
//    LINK : 4-digit PIN from the remote unlocks the kitchen door lock (parentheses next)
//    MENTAL MODEL: "4 ordered stages — skip spaces → read sign → read digits → stop at non-digit"
//    Edge cases checklist: leading spaces, optional +/-, leading zeros, non-digit mid-string,
//    INT_MAX/INT_MIN overflow (use long during accumulation, clamp before return).
// ─────────────────────────────────────────────────────────────────────────
int myAtoi(const string& s) {
    int i = 0, n = s.size();
    while (i < n && s[i] == ' ') i++;
    int sign = 1;
    if (i < n && (s[i] == '+' || s[i] == '-')) sign = (s[i++] == '+') ? 1 : -1;
    long result = 0;
    while (i < n && isdigit(s[i])) {
        result = result * 10 + (s[i++] - '0');
        if (result * sign >  INT_MAX) return  INT_MAX;
        if (result * sign <  INT_MIN) return  INT_MIN;
    }
    return sign * (int)result;
}


// ─────────────────────────────────────────────────────────────────────────
// 9. Valid Parentheses
//    LOCI : KITCHEN DOOR — every door that OPENS must CLOSE; unmatched slam = invalid house
//    LINK : inside the kitchen the chef never re-reads steps (KMP next)
//    MENTAL MODEL: "stack = your expectation list — push what you need to close, pop when you close it"
//    Open bracket → push onto stack.  Close bracket → must match top, else invalid.
//    At end: stack must be empty (every open was closed).
//    Min-remove variant: unmatched ')' = immediately bad index; unmatched '(' = leftover on stack.
// ─────────────────────────────────────────────────────────────────────────
bool isValidParens(const string& s) {
    stack<char> st;
    for (char c : s) {
        if (c == '(' || c == '[' || c == '{') { st.push(c); continue; }
        if (st.empty()) return false;
        char top = st.top(); st.pop();
        if ((c==')' && top!='(') || (c==']' && top!='[') || (c=='}' && top!='{'))
            return false;
    }
    return st.empty();
}

// Minimum removals to make valid
string minRemoveToValid(string s) {
    stack<int> opens;
    set<int> toRemove;
    for (int i = 0; i < (int)s.size(); i++) {
        if      (s[i] == '(') opens.push(i);
        else if (s[i] == ')') {
            if (opens.empty()) toRemove.insert(i);
            else opens.pop();
        }
    }
    while (!opens.empty()) { toRemove.insert(opens.top()); opens.pop(); }
    string res;
    for (int i = 0; i < (int)s.size(); i++)
        if (!toRemove.count(i)) res += s[i];
    return res;
}


// ─────────────────────────────────────────────────────────────────────────
// 10. KMP — O(n + m)
//     LOCI : KITCHEN STOVE — chef never re-chops; burns a step → recipe (LPS) says exactly where to restart
//     LINK : recipe has a fingerprint sticker on every dish (Rabin-Karp next)
//     MENTAL MODEL: "never re-examine text — on mismatch, jump pattern backward using LPS"
//     LPS[i] = length of longest proper prefix of pattern[0..i] that is also a suffix.
//     LPS tells you: "this much of the pattern is already matched even after a mismatch."
//     Build LPS once on pattern (O(m)), then scan text once (O(n)) — never backtrack i.
//     Mnemonic: lps = "longest prefix suffix" = how far to rewind j (not i) on failure.
// ─────────────────────────────────────────────────────────────────────────
vector<int> buildLPS(const string& p) {
    int m = p.size();
    vector<int> lps(m, 0);
    for (int i = 1, len = 0; i < m; ) {
        if (p[i] == p[len]) { lps[i++] = ++len; }
        else if (len)        { len = lps[len - 1]; }
        else                 { lps[i++] = 0; }
    }
    return lps;
}

vector<int> kmpSearch(const string& text, const string& pattern) {
    vector<int> matches, lps = buildLPS(pattern);
    int n = text.size(), m = pattern.size(), i = 0, j = 0;
    while (i < n) {
        if (text[i] == pattern[j]) { i++; j++; }
        if (j == m)              { matches.push_back(i - j); j = lps[j - 1]; }
        else if (i<n && text[i]!=pattern[j]) {
            if (j) j = lps[j - 1]; else i++;
        }
    }
    return matches;
}


// ─────────────────────────────────────────────────────────────────────────
// 11. Rabin-Karp Rolling Hash — O(n + m) average
//     LOCI : REFRIGERATOR — fingerprint scanner on every shelf; slide a same-size tray and compare
//     LINK : the fingerprint sticker is shaped like a Z (Z-algorithm next)
//     MENTAL MODEL: "fingerprint the pattern, then slide a same-size fingerprint across text"
//     Rolling update: hash = (hash - outgoing*BASE^(m-1)) * BASE + incoming  (mod prime)
//     Always verify on hash match — two different strings can hash to the same value.
//     Best used when searching for MULTIPLE patterns simultaneously (one pass, many hashes).
//     vs KMP: KMP is better for single pattern; R-K better for multi-pattern or 2D matching.
// ─────────────────────────────────────────────────────────────────────────
vector<int> rabinKarp(const string& text, const string& pattern) {
    const long MOD = 1e9 + 7, BASE = 31;
    int n = text.size(), m = pattern.size();
    vector<int> matches;
    if (m > n) return matches;

    long patHash = 0, winHash = 0, basePow = 1;
    for (int i = 0; i < m - 1; i++) basePow = basePow * BASE % MOD;   // BASE^(m-1)
    for (int i = 0; i < m; i++) {
        patHash = (patHash * BASE + (pattern[i] - 'a' + 1)) % MOD;
        winHash = (winHash * BASE + (text[i]    - 'a' + 1)) % MOD;
    }
    for (int i = 0; i <= n - m; i++) {
        if (winHash == patHash && text.substr(i, m) == pattern)   // verify to avoid collision
            matches.push_back(i);
        if (i < n - m) {
            winHash = (winHash - (text[i] - 'a' + 1) * basePow % MOD + MOD) % MOD;
            winHash = (winHash * BASE + (text[i + m] - 'a' + 1)) % MOD;
        }
    }
    return matches;
}


// ─────────────────────────────────────────────────────────────────────────
// 12. Z-Algorithm — O(n + m)
//     LOCI : KITCHEN WINDOW — frame is Z-shaped; view through it matches how much of the street start you see
//     LINK : Z-shaped tap above the sink fills/drains the water (min window next)
//     MENTAL MODEL: "Z[i] answers: how much of s[i..] looks like the very start of s?"
//     Search trick: build combined = pattern + "$" + text.
//       Any position where Z[i] == m is a match (the "$" separator prevents Z from crossing boundary).
//     Maintain a Z-box [l,r] = rightmost matching window seen so far.
//       If i is inside the box, reuse Z[i-l] as a free head start, then extend manually.
//     vs KMP: same complexity, Z is often easier to remember and implement correctly.
// ─────────────────────────────────────────────────────────────────────────
vector<int> zArray(const string& s) {
    int n = s.size();
    vector<int> z(n, 0);
    z[0] = n;
    for (int i = 1, l = 0, r = 0; i < n; i++) {
        if (i < r) z[i] = min(r - i, z[i - l]);
        while (i + z[i] < n && s[z[i]] == s[i + z[i]]) z[i]++;
        if (i + z[i] > r) { l = i; r = i + z[i]; }
    }
    return z;
}

vector<int> zSearch(const string& text, const string& pattern) {
    string combined = pattern + "$" + text;
    vector<int> z = zArray(combined);
    int m = pattern.size();
    vector<int> matches;
    for (int i = m + 1; i < (int)combined.size(); i++)
        if (z[i] == m) matches.push_back(i - m - 1);
    return matches;
}


// ─────────────────────────────────────────────────────────────────────────
// 13. Minimum Window Substring — O(n)
//     LOCI : KITCHEN SINK — turn tap right to fill (expand); once full, turn left to drain to minimum (shrink)
//     LINK : draining reveals a cutting board with veggies (longest no-repeat next)
//     MENTAL MODEL: "expand right to satisfy, shrink left to minimize — record best before shrinking"
//     Track `have` (distinct chars satisfied) vs `required` (distinct chars needed).
//     A char is "satisfied" when window[c] == need[c] — not just present, but enough copies.
//     Template for ALL variable-window problems:
//       right loop → add s[right] → check if window valid → inner while: record/shrink left.
// ─────────────────────────────────────────────────────────────────────────
string minWindow(const string& s, const string& t) {
    unordered_map<char,int> need;
    for (char c : t) need[c]++;
    int have = 0, required = need.size(), left = 0, minLen = INT_MAX, minL = 0;
    unordered_map<char,int> window;
    for (int right = 0; right < (int)s.size(); right++) {
        window[s[right]]++;
        if (need.count(s[right]) && window[s[right]] == need[s[right]]) have++;
        while (have == required) {
            if (right - left + 1 < minLen) { minLen = right - left + 1; minL = left; }
            window[s[left]]--;
            if (need.count(s[left]) && window[s[left]] < need[s[left]]) have--;
            left++;
        }
    }
    return minLen == INT_MAX ? "" : s.substr(minL, minLen);
}


// ─────────────────────────────────────────────────────────────────────────
// 14. Longest Substring Without Repeating Characters — O(n)
//     LOCI : CUTTING BOARD — sliding knife hits a duplicate carrot; JUMP the board past where you last saw it
//     LINK : recipe on board can be read in 1 or 2 chunks (decode ways next)
//     MENTAL MODEL: "last[c] = where I last saw c — if it's inside my window, jump left past it"
//     Don't shrink left by 1 each time — jump directly: left = last[c] + 1.
//     Condition: last[c] >= left (not just last.count(c)) — char may be outside current window.
//     Window size at any point = right - left + 1.
// ─────────────────────────────────────────────────────────────────────────
int longestNoRepeat(const string& s) {
    unordered_map<char,int> last;
    int maxLen = 0, left = 0;
    for (int right = 0; right < (int)s.size(); right++) {
        if (last.count(s[right]) && last[s[right]] >= left)
            left = last[s[right]] + 1;
        last[s[right]] = right;
        maxLen = max(maxLen, right - left + 1);
    }
    return maxLen;
}


// ─────────────────────────────────────────────────────────────────────────
// 15. Decode Ways — "12" → 2 ("AB" or "L") — O(n) DP
//     LOCI : MICROWAVE — two buttons 1-min and 2-min; dp[i] = ways pressing 1-btn (dp[i-1]) + 2-btn (dp[i-2])
//     LINK : recipe decoded in 1 or 2 chunks → sealed with a JOKER stamp (*) (wildcard next)
//     MENTAL MODEL: "at each position, try single-digit decode AND two-digit decode independently"
//     dp[i] = number of ways to decode s[0..i-1].
//     Single digit valid: s[i-1] != '0'  → dp[i] += dp[i-1]
//     Two digit valid:    10 <= s[i-2..i-1] <= 26  → dp[i] += dp[i-2]
//     Watch out: "06" is NOT a valid two-digit decode (leading zero).
// ─────────────────────────────────────────────────────────────────────────
int numDecodings(const string& s) {
    int n = s.size();
    vector<int> dp(n + 1, 0);
    dp[0] = 1;
    dp[1] = s[0] != '0' ? 1 : 0;
    for (int i = 2; i <= n; i++) {
        if (s[i-1] != '0') dp[i] += dp[i-1];
        int two = stoi(s.substr(i-2, 2));
        if (two >= 10 && two <= 26) dp[i] += dp[i-2];
    }
    return dp[n];
}


// ─────────────────────────────────────────────────────────────────────────
// 16. Wildcard Matching — '?' = any single char, '*' = any sequence — O(m*n)
//     LOCI : OVEN — JOKER dial marked *; matches 0 degrees (dp[i][j-1]) or eats one more degree (dp[i-1][j])
//     LINK : joker stamp has ROMAN NUMERALS engraved on its back (roman next)
//     MENTAL MODEL: "* is a shape-shifter — it can match nothing (dp[i][j-1]) or eat one more char (dp[i-1][j])"
//     dp[i][j] = can s[0..i-1] be matched by p[0..j-1]?
//     '?': matches exactly one char → same as exact match: dp[i-1][j-1]
//     '*': matches 0 chars → dp[i][j-1]  OR  matches 1+ chars → dp[i-1][j]
//     Base case: dp[0][j] = true only if p[0..j-1] is all '*'.
// ─────────────────────────────────────────────────────────────────────────
bool wildcardMatch(const string& s, const string& p) {
    int m = s.size(), n = p.size();
    vector<vector<bool>> dp(m+1, vector<bool>(n+1, false));
    dp[0][0] = true;
    for (int j = 1; j <= n; j++) dp[0][j] = p[j-1] == '*' && dp[0][j-1];
    for (int i = 1; i <= m; i++)
        for (int j = 1; j <= n; j++)
            dp[i][j] = (p[j-1] == '*') ? dp[i-1][j] || dp[i][j-1]
                                        : (p[j-1]=='?' || s[i-1]==p[j-1]) && dp[i-1][j-1];
    return dp[m][n];
}


// ─────────────────────────────────────────────────────────────────────────
// 17. Roman ↔ Integer
//     LOCI : DINING TABLE — GRANDPA subtracts a coin when a smaller one precedes a larger; greedy largest-first back
//     LINK : grandpa plants a letter-tree of swords to guard the table (trie next)
//     MENTAL MODEL (toInt): "add normally, but subtract if current symbol < the one after it"
//     Rule: IV=4, IX=9, XL=40 — whenever a smaller value precedes a larger, it's a subtraction.
//     MENTAL MODEL (toRoman): "greedy — pick the largest denomination that fits, repeat"
//     Always list values in descending order including the subtractive combos (CM, CD, XC...).
// ─────────────────────────────────────────────────────────────────────────
int romanToInt(const string& s) {
    unordered_map<char,int> v{{'I',1},{'V',5},{'X',10},{'L',50},
                               {'C',100},{'D',500},{'M',1000}};
    int res = 0;
    for (int i = 0; i < (int)s.size(); i++)
        res += (i+1 < (int)s.size() && v[s[i]] < v[s[i+1]]) ? -v[s[i]] : v[s[i]];
    return res;
}

string intToRoman(int num) {
    vector<pair<int,string>> vals{
        {1000,"M"},{900,"CM"},{500,"D"},{400,"CD"},{100,"C"},{90,"XC"},
        {50,"L"},{40,"XL"},{10,"X"},{9,"IX"},{5,"V"},{4,"IV"},{1,"I"}
    };
    string res;
    for (auto& p : vals) while (num >= p.first) { res += p.second; num -= p.first; }
    return res;
}


// ─────────────────────────────────────────────────────────────────────────
// 18. Trie — prefix tree for fast prefix lookups and autocomplete
//     LOCI : BOOKSHELF — organized A→Z, then A→Z inside each section; walk one letter at a time, isEnd = full word
//     LINK : each sword's hilt is engraved with a HASH number (string hashing next)
//     MENTAL MODEL: "each node is a character slot; walk down one char at a time, create if missing"
//     insert/search/startsWith all O(L) where L = word length
//     children[26]: branch on each lowercase letter.  isEnd: marks a complete word.
//     search vs startsWith: search needs isEnd==true at final node; startsWith just needs to reach it.
//     Use when: autocomplete, dictionary lookup, word prefix problems, IP routing.
// ─────────────────────────────────────────────────────────────────────────
struct TrieNode {
    TrieNode* children[26] = {};
    bool isEnd = false;
};

struct Trie {
    TrieNode* root = new TrieNode();

    void insert(const string& word) {
        TrieNode* node = root;
        for (char c : word) {
            int idx = c - 'a';
            if (!node->children[idx]) node->children[idx] = new TrieNode();
            node = node->children[idx];
        }
        node->isEnd = true;
    }

    bool search(const string& word) {
        TrieNode* node = root;
        for (char c : word) {
            int idx = c - 'a';
            if (!node->children[idx]) return false;
            node = node->children[idx];
        }
        return node->isEnd;
    }

    bool startsWith(const string& prefix) {
        TrieNode* node = root;
        for (char c : prefix) {
            int idx = c - 'a';
            if (!node->children[idx]) return false;
            node = node->children[idx];
        }
        return true;
    }
};


// ─────────────────────────────────────────────────────────────────────────
// 19. String hashing — useful quick reference
//     LOCI : BACK DOOR SAFE — combination is a prefix-sum address; range hash = (prefix[r+1]-prefix[l]) * inv(B^l)
//     LINK : (end of palace — you've cracked the safe and stepped out the back door!)
//     MENTAL MODEL: "treat the string as a number in base B — prefix sums give O(1) substring hash"
//     Polynomial: h = s[0]*B^0 + s[1]*B^1 + ...  stored as prefix array.
//     Range hash of s[l..r] = (prefix[r+1] - prefix[l]) * modInverse(B^l)  (mod prime)
//     Use when: O(1) substring comparison, finding duplicate substrings, Rabin-Karp extension.
//     Choose B=31 (lowercase) or B=131 (all ASCII), MOD=1e9+7 (large prime).
//     Double hashing (two different MODs) almost eliminates collision probability.
// ─────────────────────────────────────────────────────────────────────────
struct StringHash {
    const long MOD = 1e9 + 7, BASE = 31;
    vector<long> h, pw;

    StringHash(const string& s) : h(s.size()+1, 0), pw(s.size()+1, 1) {
        for (int i = 0; i < (int)s.size(); i++) {
            h[i+1] = (h[i] + (s[i]-'a'+1) * pw[i]) % MOD;
            pw[i+1] = pw[i] * BASE % MOD;
        }
    }

    // hash of s[l..r] (inclusive), normalised so it starts at power 0
    long get(int l, int r) {
        return (h[r+1] - h[l] + MOD) % MOD * /* modInverse(pw[l]) — omitted for brevity */ 1 % MOD;
    }
};


// ─────────────────────────────────────────────────────────────────────────
// Demo
// ─────────────────────────────────────────────────────────────────────────
int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    cout << "=== Reverse ===\n";
    cout << reverseStr("hello") << "\n";                       // olleh
    cout << reverseWords("  hello   world  ") << "\n";         // world hello

    cout << "\n=== Palindrome ===\n";
    cout << boolalpha;
    cout << isPalindrome("racecar") << "\n";                    // true
    cout << isValidPalindrome("A man, a plan, a canal: Panama") << "\n"; // true
    cout << longestPalindrome("babad") << "\n";                 // bab or aba
    cout << countPalindromes("abc") << "\n";                    // 3
    cout << validPalindromeOneDelete("abca") << "\n";           // true

    cout << "\n=== Anagram ===\n";
    cout << isAnagram("anagram","nagaram") << "\n";             // true
    cout << permutationInString("ab","eidbaooo") << "\n";       // true
    vector<string> strs = {"eat","tea","tan","ate","nat","bat"};
    cout << groupAnagrams(strs).size() << " groups\n";          // 3

    cout << "\n=== Compression / Encoding ===\n";
    cout << compress("aabbbcccc") << "\n";                      // a2b3c4
    cout << numDecodings("226") << "\n";                        // 3

    cout << "\n=== Misc ===\n";
    cout << firstUnique("leetcode") << "\n";                    // 0
    vector<string> words = {"flower","flow","flight"};
    cout << longestCommonPrefix(words) << "\n";                 // fl
    cout << myAtoi("  -42abc") << "\n";                         // -42
    cout << isValidParens("()[]{}") << "\n";                    // true
    cout << isValidParens("([)]") << "\n";                      // false
    cout << minRemoveToValid("lee(t(c)o)de)") << "\n";          // lee(t(c)o)de

    cout << "\n=== Pattern Matching ===\n";
    auto kmpR = kmpSearch("aababcabcabc","abc");
    cout << "KMP: ";
    for (int i : kmpR) cout << i << " ";    cout << "\n";       // 5 8

    auto rkR = rabinKarp("aababcabcabc","abc");
    cout << "R-K: ";
    for (int i : rkR) cout << i << " ";    cout << "\n";        // 5 8

    auto zR = zSearch("aababcabcabc","abc");
    cout << "Z:   ";
    for (int i : zR) cout << i << " ";     cout << "\n";        // 5 8

    cout << "\n=== Sliding Window ===\n";
    cout << minWindow("ADOBECODEBANC","ABC") << "\n";            // BANC
    cout << longestNoRepeat("abcabcbb") << "\n";                 // 3

    cout << "\n=== DP ===\n";
    cout << wildcardMatch("aa","*") << "\n";                     // true
    cout << wildcardMatch("cb","?a") << "\n";                    // false

    cout << "\n=== Roman ===\n";
    cout << romanToInt("IX") << "\n";                            // 9
    cout << intToRoman(1994) << "\n";                            // MCMXCIV

    cout << "\n=== Trie ===\n";
    Trie trie;
    trie.insert("apple");
    cout << trie.search("apple") << "\n";                        // true
    cout << trie.search("app") << "\n";                         // false
    cout << trie.startsWith("app") << "\n";                     // true
    trie.insert("app");
    cout << trie.search("app") << "\n";                         // true

    return 0;
}
