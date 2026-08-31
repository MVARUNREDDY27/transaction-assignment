package com.example.transactionstarter.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionStatusTest {

    @Test
    @DisplayName("PENDING can transition to COMPLETED, FAILED, and CANCELLED")
    void pendingTransitions() {
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.COMPLETED));
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.FAILED));
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.CANCELLED));
        assertFalse(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.REFUNDED));
    }

    @Test
    @DisplayName("COMPLETED can only transition to REFUNDED")
    void completedTransitions() {
        assertTrue(TransactionStatus.COMPLETED.canTransitionTo(TransactionStatus.REFUNDED));
        assertFalse(TransactionStatus.COMPLETED.canTransitionTo(TransactionStatus.PENDING));
        assertFalse(TransactionStatus.COMPLETED.canTransitionTo(TransactionStatus.FAILED));
        assertFalse(TransactionStatus.COMPLETED.canTransitionTo(TransactionStatus.CANCELLED));
    }

    @Test
    @DisplayName("Terminal statuses (FAILED, CANCELLED, REFUNDED) cannot transition to any other status")
    void terminalStatuses() {
        assertFalse(TransactionStatus.FAILED.canTransitionTo(TransactionStatus.COMPLETED));
        assertFalse(TransactionStatus.FAILED.canTransitionTo(TransactionStatus.PENDING));

        assertFalse(TransactionStatus.CANCELLED.canTransitionTo(TransactionStatus.COMPLETED));
        assertFalse(TransactionStatus.CANCELLED.canTransitionTo(TransactionStatus.PENDING));

        assertFalse(TransactionStatus.REFUNDED.canTransitionTo(TransactionStatus.COMPLETED));
        assertFalse(TransactionStatus.REFUNDED.canTransitionTo(TransactionStatus.PENDING));
    }

    @Test
    @DisplayName("Self-transitions (idempotent updates) are permitted")
    void idempotentTransitions() {
        assertTrue(TransactionStatus.PENDING.canTransitionTo(TransactionStatus.PENDING));
        assertTrue(TransactionStatus.COMPLETED.canTransitionTo(TransactionStatus.COMPLETED));
        assertTrue(TransactionStatus.FAILED.canTransitionTo(TransactionStatus.FAILED));
        assertTrue(TransactionStatus.CANCELLED.canTransitionTo(TransactionStatus.CANCELLED));
        assertTrue(TransactionStatus.REFUNDED.canTransitionTo(TransactionStatus.REFUNDED));
    }
}
