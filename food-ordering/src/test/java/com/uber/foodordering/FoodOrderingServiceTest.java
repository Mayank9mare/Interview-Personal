package com.uber.foodordering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FoodOrderingServiceTest {
    private FoodOrderingService svc;

    @BeforeEach
    void setUp() { svc = new FoodOrderingService(); }

    @Test
    void exampleFromProblemStatement() {
        svc.orderFood("order-0", "restaurant-0", "food-1");
        svc.rateOrder("order-0", 3);
        svc.orderFood("order-1", "restaurant-2", "food-0");
        svc.rateOrder("order-1", 1);
        svc.orderFood("order-2", "restaurant-1", "food-0");
        svc.rateOrder("order-2", 3);
        svc.orderFood("order-3", "restaurant-2", "food-0");
        svc.rateOrder("order-3", 5);
        svc.orderFood("order-4", "restaurant-0", "food-0");
        svc.rateOrder("order-4", 3);
        svc.orderFood("order-5", "restaurant-1", "food-0");
        svc.rateOrder("order-5", 4);
        svc.orderFood("order-6", "restaurant-0", "food-1");
        svc.rateOrder("order-6", 2);
        svc.orderFood("order-7", "restaurant-0", "food-1");
        svc.rateOrder("order-7", 2);
        svc.orderFood("order-8", "restaurant-1", "food-0");
        svc.rateOrder("order-8", 2);
        svc.orderFood("order-9", "restaurant-1", "food-0");
        svc.rateOrder("order-9", 4);

        List<String> byFood0 = svc.getTopRestaurantsByFood("food-0");
        assertEquals("restaurant-1", byFood0.get(0));
        assertEquals("restaurant-0", byFood0.get(1));
        assertEquals("restaurant-2", byFood0.get(2));

        List<String> byFood1 = svc.getTopRestaurantsByFood("food-1");
        assertEquals("restaurant-0", byFood1.get(0));

        List<String> top = svc.getTopRatedRestaurants();
        assertEquals("restaurant-1", top.get(0));
        assertEquals("restaurant-2", top.get(1));
        assertEquals("restaurant-0", top.get(2));
    }

    @Test
    void getTopRatedRestaurants_tieBrokenByRestaurantId() {
        svc.orderFood("o1", "restaurant-b", "food-1");
        svc.rateOrder("o1", 4);
        svc.orderFood("o2", "restaurant-a", "food-1");
        svc.rateOrder("o2", 4);
        List<String> top = svc.getTopRatedRestaurants();
        assertEquals("restaurant-a", top.get(0));
        assertEquals("restaurant-b", top.get(1));
    }

    @Test
    void getTopRestaurantsByFood_unratedAtBottom() {
        svc.orderFood("o1", "restaurant-a", "food-1");
        svc.rateOrder("o1", 5);
        svc.orderFood("o2", "restaurant-b", "food-1");
        // restaurant-b has no rating for food-1 -> avg 0.0
        List<String> result = svc.getTopRestaurantsByFood("food-1");
        assertEquals("restaurant-a", result.get(0));
        assertEquals("restaurant-b", result.get(1));
    }

    @Test
    void orderFood_multipleOrdersSameRestaurantAndFood() {
        svc.orderFood("o1", "r1", "f1");
        svc.rateOrder("o1", 2);
        svc.orderFood("o2", "r1", "f1");
        svc.rateOrder("o2", 4);
        List<String> result = svc.getTopRatedRestaurants();
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0));
    }

    @Test
    void getTopRestaurantsByFood_emptyFoodReturnsEmpty() {
        assertEquals(List.of(), svc.getTopRestaurantsByFood("nonexistent-food"));
    }

    @Test
    void getTopRatedRestaurants_emptyReturnsEmpty() {
        assertEquals(List.of(), svc.getTopRatedRestaurants());
    }
}
