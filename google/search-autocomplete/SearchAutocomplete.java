import java.util.*;

public class SearchAutocomplete {

    // ═══════════════════════════════════════════════════════════════════════════
    // Search Autocomplete System — Google interview problem (LeetCode 642)
    //
    // Simulates Google Search's type-ahead: as the user types character by
    // character, return the top-3 historical sentences matching the current prefix.
    //
    // Ranking: higher frequency first; ties broken lexicographically (ascending).
    //
    // API:
    //   AutocompleteSystem(String[] sentences, int[] times) — seed with history
    //   List<String> input(char c)  — type one character; '#' submits the query
    //     '#' records the current typed string as a new sentence (freq +1)
    //     and resets the current prefix. Returns empty list.
    //     Any other char appends to prefix and returns top-3 matching sentences.
    //
    // Design:
    //   • Trie for O(P) prefix lookup (P = prefix length).
    //   • Each TrieNode stores a Map<sentence, frequency> of all sentences
    //     passing through it — so getSuggestions is O(k log k) at the prefix node
    //     (k = unique sentences with that prefix), not a DFS over all leaves.
    //   • When a sentence is added/incremented, walk its path and update every
    //     node's freq map in O(L) where L = sentence length.
    //
    // Complexity: input(c) O(L + k log k) where L = current prefix length,
    //             k = number of matching sentences.
    //             Space: O(total characters across all sentences).
    // ═══════════════════════════════════════════════════════════════════════════

    static class AutocompleteSystem {
        private static final int TOP_K = 3;

        private static class TrieNode {
            final Map<Character, TrieNode>  children = new HashMap<>();
            // All sentences passing through this node and their frequencies
            final Map<String, Integer>      freq     = new HashMap<>();
        }

        private final TrieNode root = new TrieNode();
        private final StringBuilder currentInput = new StringBuilder();

        public AutocompleteSystem(String[] sentences, int[] times) {
            for (int i = 0; i < sentences.length; i++)
                addSentence(sentences[i], times[i]);
        }

        // Type one character. Returns top-3 completions, or [] on '#'.
        public List<String> input(char c) {
            if (c == '#') {
                addSentence(currentInput.toString(), 1);
                currentInput.setLength(0);
                return Collections.emptyList();
            }
            currentInput.append(c);
            return getSuggestions(currentInput.toString());
        }

        // ── Internals ─────────────────────────────────────────────────────────

        private void addSentence(String sentence, int count) {
            TrieNode node = root;
            for (char ch : sentence.toCharArray()) {
                node.children.putIfAbsent(ch, new TrieNode());
                node = node.children.get(ch);
                node.freq.merge(sentence, count, Integer::sum);
            }
        }

        private List<String> getSuggestions(String prefix) {
            TrieNode node = root;
            for (char ch : prefix.toCharArray()) {
                if (!node.children.containsKey(ch)) return Collections.emptyList();
                node = node.children.get(ch);
            }

            // Sort by freq desc, then lex asc; take top-3
            return node.freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                    .thenComparing(Map.Entry.comparingByKey()))
                .limit(TOP_K)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Example 1: Basic autocomplete            ");
        System.out.println("══════════════════════════════════════════");
        AutocompleteSystem sys1 = new AutocompleteSystem(
            new String[]{"i love you", "island", "iroman", "i love leetcode"},
            new int[]   {5,            3,         2,         2}
        );

        // User types 'i'
        System.out.println("input('i') → " + sys1.input('i'));
        // Expected: ["i love you"(5), "island"(3), "i love leetcode"(2)]
        // "iroman"(2) loses tie-break to "i love leetcode" lexicographically

        // User types ' '
        System.out.println("input(' ') → " + sys1.input(' '));
        // Only sentences starting with "i ": "i love you"(5), "i love leetcode"(2)

        // User types 'a'
        System.out.println("input('a') → " + sys1.input('a'));
        // No sentences start with "i a" → []

        // User submits with '#' — "i a" gets recorded as a new sentence
        System.out.println("input('#') → " + sys1.input('#'));

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 2: New sentence from typing      ");
        System.out.println("══════════════════════════════════════════");
        AutocompleteSystem sys2 = new AutocompleteSystem(
            new String[]{"hello world", "hello there", "help me"},
            new int[]   {3,              2,              5}
        );

        System.out.println("input('h') → " + sys2.input('h'));
        // ["help me"(5), "hello world"(3), "hello there"(2)]

        System.out.println("input('e') → " + sys2.input('e'));
        // same three sentences still match "he"

        System.out.println("input('l') → " + sys2.input('l'));
        // ["help me"(5), "hello world"(3), "hello there"(2)]  — all match "hel"

        System.out.println("input('p') → " + sys2.input('p'));
        // ["help me"(5)] — only sentence starting with "help"

        System.out.println("input('#') → " + sys2.input('#'));
        // records "help" as new sentence (freq 1)

        System.out.println("input('h') → " + sys2.input('h'));
        // "help me"(5) + "help"(1) now also visible; "hello world"(3), "hello there"(2)
        // Top 3: ["help me"(5), "hello world"(3), "hello there"(2)]

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 3: Frequency tie-breaking        ");
        System.out.println("══════════════════════════════════════════");
        AutocompleteSystem sys3 = new AutocompleteSystem(
            new String[]{"abc", "abd", "abe", "abf"},
            new int[]   {2,     2,     2,     2}
        );

        System.out.println("input('a') → " + sys3.input('a'));
        // All have freq 2; lex order: abc, abd, abe (abf is 4th, cut off)

        System.out.println("input('b') → " + sys3.input('b'));
        // Same result

        System.out.println("input('c') → " + sys3.input('c'));
        // Only "abc"

        System.out.println("input('#') → " + sys3.input('#'));
        // records "abc" → freq 3

        System.out.println("input('a') → " + sys3.input('a'));
        // "abc"(3) first, then "abd"(2), "abe"(2)
    }
}
