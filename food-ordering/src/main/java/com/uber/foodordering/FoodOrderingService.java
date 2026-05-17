package com.uber.foodordering;

import java.util.*;

/**
 * In-memory food ordering service that records orders, accepts ratings, and surfaces
 * top-ranked restaurants by overall rating or by their rating for a specific food item.
 * <p>
 * Ratings are accumulated as {@code [sum, count]} integer pairs to avoid floating-point
 * accumulation errors during updates; averages are rounded to one decimal place on retrieval.
 * Top-N queries return at most 20 results ordered by descending average rating with
 * lexicographic tie-breaking. Not thread-safe.
 */
public class FoodOrderingService {
    /** Maps orderId to {@code [restaurantId, foodItemId]}. */
    private final Map<String, String[]> orders = new HashMap<>();

    /**
     * Maps restaurantId to {@code [ratingSum, ratingCount]} across all orders for that restaurant.
     */
    private final Map<String, int[]> restaurantRatings = new HashMap<>();

    /**
     * Maps {@code "restaurantId|foodItemId"} composite key to {@code [ratingSum, ratingCount]}.
     */
    private final Map<String, int[]> foodRatings = new HashMap<>();

    /** Insertion-ordered set of all restaurant IDs that have received at least one order. */
    private final Set<String> restaurants = new LinkedHashSet<>();

    /** Maps foodItemId to the set of restaurants that have offered it. */
    private final Map<String, Set<String>> foodRestaurants = new HashMap<>();

    /**
     * Records a new order and initialises rating accumulators for the restaurant and food item
     * if they have not been seen before.
     *
     * @param orderId      unique order identifier
     * @param restaurantId restaurant fulfilling the order
     * @param foodItemId   food item that was ordered
     */
    public void orderFood(String orderId, String restaurantId, String foodItemId) {
        orders.put(orderId, new String[]{restaurantId, foodItemId});
        restaurants.add(restaurantId);
        restaurantRatings.putIfAbsent(restaurantId, new int[]{0, 0});
        foodRatings.putIfAbsent(restaurantId + "|" + foodItemId, new int[]{0, 0});
        foodRestaurants.computeIfAbsent(foodItemId, k -> new LinkedHashSet<>()).add(restaurantId);
    }

    /**
     * Applies a rating to the restaurant and food item associated with the given order.
     *
     * @param orderId unique order identifier (must have been registered via {@link #orderFood})
     * @param rating  numeric rating value
     */
    public void rateOrder(String orderId, int rating) {
        String[] info = orders.get(orderId);
        String restaurantId = info[0], foodItemId = info[1];
        int[] rr = restaurantRatings.get(restaurantId);
        rr[0] += rating; rr[1]++;
        int[] fr = foodRatings.get(restaurantId + "|" + foodItemId);
        fr[0] += rating; fr[1]++;
    }

    /**
     * Returns up to 20 restaurants that offer the given food item, ranked by their average rating
     * for that item (descending), with lexicographic ordering as a tie-breaker.
     *
     * @param foodItemId the food item to rank restaurants by
     * @return ordered list of restaurant IDs, at most 20 entries
     */
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

    /**
     * Returns up to 20 restaurants ranked by their overall average rating (descending),
     * with lexicographic ordering as a tie-breaker.
     *
     * @return ordered list of restaurant IDs, at most 20 entries
     */
    public List<String> getTopRatedRestaurants() {
        List<String> list = new ArrayList<>(restaurants);
        list.sort((a, b) -> {
            double ra = avgRestaurantRating(a), rb = avgRestaurantRating(b);
            if (rb != ra) return Double.compare(rb, ra);
            return a.compareTo(b);
        });
        return list.subList(0, Math.min(20, list.size()));
    }

    /** Computes a rounded-to-one-decimal average from a {@code [sum, count]} accumulator. */
    private double computeAvg(int[] data) {
        if (data[1] == 0) return 0.0;
        double raw = (double) data[0] / data[1];
        return (double)((int)((raw + 0.05) * 10)) / 10.0;
    }

    /** Returns the overall average rating for {@code restaurantId}. */
    private double avgRestaurantRating(String restaurantId) {
        return computeAvg(restaurantRatings.getOrDefault(restaurantId, new int[]{0, 0}));
    }

    /** Returns the average rating of {@code foodItemId} specifically at {@code restaurantId}. */
    private double avgFoodRating(String restaurantId, String foodItemId) {
        return computeAvg(foodRatings.getOrDefault(restaurantId + "|" + foodItemId, new int[]{0, 0}));
    }
}
