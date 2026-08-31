package com.example.transactionstarter.dto;

import com.example.transactionstarter.entity.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Transaction status is required (e.g., PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED)")
    private TransactionStatus status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(TransactionStatus status) {
        this.status = status;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
