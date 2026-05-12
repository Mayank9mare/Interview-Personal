package com.uber.wallet;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WalletPaymentSystemTest {
    @Test
    void transferMovesMoneyAndWritesLedger() {
        WalletPaymentSystem system = new WalletPaymentSystem();
        system.deposit("d1", "a", 500);

        WalletPaymentSystem.LedgerEntry entry = system.transfer("t1", "a", "b", 200);

        assertEquals(WalletPaymentSystem.Status.SUCCESS, entry.status);
        assertEquals(300, system.balance("a"));
        assertEquals(200, system.balance("b"));
        assertEquals(2, system.ledger().size());
    }

    @Test
    void idempotencyKeyPreventsDuplicateTransfer() {
        WalletPaymentSystem system = new WalletPaymentSystem();
        system.deposit("d1", "a", 500);

        system.transfer("t1", "a", "b", 200);
        system.transfer("t1", "a", "b", 200);

        assertEquals(300, system.balance("a"));
        assertEquals(200, system.balance("b"));
    }
}
