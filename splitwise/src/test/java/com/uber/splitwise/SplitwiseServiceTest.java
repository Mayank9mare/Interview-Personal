package com.uber.splitwise;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SplitwiseServiceTest {
    @Test
    void equalExpenseUpdatesNetBalances() {
        SplitwiseService service = new SplitwiseService();
        service.addGroup("trip", List.of("a", "b", "c"));

        service.addEqualExpense("trip", "a", 300);

        assertEquals(200, service.getNetBalance("a"));
        assertEquals(-100, service.getNetBalance("b"));
        assertEquals(-100, service.getNetBalance("c"));
    }

    @Test
    void simplifyDebtsCreatesMinimalSettlements() {
        SplitwiseService service = new SplitwiseService();
        service.addExpense("a", Map.of("a", 100, "b", 100, "c", 100));

        List<SplitwiseService.Settlement> settlements = service.simplifyDebts();

        assertEquals(2, settlements.size());
        assertEquals(200, settlements.stream().mapToInt(s -> s.amount).sum());
    }
}
