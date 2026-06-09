package com.example.accountservice.transaction.service;

import com.example.accountservice.account.repository.AccountRepository;
import com.example.accountservice.common.exception.ResourceNotFoundException;
import com.example.accountservice.transaction.dto.response.TransactionResponse;
import com.example.accountservice.transaction.entity.TransactionType;
import com.example.accountservice.transaction.mapper.TransactionMapper;
import com.example.accountservice.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    public Page<TransactionResponse> getTransactions(UUID accountId, Pageable pageable) {
        requireAccountExists(accountId);
        return transactionRepository.findByAccountId(accountId, pageable)
                .map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByType(
            UUID accountId, TransactionType type, Pageable pageable) {
        requireAccountExists(accountId);
        return transactionRepository.findByAccountIdAndType(accountId, type, pageable)
                .map(transactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByDateRange(
            UUID accountId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        requireAccountExists(accountId);
        return transactionRepository
                .findByAccountIdAndCreatedAtBetween(accountId, from, to, pageable)
                .map(transactionMapper::toResponse);
    }

    public TransactionResponse getTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .map(transactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found: " + transactionId));
    }

    public TransactionResponse getTransactionByReference(String reference) {
        return transactionRepository.findByReference(reference)
                .map(transactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with reference: " + reference));
    }

    private void requireAccountExists(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
    }
}
