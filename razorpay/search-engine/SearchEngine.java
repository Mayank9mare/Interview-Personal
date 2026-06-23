// Companies: Razorpay
// In-memory search engine for a tech blog: add/remove documents, search by keywords
// and tags, order results by keyword frequency or document size.
// Confirmed 4+x at Razorpay machine coding round (Oct/Nov 2024, Jun 2025).

import java.util.*;
import java.util.stream.*;

/**
 * Models an in-memory search engine for a collection of text documents.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>{@code invertedIndex}: word → set of docIds (O(1) keyword lookup)</li>
 *   <li>{@code tagIndex}: tag → set of docIds (O(1) tag lookup)</li>
 *   <li>{@code documents}: docId → Document (O(1) content retrieval)</li>
 * </ul>
 *
 * <p>Key invariant: every word present in a document's text is indexed in
 * {@code invertedIndex} at insert time; removal cleans up all index entries.
 *
 * <p>Extensibility: result ordering is pluggable via {@link SortingStrategy}.
 * New orderings (e.g., by recency, by author) add a class without touching
 * SearchEngine itself (Open-Closed Principle).
 *
 * <p>Thread safety: Not thread-safe. Wrap with external synchronisation for
 * concurrent access.
 */
public class SearchEngine {

    // ── Inner types ────────────────────────────────────────────────────────

    /**
     * A document stored in the engine.
     */
    static class Document {
        /** Unique identifier. */
        final String id;
        /** Raw text content. */
        final String content;
        /** Tags associated with this document. */
        final Set<String> tags;

        Document(String id, String content, Set<String> tags) {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("docId cannot be blank");
            if (content == null) throw new IllegalArgumentException("content cannot be null");
            this.id = id;
            this.content = content;
            this.tags = tags == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(tags));
        }

        /** Tokenises content into lower-case words (strips punctuation). */
        Set<String> words() {
            Set<String> result = new HashSet<>();
            for (String token : content.toLowerCase().split("[^a-z0-9]+")) {
                if (!token.isBlank()) result.add(token);
            }
            return result;
        }
    }

    /**
     * A search result paired with its relevance score for the query term.
     */
    static class SearchResult {
        final Document doc;
        /** Number of times the query keyword(s) appear in this document. */
        final int frequency;

        SearchResult(Document doc, int frequency) {
            this.doc = doc;
            this.frequency = frequency;
        }

        @Override
        public String toString() {
            return String.format("[%s | freq=%d | size=%d | \"%s\"]",
                    doc.id, frequency, doc.content.length(),
                    doc.content.length() > 40 ? doc.content.substring(0, 40) + "..." : doc.content);
        }
    }

    // ── Strategy interface ────────────────────────────────────────────────

    /**
     * Strategy for ordering a list of {@link SearchResult}s.
     *
     * <p>Implement this interface to add new orderings without modifying
     * {@link SearchEngine}.
     */
    interface SortingStrategy {
        /**
         * Sorts {@code results} in-place according to this strategy.
         *
         * @param results mutable list of search results to sort
         */
        void sort(List<SearchResult> results);
    }

    /** Orders by keyword frequency descending (most-mentioned document first). */
    static class FrequencyDescStrategy implements SortingStrategy {
        @Override
        public void sort(List<SearchResult> results) {
            results.sort((a, b) -> Integer.compare(b.frequency, a.frequency));
        }
    }

    /** Orders by document content size descending (longest document first). */
    static class DocumentSizeDescStrategy implements SortingStrategy {
        @Override
        public void sort(List<SearchResult> results) {
            results.sort((a, b) -> Integer.compare(b.doc.content.length(), a.doc.content.length()));
        }
    }

    /** Orders alphabetically by document id ascending. */
    static class AlphaByIdStrategy implements SortingStrategy {
        @Override
        public void sort(List<SearchResult> results) {
            results.sort(Comparator.comparing(r -> r.doc.id));
        }
    }

    // ── Engine state ──────────────────────────────────────────────────────

    /** docId → Document. */
    private final Map<String, Document> documents = new HashMap<>();

    /** word → set of docIds that contain that word. */
    private final Map<String, Set<String>> invertedIndex = new HashMap<>();

    /** tag → set of docIds with that tag. */
    private final Map<String, Set<String>> tagIndex = new HashMap<>();

    /** Active ordering strategy (defaults to frequency descending). */
    private SortingStrategy sortingStrategy = new FrequencyDescStrategy();

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Sets the strategy used to order search results.
     *
     * @param strategy non-null sorting strategy
     */
    public void setSortingStrategy(SortingStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy cannot be null");
        this.sortingStrategy = strategy;
    }

    /**
     * Adds a document to the engine, indexing all its words and tags.
     *
     * <p>If a document with the same id already exists it is silently replaced
     * (old index entries are cleaned up first).
     *
     * @param id      unique document identifier
     * @param content plain-text content
     * @param tags    optional tags (may be null or empty)
     */
    public void addDocument(String id, String content, Set<String> tags) {
        if (documents.containsKey(id)) {
            removeDocument(id); // clean old index entries first
        }
        Document doc = new Document(id, content, tags);
        documents.put(id, doc);
        for (String word : doc.words()) {
            invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(id);
        }
        for (String tag : doc.tags) {
            tagIndex.computeIfAbsent(tag.toLowerCase(), k -> new HashSet<>()).add(id);
        }
    }

    /**
     * Removes a document and cleans up all index entries for it.
     *
     * @param id document identifier to remove
     * @throws NoSuchElementException if no document with that id exists
     */
    public void removeDocument(String id) {
        Document doc = documents.remove(id);
        if (doc == null) throw new NoSuchElementException("No document found with id: " + id);
        for (String word : doc.words()) {
            Set<String> ids = invertedIndex.get(word);
            if (ids != null) {
                ids.remove(id);
                if (ids.isEmpty()) invertedIndex.remove(word);
            }
        }
        for (String tag : doc.tags) {
            Set<String> ids = tagIndex.get(tag.toLowerCase());
            if (ids != null) {
                ids.remove(id);
                if (ids.isEmpty()) tagIndex.remove(tag.toLowerCase());
            }
        }
    }

    /**
     * Searches for documents containing <em>all</em> query words (AND semantics).
     *
     * <p>Score = total occurrences of all query terms in the document text.
     * Results are ordered by the active {@link SortingStrategy}.
     *
     * @param query space-separated search terms
     * @return ordered list of matching search results (empty if no match)
     */
    public List<SearchResult> search(String query) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        String[] terms = query.toLowerCase().split("[^a-z0-9]+");
        List<String> validTerms = Arrays.stream(terms)
                .filter(t -> !t.isBlank())
                .collect(Collectors.toList());
        if (validTerms.isEmpty()) return Collections.emptyList();

        // Intersection: start with docs matching first term, intersect with rest
        Set<String> candidates = new HashSet<>(
                invertedIndex.getOrDefault(validTerms.get(0), Collections.emptySet()));
        for (int i = 1; i < validTerms.size(); i++) {
            candidates.retainAll(invertedIndex.getOrDefault(validTerms.get(i), Collections.emptySet()));
            if (candidates.isEmpty()) return Collections.emptyList();
        }

        List<SearchResult> results = new ArrayList<>();
        for (String docId : candidates) {
            Document doc = documents.get(docId);
            int freq = countFrequency(doc.content.toLowerCase(), validTerms);
            results.add(new SearchResult(doc, freq));
        }
        sortingStrategy.sort(results);
        return results;
    }

    /**
     * Searches for documents that have the given tag (case-insensitive).
     *
     * <p>Results are ordered by the active {@link SortingStrategy} with frequency=0.
     *
     * @param tag tag to search for
     * @return ordered list of matching search results
     */
    public List<SearchResult> searchByTag(String tag) {
        if (tag == null || tag.isBlank()) return Collections.emptyList();
        Set<String> ids = tagIndex.getOrDefault(tag.toLowerCase(), Collections.emptySet());
        List<SearchResult> results = new ArrayList<>();
        for (String id : ids) {
            results.add(new SearchResult(documents.get(id), 0));
        }
        sortingStrategy.sort(results);
        return results;
    }

    /** Returns the total number of documents in the engine. */
    public int size() {
        return documents.size();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Counts total occurrences of all terms in the given text. */
    private int countFrequency(String text, List<String> terms) {
        int count = 0;
        String[] words = text.split("[^a-z0-9]+");
        for (String word : words) {
            if (terms.contains(word)) count++;
        }
        return count;
    }

    // ── Demo ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SearchEngine engine = new SearchEngine();

        // Populate with tech-blog documents
        engine.addDocument("doc1", "Apple is a fruit. Apple pie is sweet.",
                new HashSet<>(Arrays.asList("food", "fruit")));
        engine.addDocument("doc2", "Apple, apple come on! Apple rules the market.",
                new HashSet<>(Arrays.asList("tech", "brand")));
        engine.addDocument("doc3", "Oranges are sour fruits grown in warm climates.",
                new HashSet<>(Arrays.asList("food", "fruit")));
        engine.addDocument("doc4", "Apple-pie is a classic American dessert.",
                new HashSet<>(Arrays.asList("food", "dessert")));
        engine.addDocument("doc5", "Java and Apple are both popular in the tech world.",
                new HashSet<>(Arrays.asList("tech", "programming")));

        System.out.println("=== Search: 'apple' (default: frequency desc) ===");
        for (SearchResult r : engine.search("apple")) {
            System.out.println("  " + r);
        }
        // Expected: doc2 (3 hits) first, then doc1/doc4 (2 hits), then doc5 (1 hit)

        System.out.println("\n=== Search: 'apple' (strategy: doc size desc) ===");
        engine.setSortingStrategy(new DocumentSizeDescStrategy());
        for (SearchResult r : engine.search("apple")) {
            System.out.println("  " + r);
        }
        // Expected: longest document first

        System.out.println("\n=== Search: 'apple pie' (AND semantics) ===");
        engine.setSortingStrategy(new FrequencyDescStrategy());
        for (SearchResult r : engine.search("apple pie")) {
            System.out.println("  " + r);
        }
        // Expected: doc1 and doc4 only (both contain 'apple' AND 'pie')

        System.out.println("\n=== Search by tag: 'food' ===");
        for (SearchResult r : engine.searchByTag("food")) {
            System.out.println("  " + r);
        }
        // Expected: doc1, doc3, doc4

        System.out.println("\n=== Remove doc2, search 'apple' again ===");
        engine.removeDocument("doc2");
        System.out.println("  Size after remove: " + engine.size()); // Expected: 4
        for (SearchResult r : engine.search("apple")) {
            System.out.println("  " + r);
        }
        // Expected: doc2 gone, doc1/doc4/doc5 remain

        System.out.println("\n=== Search non-existent term 'mango' ===");
        List<SearchResult> empty = engine.search("mango");
        System.out.println("  Results: " + empty.size()); // Expected: 0

        System.out.println("\n=== Search by tag: 'tech' ===");
        engine.setSortingStrategy(new AlphaByIdStrategy());
        for (SearchResult r : engine.searchByTag("tech")) {
            System.out.println("  " + r);
        }
        // Expected: doc5 (doc2 removed)
    }
}
