package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.entity.TransactionEntity;
import com.example.transactionstarter.entity.TransactionStatus;
import com.example.transactionstarter.exception.BusinessValidationException;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;
import com.example.transactionstarter.exception.ResourceNotFoundException;
import com.example.transactionstarter.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Transaction request cannot be null");
        }

        String transactionId = request.getTransactionId() != null ? request.getTransactionId().trim() : null;
        if (transactionId == null || transactionId.isEmpty()) {
            throw new BusinessValidationException("Transaction ID is required");
        }

        if (transactionRepository.existsById(transactionId)) {
            throw new DuplicateTransactionException("Transaction with ID '" + transactionId + "' already exists");
        }

        TransactionStatus initialStatus = request.getStatus();
        if (initialStatus == null) {
            initialStatus = TransactionStatus.PENDING;
        } else if (initialStatus != TransactionStatus.PENDING && initialStatus != TransactionStatus.COMPLETED) {
            throw new BusinessValidationException(
                    "Initial status for a new transaction must be PENDING or COMPLETED, but was: " + initialStatus);
        }

        TransactionEntity entity = new TransactionEntity(
                transactionId,
                request.getCustomerId() != null ? request.getCustomerId().trim() : null,
                request.getAmount(),
                request.getCurrency(),
                request.getType(),
                initialStatus
        );

        TransactionEntity saved = transactionRepository.save(entity);
        return TransactionResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new ResourceNotFoundException("Transaction ID must be provided");
        }

        TransactionEntity entity = transactionRepository.findById(transactionId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId.trim()));

        return TransactionResponse.fromEntity(entity);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransactionStatus(String transactionId, UpdateStatusRequest request) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new ResourceNotFoundException("Transaction ID must be provided");
        }

        if (request == null || request.getStatus() == null) {
            throw new BusinessValidationException("A valid target status is required");
        }

        TransactionEntity entity = transactionRepository.findById(transactionId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId.trim()));

        TransactionStatus currentStatus = entity.getStatus();
        TransactionStatus newStatus = request.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Illegal status transition from '%s' to '%s' for transaction '%s'. Allowed transitions from '%s': %s",
                            currentStatus, newStatus, transactionId, currentStatus, currentStatus.getAllowedTransitions())
            );
        }

        entity.setStatus(newStatus);
        TransactionEntity updated = transactionRepository.save(entity);
        return TransactionResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new BusinessValidationException("Customer ID must not be blank");
        }

        List<TransactionEntity> entities = transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId.trim());
        return entities.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
