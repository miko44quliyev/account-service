package com.example.accountservice.transaction;

import com.example.accountservice.account.entity.Account;
import com.example.accountservice.account.entity.AccountStatus;
import com.example.accountservice.account.repository.AccountRepository;
import com.example.accountservice.common.exception.ResourceNotFoundException;
import com.example.accountservice.transaction.dto.response.TransactionResponse;
import com.example.accountservice.transaction.entity.Transaction;
import com.example.accountservice.transaction.entity.TransactionStatus;
import com.example.accountservice.transaction.entity.TransactionType;
import com.example.accountservice.transaction.mapper.TransactionMapper;
import com.example.accountservice.transaction.repository.TransactionRepository;
import com.example.accountservice.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock TransactionMapper transactionMapper;
    @InjectMocks TransactionService transactionService;

    private UUID accountId;
    private Account account;
    private Transaction transaction;
    private TransactionResponse txResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = Account.builder()
                .id(accountId)
                .accountNumber("ACC001")
                .balance(BigDecimal.valueOf(500))
                .status(AccountStatus.ACTIVE)
                .build();

        transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .account(account)
                .reference("TXN-ABC123")
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .balanceBefore(BigDecimal.valueOf(400))
                .balanceAfter(BigDecimal.valueOf(500))
                .status(TransactionStatus.COMPLETED)
                .build();

        txResponse = TransactionResponse.builder()
                .id(transaction.getId())
                .reference("TXN-ABC123")
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(100))
                .accountId(accountId)
                .build();

        pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
    }

    @Test
    @DisplayName("getTransactions - returns page of transactions")
    void getTransactions_success() {
        when(accountRepository.existsById(accountId)).thenReturn(true);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        when(transactionRepository.findByAccountId(accountId, pageable)).thenReturn(page);
        when(transactionMapper.toResponse(transaction)).thenReturn(txResponse);

        Page<TransactionResponse> result = transactionService.getTransactions(accountId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getReference()).isEqualTo("TXN-ABC123");
    }

    @Test
    @DisplayName("getTransactions - account not found throws")
    void getTransactions_accountNotFound() {
        when(accountRepository.existsById(accountId)).thenReturn(false);
        assertThatThrownBy(() -> transactionService.getTransactions(accountId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTransactionById - returns matching transaction")
    void getTransactionById_success() {
        UUID txId = transaction.getId();
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(txResponse);

        TransactionResponse result = transactionService.getTransactionById(txId);
        assertThat(result.getReference()).isEqualTo("TXN-ABC123");
    }

    @Test
    @DisplayName("getTransactionById - not found throws")
    void getTransactionById_notFound() {
        UUID txId = UUID.randomUUID();
        when(transactionRepository.findById(txId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionService.getTransactionById(txId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTransactionByReference - returns correct transaction")
    void getTransactionByReference_success() {
        when(transactionRepository.findByReference("TXN-ABC123"))
                .thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(txResponse);

        TransactionResponse result = transactionService.getTransactionByReference("TXN-ABC123");
        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
    }
}
