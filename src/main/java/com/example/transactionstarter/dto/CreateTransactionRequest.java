package com.example.transactionstarter.dto;

import com.example.transactionstarter.entity.TransactionStatus;
import com.example.transactionstarter.entity.TransactionType;
import com.example.transactionstarter.validation.ValidCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateTransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 64, message = "Transaction ID cannot exceed 64 characters")
    private String transactionId;

    @NotBlank(message = "Customer ID is required")
    @Size(max = 64, message = "Customer ID cannot exceed 64 characters")
    private String customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be strictly greater than 0")
    @Digits(integer = 16, fraction = 2, message = "Amount cannot have more than 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @ValidCurrency
    private String currency;

    @NotNull(message = "Transaction type is required (e.g., PAYMENT, DEPOSIT, WITHDRAWAL, REFUND, TRANSFER)")
    private TransactionType type;

    private TransactionStatus status;

    public CreateTransactionRequest() {
    }

    public CreateTransactionRequest(String transactionId,
                                    String customerId,
                                    BigDecimal amount,
                                    String currency,
                                    TransactionType type,
                                    TransactionStatus status) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
