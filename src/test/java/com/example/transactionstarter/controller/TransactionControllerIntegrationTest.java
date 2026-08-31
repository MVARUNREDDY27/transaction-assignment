package com.example.transactionstarter.controller;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.entity.TransactionStatus;
import com.example.transactionstarter.entity.TransactionType;
import com.example.transactionstarter.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
    }

    @Nested
    @DisplayName("Starter Endpoint Verification")
    class StarterEndpointTests {
        @Test
        @DisplayName("Verify existing sample endpoint /api/sample remains functional")
        void sampleEndpointWorks() throws Exception {
            mockMvc.perform(get("/api/sample"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Starter project is running")));
        }
    }

    @Nested
    @DisplayName("Operation A: Create Transaction")
    class CreateTransactionTests {

        @Test
        @DisplayName("1. Minimum Requirement: A transaction created successfully (201 Created)")
        void createTransaction_Success() throws Exception {
            CreateTransactionRequest request = new CreateTransactionRequest(
                    "TX-1001",
                    "CUST-001",
                    new BigDecimal("250.75"),
                    "USD",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.transactionId", is("TX-1001")))
                    .andExpect(jsonPath("$.customerId", is("CUST-001")))
                    .andExpect(jsonPath("$.amount", is(250.75)))
                    .andExpect(jsonPath("$.currency", is("USD")))
                    .andExpect(jsonPath("$.type", is("PAYMENT")))
                    .andExpect(jsonPath("$.status", is("PENDING")))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.updatedAt", notNullValue()));
        }

        @Test
        @DisplayName("Create transaction with omitted status defaults to PENDING (201 Created)")
        void createTransaction_DefaultStatus_Success() throws Exception {
            CreateTransactionRequest request = new CreateTransactionRequest(
                    "TX-1002",
                    "CUST-002",
                    new BigDecimal("80.00"),
                    "EUR",
                    TransactionType.DEPOSIT,
                    null
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.transactionId", is("TX-1002")))
                    .andExpect(jsonPath("$.status", is("PENDING")));
        }

        @Test
        @DisplayName("2. Minimum Requirement: A transaction rejected because it fails validation (400 Bad Request)")
        void createTransaction_ValidationFailure_InvalidAmountAndBlankFields() throws Exception {
            CreateTransactionRequest invalidRequest = new CreateTransactionRequest(
                    "", // blank transactionId
                    "", // blank customerId
                    new BigDecimal("-10.00"), // negative amount
                    "INVALID_CURRENCY", // invalid currency code
                    null, // null type
                    null
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.validationErrors", notNullValue()));
        }

        @Test
        @DisplayName("Reject transaction with zero amount (400 Bad Request)")
        void createTransaction_ZeroAmount_Rejected() throws Exception {
            CreateTransactionRequest zeroAmountRequest = new CreateTransactionRequest(
                    "TX-ZERO",
                    "CUST-001",
                    BigDecimal.ZERO,
                    "USD",
                    TransactionType.DEPOSIT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(zeroAmountRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }

        @Test
        @DisplayName("Reject transaction with excessive decimal fraction (400 Bad Request)")
        void createTransaction_ExcessiveDecimals_Rejected() throws Exception {
            CreateTransactionRequest request = new CreateTransactionRequest(
                    "TX-DECIMAL",
                    "CUST-001",
                    new BigDecimal("10.999"), // 3 decimal places
                    "USD",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }

        @Test
        @DisplayName("Reject transaction with malformed JSON body or invalid enum (400 Bad Request)")
        void createTransaction_MalformedJson_Rejected() throws Exception {
            String malformedJson = "{\"transactionId\":\"TX-1\",\"type\":\"NON_EXISTENT_TYPE\"}";

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }

        @Test
        @DisplayName("3. Minimum Requirement: A duplicate Transaction ID rejected (409 Conflict)")
        void createTransaction_DuplicateId_Returns409Conflict() throws Exception {
            CreateTransactionRequest firstRequest = new CreateTransactionRequest(
                    "TX-DUPLICATE",
                    "CUST-001",
                    new BigDecimal("100.00"),
                    "EUR",
                    TransactionType.DEPOSIT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isCreated());

            // Attempt to create again with identical transactionId
            CreateTransactionRequest duplicateRequest = new CreateTransactionRequest(
                    "TX-DUPLICATE",
                    "CUST-002",
                    new BigDecimal("200.00"),
                    "GBP",
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.error", is("Conflict")))
                    .andExpect(jsonPath("$.message", is("Transaction with ID 'TX-DUPLICATE' already exists")));
        }
    }

    @Nested
    @DisplayName("Operation B: Get Transaction")
    class GetTransactionTests {

        @Test
        @DisplayName("4. Minimum Requirement: A request for a transaction that does not exist (404 Not Found)")
        void getTransaction_NotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/transactions/TX-NONEXISTENT"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", is("Transaction not found with ID: TX-NONEXISTENT")));
        }

        @Test
        @DisplayName("Retrieve existing transaction successfully (200 OK)")
        void getTransaction_Success() throws Exception {
            CreateTransactionRequest request = new CreateTransactionRequest(
                    "TX-GET-01",
                    "CUST-999",
                    new BigDecimal("500.00"),
                    "GBP",
                    TransactionType.TRANSFER,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/transactions/TX-GET-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId", is("TX-GET-01")))
                    .andExpect(jsonPath("$.customerId", is("CUST-999")))
                    .andExpect(jsonPath("$.amount", is(500.00)))
                    .andExpect(jsonPath("$.currency", is("GBP")))
                    .andExpect(jsonPath("$.type", is("TRANSFER")))
                    .andExpect(jsonPath("$.status", is("PENDING")));
        }
    }

    @Nested
    @DisplayName("Operation C: Update Transaction Status")
    class UpdateTransactionStatusTests {

        @Test
        @DisplayName("Update transaction status from PENDING to COMPLETED via PATCH (200 OK)")
        void updateStatus_PendingToCompleted_Patch_Success() throws Exception {
            CreateTransactionRequest createRequest = new CreateTransactionRequest(
                    "TX-STATUS-01",
                    "CUST-001",
                    new BigDecimal("75.00"),
                    "USD",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());

            UpdateStatusRequest updateRequest = new UpdateStatusRequest(TransactionStatus.COMPLETED);

            mockMvc.perform(patch("/api/transactions/TX-STATUS-01/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId", is("TX-STATUS-01")))
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("Update transaction status from PENDING to CANCELLED via PUT (200 OK)")
        void updateStatus_PendingToCancelled_Put_Success() throws Exception {
            CreateTransactionRequest createRequest = new CreateTransactionRequest(
                    "TX-STATUS-CANCEL",
                    "CUST-001",
                    new BigDecimal("75.00"),
                    "USD",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());

            UpdateStatusRequest updateRequest = new UpdateStatusRequest(TransactionStatus.CANCELLED);

            mockMvc.perform(put("/api/transactions/TX-STATUS-CANCEL/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId", is("TX-STATUS-CANCEL")))
                    .andExpect(jsonPath("$.status", is("CANCELLED")));
        }

        @Test
        @DisplayName("Update transaction status from COMPLETED to REFUNDED (200 OK)")
        void updateStatus_CompletedToRefunded_Success() throws Exception {
            CreateTransactionRequest createRequest = new CreateTransactionRequest(
                    "TX-STATUS-02",
                    "CUST-001",
                    new BigDecimal("75.00"),
                    "USD",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());

            // PENDING -> COMPLETED
            mockMvc.perform(patch("/api/transactions/TX-STATUS-02/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateStatusRequest(TransactionStatus.COMPLETED))))
                    .andExpect(status().isOk());

            // COMPLETED -> REFUNDED
            mockMvc.perform(patch("/api/transactions/TX-STATUS-02/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateStatusRequest(TransactionStatus.REFUNDED))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("REFUNDED")));
        }

        @Test
        @DisplayName("Illegal transition from COMPLETED to PENDING rejected (422 Unprocessable Entity)")
        void updateStatus_IllegalTransition_Returns422() throws Exception {
            CreateTransactionRequest createRequest = new CreateTransactionRequest(
                    "TX-STATUS-03",
                    "CUST-001",
                    new BigDecimal("120.00"),
                    "USD",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );

            mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());

            // Move to COMPLETED
            mockMvc.perform(patch("/api/transactions/TX-STATUS-03/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateStatusRequest(TransactionStatus.COMPLETED))))
                    .andExpect(status().isOk());

            // Try invalid move COMPLETED -> PENDING
            mockMvc.perform(patch("/api/transactions/TX-STATUS-03/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateStatusRequest(TransactionStatus.PENDING))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status", is(422)))
                    .andExpect(jsonPath("$.error", is("Unprocessable Entity")));
        }

        @Test
        @DisplayName("Update status for non-existent transaction returns 404 Not Found")
        void updateStatus_NotFound_Returns404() throws Exception {
            UpdateStatusRequest updateRequest = new UpdateStatusRequest(TransactionStatus.COMPLETED);

            mockMvc.perform(patch("/api/transactions/TX-DOES-NOT-EXIST/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    @Nested
    @DisplayName("Operation D: Get Customer Transactions")
    class GetCustomerTransactionsTests {

        @Test
        @DisplayName("Retrieve transactions for customer via query parameter")
        void getCustomerTransactions_QueryParam_Success() throws Exception {
            CreateTransactionRequest tx1 = new CreateTransactionRequest(
                    "TX-CUST-1",
                    "CUST-VIP",
                    new BigDecimal("100.00"),
                    "USD",
                    TransactionType.DEPOSIT,
                    TransactionStatus.COMPLETED
            );
            CreateTransactionRequest tx2 = new CreateTransactionRequest(
                    "TX-CUST-2",
                    "CUST-VIP",
                    new BigDecimal("50.00"),
                    "USD",
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.PENDING
            );
            CreateTransactionRequest txOther = new CreateTransactionRequest(
                    "TX-OTHER",
                    "CUST-OTHER",
                    new BigDecimal("999.00"),
                    "EUR",
                    TransactionType.PAYMENT,
                    TransactionStatus.COMPLETED
            );

            mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tx1)));
            mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tx2)));
            mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(txOther)));

            mockMvc.perform(get("/api/transactions").param("customerId", "CUST-VIP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].transactionId", containsInAnyOrder("TX-CUST-1", "TX-CUST-2")));
        }

        @Test
        @DisplayName("Retrieve transactions for customer via REST path /api/customers/{customerId}/transactions")
        void getCustomerTransactions_RestPath_Success() throws Exception {
            CreateTransactionRequest tx1 = new CreateTransactionRequest(
                    "TX-CUST-3",
                    "CUST-TEST",
                    new BigDecimal("300.00"),
                    "USD",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );
            mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tx1)));

            mockMvc.perform(get("/api/customers/CUST-TEST/transactions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].transactionId", is("TX-CUST-3")));
        }

        @Test
        @DisplayName("Retrieve transactions for customer via REST path /api/transactions/customer/{customerId}")
        void getCustomerTransactions_AlternativePath_Success() throws Exception {
            CreateTransactionRequest tx1 = new CreateTransactionRequest(
                    "TX-CUST-4",
                    "CUST-TEST-2",
                    new BigDecimal("450.00"),
                    "GBP",
                    TransactionType.PAYMENT,
                    TransactionStatus.PENDING
            );
            mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tx1)));

            mockMvc.perform(get("/api/transactions/customer/CUST-TEST-2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].transactionId", is("TX-CUST-4")));
        }

        @Test
        @DisplayName("Retrieve transactions for customer with no records returns empty array []")
        void getCustomerTransactions_NoRecords_ReturnsEmptyList() throws Exception {
            mockMvc.perform(get("/api/transactions").param("customerId", "CUST-EMPTY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", empty()));
        }

        @Test
        @DisplayName("GET /api/transactions without customerId query param returns 400 Bad Request")
        void getCustomerTransactions_MissingParam_Returns400() throws Exception {
            mockMvc.perform(get("/api/transactions"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }
    }
}
