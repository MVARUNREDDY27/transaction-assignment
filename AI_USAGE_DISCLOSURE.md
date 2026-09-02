# AI Usage Disclosure Statement

**Project:** Transaction Processing Service (Spring Boot 3.5.5 / Java 17)  
**Candidate Name:** M. Varun Reddy  
**Development Environment:** Visual Studio Code  
**Submission Date:** September 2026  

---

## 1. Declaration & Overview
In alignment with programme guidelines (Section 7 — AI Usage & Assistance Policy), this document provides full transparency regarding the utilization of AI-assisted tools during the design, implementation, and verification of this transaction processing service. 

AI was utilized strictly as an **intelligent pair-programming assistant** within **Visual Studio Code**. All architectural decisions, state-machine designs, domain modeling, code reviews, and test validations were directed, reviewed, and finalized by the candidate.

---

## 2. Tools & Environments Used
* **Primary AI Tool:** Antigravity AI Coding Assistant (LLM-based pair programming)
* **IDE & Workflow:** Visual Studio Code (Terminal, REST Client, Java Extension Pack)
* **Build & Testing:** Apache Maven Wrapper (`mvnw.cmd`), JUnit 5, MockMvc

---

## 3. Scope of AI Assistance

| Category | AI Assistance Provided | Candidate Action & Ownership |
| :--- | :--- | :--- |
| **Boilerplate & Scaffolding** | Generated initial DTOs, JPA entities, and repository interfaces. | Reviewed field constraints, auditing timestamps, and table indexing. |
| **Domain State Machine** | Suggested transition matrix structure for transaction lifecycle. | Enforced encapsulation in `TransactionStatus.canTransitionTo()`; disallowed direct creation into terminal states. |
| **Validation Rules** | Proposed custom constraint validator for currency codes. | Implemented dynamic ISO-4217 validation using `java.util.Currency` (case-insensitive) rather than hardcoded lists. |
| **Test Suite Generation** | Drafted MockMvc integration test scenarios and Mockito unit tests. | Expanded test boundary cases (e.g., fractional precision, zero amounts, duplicate IDs, missing parameters) to 38 test cases. |
| **Exception Handling** | Identified edge-case exceptions for HTTP payload parsing. | Added handler for `HttpMessageNotReadableException` to ensure uniform JSON `400 Bad Request` structure. |

---

## 4. Key Decisions, Modifications & Rejected Suggestions
* **Rejected Fabricated Variant Limits:** Since no candidate-specific variant caps were supplied in the assignment prompt, AI suggestions proposing synthetic upper limit transaction caps (e.g., max \$10,000) were rejected in favor of pure positive monetary validation (`BigDecimal > 0.01`).
* **Enforced Terminal State Immutability:** Overrode generic status updates to guarantee that once a transaction enters a terminal state (`FAILED`, `CANCELLED`, `REFUNDED`), no further state modifications are permitted (returning `422 Unprocessable Entity`).
* **Multi-Endpoint Compatibility:** Introduced query parameter support (`/api/transactions?customerId=...`) alongside direct REST sub-resources (`/api/customers/{id}/transactions`) to handle varied client access patterns.

---

## 5. Verification & Quality Assurance
The entire solution was locally built, executed, and verified inside **Visual Studio Code**:
* **Compilation & Build:** `mvnw.cmd clean compile` (0 warnings, clean build).
* **Automated Test Suite:** `mvnw.cmd clean test` (38 tests run, 0 failures, 0 errors, 0 skipped — `BUILD SUCCESS`).
* **Manual API Verification:** Executed full end-to-end lifecycle calls via VS Code REST Client (`src/api-test.http`).
