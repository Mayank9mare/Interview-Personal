import java.util.*;

/**
 * Entry point demonstrating {@link MusicAnalyticsSystem}.
 * Compile: {@code javac MusicAnalytics.java}  Run: {@code java MusicAnalytics}
 */
public class MusicAnalytics {

    /**
     * In-memory music analytics system tracking song plays per user.
     *
     * <p>Core data structures:
     * <ul>
     *   <li>{@code songUniqueUsers}: songId → {@code Set<userId>} — tracks which distinct
     *       users have played each song; {@code size()} gives the unique-play count.</li>
     *   <li>{@code recentlyPlayed}: userId → {@code LinkedHashSet<songId>} — insertion-ordered
     *       set where the tail is the most recently played song. Re-playing a song moves it
     *       to the tail via remove-then-add in O(1).</li>
     * </ul>
     *
     * <p>Core invariant: {@code recentlyPlayed} for any user contains no duplicates and
     * reflects the most-recent-last ordering of distinct songs that user has played.
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class MusicAnalyticsSystem {

        /** Songs registered in the catalog. */
        private final Set<String> catalog = new HashSet<>();

        /** Maps songId to the set of distinct userIds who have played it. */
        private final Map<String, Set<String>> songUniqueUsers = new HashMap<>();

        /** Maps userId to their play history; tail of the set = most recently played. */
        private final Map<String, LinkedHashSet<String>> recentlyPlayed = new HashMap<>();

        /**
         * Registers a song in the catalog. No-op if the song is already registered.
         *
         * @param songId unique identifier for the song
         */
        public void addSong(String songId) {
            if (catalog.add(songId)) {
                songUniqueUsers.put(songId, new HashSet<>());
            }
        }

        /**
         * Records a play event. Moves {@code songId} to the tail of the user's history,
         * bumping it to "most recently played". Increments the unique-user count if this
         * is the user's first play of the song.
         *
         * @param userId the user who played the song
         * @param songId the song played
         * @throws IllegalArgumentException if the song is not in the catalog
         */
        public void playSong(String userId, String songId) {
            if (!catalog.contains(songId))
                throw new IllegalArgumentException("Unknown song: " + songId);

            songUniqueUsers.get(songId).add(userId);

            // Remove-then-add moves the song to the tail (= most recent position)
            LinkedHashSet<String> history =
                recentlyPlayed.computeIfAbsent(userId, k -> new LinkedHashSet<>());
            history.remove(songId);
            history.add(songId);
        }

        /**
         * Prints every song ranked by unique-user count, descending.
         */
        public void printAnalytics() {
            System.out.println("=== Most Played Songs by Unique Users ===");
            songUniqueUsers.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .forEach(e -> System.out.printf("  %-20s → %d unique user(s)%n",
                                                e.getKey(), e.getValue().size()));
        }

        /**
         * Prints all unique songs the user has played, most recent first.
         *
         * @param userId the user whose history to display
         */
        public void printRecentlyPlayed(String userId) {
            printRecentlyPlayed(userId, Integer.MAX_VALUE);
        }

        /**
         * Prints the top-{@code k} most recently played unique songs for the user.
         *
         * @param userId the user whose history to display
         * @param k      maximum number of songs to print
         */
        public void printRecentlyPlayed(String userId, int k) {
            String label = (k == Integer.MAX_VALUE) ? "all" : String.valueOf(k);
            System.out.printf("=== Recently Played for %-10s (top %s) ===%n", userId, label);

            LinkedHashSet<String> history =
                recentlyPlayed.getOrDefault(userId, new LinkedHashSet<>());
            List<String> songs = new ArrayList<>(history);

            // Iterate from the tail (most recent) toward the head
            int printed = 0;
            for (int i = songs.size() - 1; i >= 0 && printed < k; i--, printed++) {
                System.out.println("  " + songs.get(i));
            }
            if (songs.isEmpty()) System.out.println("  (none)");
        }
    }

    public static void main(String[] args) {
        MusicAnalyticsSystem m = new MusicAnalyticsSystem();

        m.addSong("SongA");
        m.addSong("SongB");
        m.addSong("SongC");
        m.addSong("SongD");

        m.playSong("u1", "SongA");
        m.playSong("u2", "SongA");
        m.playSong("u3", "SongA");
        m.playSong("u1", "SongB");
        m.playSong("u2", "SongB");
        m.playSong("u1", "SongC");

        m.printAnalytics();
        // SongA: 3 unique, SongB: 2, SongC: 1, SongD: 0

        System.out.println();

        m.playSong("u1", "SongA");  // re-play — moves A back to tail of u1's history
        m.playSong("u1", "SongD");

        // u1's play order: B, C, A(re-played), D  →  most recent first: D, A, C, B
        m.printRecentlyPlayed("u1");
        System.out.println();
        m.printRecentlyPlayed("u1", 2);  // only D, A
        System.out.println();
        m.printRecentlyPlayed("u2");     // A, B  (u2's order: A then B → most recent first: B, A)
        System.out.println();
        m.printRecentlyPlayed("u9");     // unknown user → (none)
    }
}
