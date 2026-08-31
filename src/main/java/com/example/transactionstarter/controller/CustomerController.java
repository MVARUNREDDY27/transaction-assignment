package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final TransactionService transactionService;

    public CustomerController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Operation D (Resource-oriented REST Path): Get all transactions for a customer
     */
    @GetMapping("/{customerId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getCustomerTransactions(@PathVariable String customerId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }
}
