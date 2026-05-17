import java.util.*;

/**
 * Entry point demonstrating {@link AutocompleteSystem}.
 * Compile: {@code javac SearchAutocomplete.java}  Run: {@code java SearchAutocomplete}
 */
public class SearchAutocomplete {

    /**
     * Type-ahead autocomplete system backed by a Trie (LeetCode 642).
     *
     * <p>As the user types one character at a time, returns the top-3 historical sentences
     * matching the current prefix. Ranking: higher frequency first; ties broken
     * lexicographically (ascending).
     *
     * <p>Core data structure: each {@link TrieNode} stores a {@code Map<sentence, frequency>}
     * for all sentences whose path passes through it. This allows {@code getSuggestions} to
     * sort directly at the prefix node in O(k log k) rather than performing a DFS over all leaves.
     *
     * <p>When a sentence is added or incremented, every node along its path updates its frequency
     * map in O(L) where L = sentence length.
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class AutocompleteSystem {
        private static final int TOP_K = 3;

        /**
         * Trie node holding child links and an aggregated frequency map for all sentences
         * whose prefix passes through this node.
         */
        private static class TrieNode {
            /** Child nodes keyed by character. */
            final Map<Character, TrieNode>  children = new HashMap<>();

            /** All sentences passing through this node mapped to their accumulated frequencies. */
            final Map<String, Integer>      freq     = new HashMap<>();
        }

        /** Root of the Trie (empty-prefix node). */
        private final TrieNode root = new TrieNode();

        /** Characters typed so far since the last {@code '#'} submission. */
        private final StringBuilder currentInput = new StringBuilder();

        /**
         * Initialises the system with historical sentences and their hit counts.
         *
         * @param sentences array of historical query strings
         * @param times     corresponding frequencies (same length as {@code sentences})
         */
        public AutocompleteSystem(String[] sentences, int[] times) {
            for (int i = 0; i < sentences.length; i++)
                addSentence(sentences[i], times[i]);
        }

        /**
         * Processes one typed character.
         *
         * <p>If {@code c == '#'}: records the current buffer as a new sentence (freq += 1),
         * resets the buffer, and returns an empty list.
         * Otherwise: appends to the buffer and returns the top-3 matching sentences.
         *
         * @param c the typed character
         * @return top-3 completions sorted by frequency desc then lex asc, or empty on {@code '#'}
         */
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

        /** Inserts {@code sentence} into the Trie and increments every node's freq by {@code count}. */
        private void addSentence(String sentence, int count) {
            TrieNode node = root;
            for (char ch : sentence.toCharArray()) {
                node.children.putIfAbsent(ch, new TrieNode());
                node = node.children.get(ch);
                node.freq.merge(sentence, count, Integer::sum);
            }
        }

        /**
         * Returns up to {@link #TOP_K} sentences whose prefix matches {@code prefix},
         * ranked by frequency descending then lexicographically ascending.
         *
         * @param prefix the current typed prefix
         * @return sorted suggestion list (empty if no match)
         */
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
