package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.exception.BusinessValidationException;
import com.example.transactionstarter.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Operation A: Create a new transaction
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getTransactionId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Operation B: Get transaction by transaction ID
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String transactionId) {
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Operation C: Update transaction status
     * Supports both PATCH and PUT for flexible REST clients
     */
    @RequestMapping(value = "/{transactionId}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable String transactionId,
            @Valid @RequestBody UpdateStatusRequest request) {
        TransactionResponse response = transactionService.updateTransactionStatus(transactionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Operation D: Get all transactions for a customer via query parameter /api/transactions?customerId=xxx
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(name = "customerId", required = false) String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new BusinessValidationException("Query parameter 'customerId' is required to search transactions");
        }
        List<TransactionResponse> transactions = transactionService.getTransactionsByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Operation D (Alternative Path): Get all transactions for a customer via /api/transactions/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCustomerPath(@PathVariable String customerId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }
}
