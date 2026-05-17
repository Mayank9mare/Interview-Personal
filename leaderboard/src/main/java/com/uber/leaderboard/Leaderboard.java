package com.uber.leaderboard;

import java.util.*;

/**
 * Ranks fantasy-sport users by the aggregate score of their selected players.
 *
 * <p>Each user owns a fixed roster of player IDs. A user's leaderboard score is
 * the sum of the current scores of all players on their roster. Scores accumulate
 * via {@link #addScore} and are never reset.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>{@code userPlayers}: {@code HashMap<userId, List<playerId>>} — roster lookup.</li>
 *   <li>{@code playerScores}: {@code HashMap<playerId, score>} — O(1) score update and read.</li>
 * </ul>
 *
 * <p>Core invariant: every player ID that appears in any roster has an entry in
 * {@code playerScores} (initialised to 0 on {@link #addUser}).
 *
 * <p>Thread safety: Not thread-safe.
 */
public class Leaderboard {

    /** Maps each userId to the ordered list of player IDs on their roster. */
    private final Map<String, List<String>> userPlayers = new HashMap<>();

    /** Maps each playerId to their current cumulative score. */
    private final Map<String, Integer> playerScores = new HashMap<>();

    /**
     * Registers a user with a fixed roster of players.
     * Any player not yet tracked is initialised with a score of 0.
     *
     * @param userId    unique identifier for the user
     * @param playerIds ordered list of player IDs forming the user's roster
     */
    public void addUser(String userId, List<String> playerIds) {
        userPlayers.put(userId, new ArrayList<>(playerIds));
        for (String p : playerIds) playerScores.putIfAbsent(p, 0);
    }

    /**
     * Adds {@code delta} to the given player's score.
     * If the player was not previously tracked, their score is initialised to {@code delta}.
     *
     * @param playerId unique identifier for the player
     * @param delta    score increment (may be negative)
     */
    public void addScore(String playerId, int delta) {
        playerScores.merge(playerId, delta, Integer::sum);
    }

    /**
     * Returns the top {@code k} users ranked by descending roster score,
     * with ties broken lexicographically by userId (ascending).
     *
     * <p>User scores are precomputed once before sorting to avoid O(p·log u)
     * recomputation inside the comparator; total cost is O(u·p + u·log u).
     *
     * @param k maximum number of users to return; clamped to the number of registered users
     * @return ordered list of up to {@code k} userIds
     */
    public List<String> getTopK(int k) {
        Map<String, Integer> scores = new HashMap<>();
        for (String u : userPlayers.keySet()) scores.put(u, userScore(u));

        List<String> users = new ArrayList<>(userPlayers.keySet());
        users.sort((a, b) -> {
            int diff = Integer.compare(scores.get(b), scores.get(a));
            return diff != 0 ? diff : a.compareTo(b);
        });
        return new ArrayList<>(users.subList(0, Math.min(k, users.size())));
    }

    /**
     * Computes the total score of all players on the given user's roster.
     *
     * @param userId the user whose roster score to compute
     * @return sum of current scores of all roster players
     */
    private int userScore(String userId) {
        int total = 0;
        for (String p : userPlayers.get(userId)) total += playerScores.getOrDefault(p, 0);
        return total;
    }
}
