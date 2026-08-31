package com.example.transactionstarter.repository;

import com.example.transactionstarter.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    List<TransactionEntity> findByCustomerId(String customerId);

    List<TransactionEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    boolean existsByTransactionId(String transactionId);
}
