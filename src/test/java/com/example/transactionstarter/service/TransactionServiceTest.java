package com.example.transactionstarter.service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.TransactionResponse;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.entity.TransactionEntity;
import com.example.transactionstarter.entity.TransactionStatus;
import com.example.transactionstarter.entity.TransactionType;
import com.example.transactionstarter.exception.BusinessValidationException;
import com.example.transactionstarter.exception.DuplicateTransactionException;
import com.example.transactionstarter.exception.InvalidStatusTransitionException;
import com.example.transactionstarter.exception.ResourceNotFoundException;
import com.example.transactionstarter.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private CreateTransactionRequest validRequest;
    private TransactionEntity sampleEntity;

    @BeforeEach
    void setUp() {
        validRequest = new CreateTransactionRequest(
                "TX-1001",
                "CUST-001",
                new BigDecimal("150.50"),
                "USD",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );

        sampleEntity = new TransactionEntity(
                "TX-1001",
                "CUST-001",
                new BigDecimal("150.50"),
                "USD",
                TransactionType.PAYMENT,
                TransactionStatus.PENDING
        );
        sampleEntity.setCreatedAt(Instant.now());
        sampleEntity.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Create transaction successfully with default PENDING status")
    void createTransaction_Success() {
        when(transactionRepository.existsById("TX-1001")).thenReturn(false);
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(sampleEntity);

        TransactionResponse response = transactionService.createTransaction(validRequest);

        assertNotNull(response);
        assertEquals("TX-1001", response.getTransactionId());
        assertEquals("CUST-001", response.getCustomerId());
        assertEquals(new BigDecimal("150.50"), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals(TransactionType.PAYMENT, response.getType());
        assertEquals(TransactionStatus.PENDING, response.getStatus());
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Create transaction fails when Transaction ID already exists")
    void createTransaction_DuplicateId_ThrowsDuplicateTransactionException() {
        when(transactionRepository.existsById("TX-1001")).thenReturn(true);

        assertThrows(DuplicateTransactionException.class, () -> transactionService.createTransaction(validRequest));
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Create transaction fails when initial status is illegal (e.g. CANCELLED)")
    void createTransaction_IllegalInitialStatus_ThrowsBusinessValidationException() {
        validRequest.setStatus(TransactionStatus.CANCELLED);
        when(transactionRepository.existsById("TX-1001")).thenReturn(false);

        assertThrows(BusinessValidationException.class, () -> transactionService.createTransaction(validRequest));
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Get transaction by ID successfully")
    void getTransactionById_Success() {
        when(transactionRepository.findById("TX-1001")).thenReturn(Optional.of(sampleEntity));

        TransactionResponse response = transactionService.getTransactionById("TX-1001");

        assertNotNull(response);
        assertEquals("TX-1001", response.getTransactionId());
        assertEquals("CUST-001", response.getCustomerId());
    }

    @Test
    @DisplayName("Get transaction by ID throws ResourceNotFoundException when not found")
    void getTransactionById_NotFound_ThrowsResourceNotFoundException() {
        when(transactionRepository.findById("TX-9999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById("TX-9999"));
    }

    @Test
    @DisplayName("Update transaction status from PENDING to COMPLETED successfully")
    void updateTransactionStatus_ValidTransition_Success() {
        when(transactionRepository.findById("TX-1001")).thenReturn(Optional.of(sampleEntity));

        TransactionEntity updatedEntity = new TransactionEntity(
                "TX-1001",
                "CUST-001",
                new BigDecimal("150.50"),
                "USD",
                TransactionType.PAYMENT,
                TransactionStatus.COMPLETED
        );
        updatedEntity.setCreatedAt(sampleEntity.getCreatedAt());
        updatedEntity.setUpdatedAt(Instant.now());

        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(updatedEntity);

        UpdateStatusRequest request = new UpdateStatusRequest(TransactionStatus.COMPLETED);
        TransactionResponse response = transactionService.updateTransactionStatus("TX-1001", request);

        assertNotNull(response);
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        verify(transactionRepository).save(sampleEntity);
    }

    @Test
    @DisplayName("Update transaction status with illegal transition throws InvalidStatusTransitionException")
    void updateTransactionStatus_InvalidTransition_ThrowsInvalidStatusTransitionException() {
        sampleEntity.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById("TX-1001")).thenReturn(Optional.of(sampleEntity));

        UpdateStatusRequest request = new UpdateStatusRequest(TransactionStatus.PENDING);

        assertThrows(InvalidStatusTransitionException.class, () ->
                transactionService.updateTransactionStatus("TX-1001", request));
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    @DisplayName("Update transaction status for non-existent ID throws ResourceNotFoundException")
    void updateTransactionStatus_NotFound_ThrowsResourceNotFoundException() {
        when(transactionRepository.findById("TX-NONEXISTENT")).thenReturn(Optional.empty());

        UpdateStatusRequest request = new UpdateStatusRequest(TransactionStatus.COMPLETED);

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.updateTransactionStatus("TX-NONEXISTENT", request));
    }

    @Test
    @DisplayName("Get customer transactions returns all customer records")
    void getTransactionsByCustomerId_Success() {
        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc("CUST-001"))
                .thenReturn(List.of(sampleEntity));

        List<TransactionResponse> results = transactionService.getTransactionsByCustomerId("CUST-001");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("CUST-001", results.get(0).getCustomerId());
    }

    @Test
    @DisplayName("Get customer transactions with blank ID throws BusinessValidationException")
    void getTransactionsByCustomerId_BlankId_ThrowsBusinessValidationException() {
        assertThrows(BusinessValidationException.class, () -> transactionService.getTransactionsByCustomerId("   "));
    }
}
