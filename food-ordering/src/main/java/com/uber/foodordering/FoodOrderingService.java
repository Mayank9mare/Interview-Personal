package com.uber.foodordering;

import java.util.*;

public class FoodOrderingService {
    private final Map<String, String[]> orders = new HashMap<>();
    private final Map<String, int[]> restaurantRatings = new HashMap<>();
    private final Map<String, int[]> foodRatings = new HashMap<>();
    private final Set<String> restaurants = new LinkedHashSet<>();
    private final Map<String, Set<String>> foodRestaurants = new HashMap<>();

    public void orderFood(String orderId, String restaurantId, String foodItemId) {
        orders.put(orderId, new String[]{restaurantId, foodItemId});
        restaurants.add(restaurantId);
        restaurantRatings.putIfAbsent(restaurantId, new int[]{0, 0});
        foodRatings.putIfAbsent(restaurantId + "|" + foodItemId, new int[]{0, 0});
        foodRestaurants.computeIfAbsent(foodItemId, k -> new LinkedHashSet<>()).add(restaurantId);
    }

    public void rateOrder(String orderId, int rating) {
        String[] info = orders.get(orderId);
        String restaurantId = info[0], foodItemId = info[1];
        int[] rr = restaurantRatings.get(restaurantId);
        rr[0] += rating; rr[1]++;
        int[] fr = foodRatings.get(restaurantId + "|" + foodItemId);
        fr[0] += rating; fr[1]++;
    }

    public List<String> getTopRestaurantsByFood(String foodItemId) {
        Set<String> rests = foodRestaurants.getOrDefault(foodItemId, Collections.emptySet());
        List<String> list = new ArrayList<>(rests);
        list.sort((a, b) -> {
            double ra = avgFoodRating(a, foodItemId), rb = avgFoodRating(b, foodItemId);
            if (rb != ra) return Double.compare(rb, ra);
            return a.compareTo(b);
        });
        return list.subList(0, Math.min(20, list.size()));
    }

    public List<String> getTopRatedRestaurants() {
        List<String> list = new ArrayList<>(restaurants);
        list.sort((a, b) -> {
            double ra = avgRestaurantRating(a), rb = avgRestaurantRating(b);
            if (rb != ra) return Double.compare(rb, ra);
            return a.compareTo(b);
        });
        return list.subList(0, Math.min(20, list.size()));
    }

    private double computeAvg(int[] data) {
        if (data[1] == 0) return 0.0;
        double raw = (double) data[0] / data[1];
        return (double)((int)((raw + 0.05) * 10)) / 10.0;
    }

    private double avgRestaurantRating(String restaurantId) {
        return computeAvg(restaurantRatings.getOrDefault(restaurantId, new int[]{0, 0}));
    }

    private double avgFoodRating(String restaurantId, String foodItemId) {
        return computeAvg(foodRatings.getOrDefault(restaurantId + "|" + foodItemId, new int[]{0, 0}));
    }
}
