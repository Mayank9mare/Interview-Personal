import java.util.*;

public class MusicAnalytics {

    // ═══════════════════════════════════════════════════════════════════════════
    // Music Analytics System
    //
    // add_song(songId)                 — register a song in the catalog
    // play_song(userId, songId)        — record a play event
    // print_analytics()                — songs ranked by unique user count (desc)
    // print_recently_played(userId)    — all unique songs for user, most recent first
    // print_recently_played(userId, k) — top-k most recently played unique songs
    //
    // Key data structures:
    //   songUniqueUsers : songId → Set<userId>
    //     Tracks which distinct users played each song.
    //     size() of the set = unique-play count. O(1) per play.
    //
    //   recentlyPlayed : userId → LinkedHashSet<songId>
    //     Maintains insertion order (tail = most recently played).
    //     On replay: remove(songId) then add(songId) → moves song to tail in O(1).
    //     "recently played" list is always deduplicated automatically.
    // ═══════════════════════════════════════════════════════════════════════════
    static class MusicAnalyticsSystem {

        private final Set<String> catalog = new HashSet<>();
        private final Map<String, Set<String>> songUniqueUsers = new HashMap<>();
        // tail of LinkedHashSet = most recently played
        private final Map<String, LinkedHashSet<String>> recentlyPlayed = new HashMap<>();

        public void addSong(String songId) {
            if (catalog.add(songId)) {
                songUniqueUsers.put(songId, new HashSet<>());
            }
        }

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

        // Rank every song by its unique-user count, descending
        public void printAnalytics() {
            System.out.println("=== Most Played Songs by Unique Users ===");
            songUniqueUsers.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .forEach(e -> System.out.printf("  %-20s → %d unique user(s)%n",
                                                e.getKey(), e.getValue().size()));
        }

        public void printRecentlyPlayed(String userId) {
            printRecentlyPlayed(userId, Integer.MAX_VALUE);
        }

        // Most recent first; stops after k songs
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
