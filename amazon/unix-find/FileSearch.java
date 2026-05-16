// Companies: Amazon
// In-memory file lookup tool — models the Unix "find" command using pluggable search criteria.

import java.util.*;

/**
 * In-memory file search system modelling a subset of the Unix {@code find} command.
 *
 * <p>Files are stored as (path → size) entries. Search is performed by applying a
 * {@link SearchCriteria} strategy to every file whose path starts with the given directory
 * prefix. Adding new search types requires only a new {@code SearchCriteria} implementation —
 * no changes to {@code FileSearch} itself (Strategy pattern / Open-Closed Principle).
 *
 * <p>Key behaviours:
 * <ul>
 *   <li>{@code putFile}: inserts or overwrites a file entry (update semantics, not append).</li>
 *   <li>{@code search}: returns all file paths under {@code dir} that satisfy criterion {@code id}.</li>
 * </ul>
 *
 * <p>Core invariant: {@code files} always reflects the latest size for each path.
 *
 * <p>Thread safety: Not thread-safe.
 */
public class FileSearch {

    // ── Strategy interface ────────────────────────────────────────────────

    /**
     * A pluggable search criterion applied to a single file entry.
     * Implement this to add new filter types without modifying FileSearch.
     */
    @FunctionalInterface
    interface SearchCriteria {
        /**
         * Returns true if the file should be included in search results.
         *
         * @param path the absolute file path (e.g. "/data/pics/photo.jpg")
         * @param size the file size in MB
         * @param arg  the criterion-specific argument string (e.g. "8" for size threshold)
         * @return true if the file matches this criterion
         */
        boolean matches(String path, int size, String arg);
    }

    // ── Built-in criteria ─────────────────────────────────────────────────

    /**
     * Criterion 1: file size strictly greater than {@code arg} MB.
     * Typical usage: {@code search(1, "/data", "8")} → files > 8 MB under /data.
     */
    static final SearchCriteria SIZE_GREATER_THAN =
        (path, size, arg) -> size > Integer.parseInt(arg);

    /**
     * Criterion 2: file path ends with the given extension string.
     * Typical usage: {@code search(2, "/work", ".xml")} → .xml files under /work.
     */
    static final SearchCriteria EXTENSION_MATCH =
        (path, size, arg) -> path.endsWith(arg);

    // ── Fields ────────────────────────────────────────────────────────────

    /** Maps absolute file path → size in MB. Overwrites on re-put. */
    private final Map<String, Integer> files = new HashMap<>();

    /**
     * Maps criterion ID → SearchCriteria strategy.
     * Criterion 1 and 2 are registered in the constructor; new ones can be added
     * via {@link #registerCriteria}.
     */
    private final Map<Integer, SearchCriteria> criteria = new HashMap<>();

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Constructs a new FileSearch and registers the two built-in criteria:
     * <ol>
     *   <li>Size greater than (MB)</li>
     *   <li>Extension match</li>
     * </ol>
     */
    public FileSearch() {
        criteria.put(1, SIZE_GREATER_THAN);
        criteria.put(2, EXTENSION_MATCH);
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Inserts or overwrites the file at {@code path} with the given {@code size}.
     *
     * @param path absolute file path (e.g. "/data/pics/photo.jpg")
     * @param size file size in MB (must be non-negative)
     */
    public void putFile(String path, int size) {
        files.put(path, size);
    }

    /**
     * Returns all file paths under {@code dir} that satisfy criterion {@code criteriaId}.
     *
     * <p>A file is "under" a directory if its path starts with {@code dir + "/"} or
     * equals {@code dir} exactly. This ensures {@code /data} does not match {@code /dataset/...}.
     *
     * @param criteriaId the registered criterion ID (1 = size filter, 2 = extension filter)
     * @param dir        the directory prefix to search under (e.g. "/data" or "/data/pics")
     * @param arg        criterion-specific argument (e.g. "8" for size, ".xml" for extension)
     * @return list of matching file paths in insertion order
     * @throws IllegalArgumentException if criteriaId is not registered
     */
    public List<String> search(int criteriaId, String dir, String arg) {
        SearchCriteria criterion = criteria.get(criteriaId);
        if (criterion == null)
            throw new IllegalArgumentException("Unknown criteria id: " + criteriaId);

        String prefix = dir.endsWith("/") ? dir : dir + "/"; // avoid /data matching /dataset
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : files.entrySet()) {
            String path = entry.getKey();
            int size = entry.getValue();
            if (path.startsWith(prefix) && criterion.matches(path, size, arg)) {
                result.add(path);
            }
        }
        return result;
    }

    /**
     * Registers a new search criterion under the given ID.
     * Allows extending the system at runtime without modifying this class.
     *
     * @param id       the criterion ID to register (must not already exist)
     * @param criteria the strategy implementation
     */
    public void registerCriteria(int id, SearchCriteria criteria) {
        this.criteria.put(id, criteria);
    }

    // ── Main ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        FileSearch s = new FileSearch();

        s.putFile("/data/pics/photoA.jpg", 4);
        s.putFile("/data/pics/movie.mp4", 12);
        s.putFile("/work/docs/readme.md", 1);
        s.putFile("/work/docs/report.xml", 7);

        // Overwrite — photoA.jpg size updated from 4 to 9
        s.putFile("/data/pics/photoA.jpg", 9);

        // Criterion 1: files > 8 MB inside /data
        List<String> r1 = s.search(1, "/data", "8");
        Collections.sort(r1);
        System.out.println("search(1, /data, 8) = " + r1);
        // Expected: [/data/pics/movie.mp4, /data/pics/photoA.jpg]

        // Criterion 2: files with ".xml" inside /work
        List<String> r2 = s.search(2, "/work", ".xml");
        System.out.println("search(2, /work, .xml) = " + r2);
        // Expected: [/work/docs/report.xml]

        // Directory prefix isolation: /data should not match /dataset
        s.putFile("/dataset/train.csv", 50);
        List<String> r3 = s.search(1, "/data", "8");
        Collections.sort(r3);
        System.out.println("search(1, /data, 8) after adding /dataset/train.csv = " + r3);
        // Expected: [/data/pics/movie.mp4, /data/pics/photoA.jpg]  (no /dataset file)

        // Extensibility: register a custom criterion (name contains substring)
        s.registerCriteria(3, (path, size, arg) -> path.contains(arg));
        List<String> r4 = s.search(3, "/work", "readme");
        System.out.println("search(3, /work, readme) = " + r4);
        // Expected: [/work/docs/readme.md]
    }
}
