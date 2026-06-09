package com.example.accountservice.transaction.repository;

import com.example.accountservice.transaction.entity.Transaction;
import com.example.accountservice.transaction.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);

    Page<Transaction> findByAccountIdAndType(UUID accountId, TransactionType type, Pageable pageable);

    Page<Transaction> findByAccountIdAndCreatedAtBetween(
            UUID accountId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Optional<Transaction> findByReference(String reference);

    boolean existsByReference(String reference);
}
