package com.uber.leaderboard;

import java.util.*;

public class Leaderboard {
    private final Map<String, List<String>> userPlayers = new HashMap<>();
    private final Map<String, Integer> playerScores = new HashMap<>();

    public void addUser(String userId, List<String> playerIds) {
        userPlayers.put(userId, new ArrayList<>(playerIds));
        for (String p : playerIds) playerScores.putIfAbsent(p, 0);
    }

    public void addScore(String playerId, int delta) {
        playerScores.merge(playerId, delta, Integer::sum);
    }

    public List<String> getTopK(int k) {
        List<String> users = new ArrayList<>(userPlayers.keySet());
        users.sort((a, b) -> {
            int sa = userScore(a), sb = userScore(b);
            if (sb != sa) return Integer.compare(sb, sa);
            return a.compareTo(b);
        });
        return users.subList(0, Math.min(k, users.size()));
    }

    private int userScore(String userId) {
        int total = 0;
        for (String p : userPlayers.get(userId)) total += playerScores.getOrDefault(p, 0);
        return total;
    }
}
