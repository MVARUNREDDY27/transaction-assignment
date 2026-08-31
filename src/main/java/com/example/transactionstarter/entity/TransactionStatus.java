package com.example.transactionstarter.entity;

import java.util.EnumSet;
import java.util.Set;

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED;

    public boolean canTransitionTo(TransactionStatus newStatus) {
        if (this == newStatus) {
            return true; // Idempotent status update is permitted
        }

        return switch (this) {
            case PENDING -> EnumSet.of(COMPLETED, FAILED, CANCELLED).contains(newStatus);
            case COMPLETED -> EnumSet.of(REFUNDED).contains(newStatus);
            case FAILED, CANCELLED, REFUNDED -> false;
        };
    }

    public Set<TransactionStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(COMPLETED, FAILED, CANCELLED);
            case COMPLETED -> EnumSet.of(REFUNDED);
            case FAILED, CANCELLED, REFUNDED -> EnumSet.noneOf(TransactionStatus.class);
        };
    }
}
