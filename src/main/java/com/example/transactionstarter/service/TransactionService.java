package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateStatusRequest;

import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    TransactionResponse getTransactionById(String transactionId);

    TransactionResponse updateTransactionStatus(String transactionId, UpdateStatusRequest request);

    List<TransactionResponse> getTransactionsByCustomerId(String customerId);
}
