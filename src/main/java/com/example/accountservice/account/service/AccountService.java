package com.example.accountservice.account.service;

import com.example.accountservice.account.dto.request.DepositRequest;
import com.example.accountservice.account.dto.request.TransferRequest;
import com.example.accountservice.account.dto.request.WithdrawRequest;
import com.example.accountservice.account.dto.response.AccountResponse;
import com.example.accountservice.account.entity.Account;
import com.example.accountservice.account.entity.AccountStatus;
import com.example.accountservice.account.mapper.AccountMapper;
import com.example.accountservice.account.repository.AccountRepository;
import com.example.accountservice.common.exception.BusinessException;
import com.example.accountservice.common.exception.ResourceNotFoundException;
import com.example.accountservice.transaction.entity.Transaction;
import com.example.accountservice.transaction.entity.TransactionType;
import com.example.accountservice.transaction.repository.TransactionRepository;
import com.example.accountservice.user.entity.User;
import com.example.accountservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse openAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Account account = Account.builder()
                .owner(user)
                .accountNumber(generateAccountNumber())
                .build();

        return accountMapper.toResponse(accountRepository.save(account));
    }

    public AccountResponse getAccount(UUID accountId) {
        Account account = accountRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        return accountMapper.toResponse(account);
    }

    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        return accountRepository.findAllByOwnerIdWithUser(userId)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Transactional
    public AccountResponse deposit(UUID accountId, DepositRequest request) {
        Account account = lockAccount(accountId);
        requireActive(account);

        BigDecimal before = account.getBalance();
        BigDecimal after = before.add(request.getAmount());
        account.setBalance(after);

        recordTransaction(account, TransactionType.DEPOSIT, request.getAmount(),
                before, after, request.getDescription());

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse withdraw(UUID accountId, WithdrawRequest request) {
        Account account = lockAccount(accountId);
        requireActive(account);
        requireSufficientBalance(account, request.getAmount());

        BigDecimal before = account.getBalance();
        BigDecimal after = before.subtract(request.getAmount());
        account.setBalance(after);

        recordTransaction(account, TransactionType.WITHDRAWAL, request.getAmount(),
                before, after, request.getDescription());

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse transfer(UUID sourceAccountId, TransferRequest request) {
        Account source = lockAccount(sourceAccountId);
        Account target = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Target account not found: " + request.getTargetAccountNumber()));

        if (source.getId().equals(target.getId())) {
            throw new BusinessException("Cannot transfer to the same account");
        }

        requireActive(source);
        requireActive(target);
        requireSufficientBalance(source, request.getAmount());

        BigDecimal sourceBefore = source.getBalance();
        BigDecimal sourceAfter = sourceBefore.subtract(request.getAmount());
        BigDecimal targetBefore = target.getBalance();
        BigDecimal targetAfter = targetBefore.add(request.getAmount());

        source.setBalance(sourceAfter);
        target.setBalance(targetAfter);

        String description = request.getDescription();
        recordTransaction(source, TransactionType.TRANSFER_OUT, request.getAmount(),
                sourceBefore, sourceAfter, description);
        recordTransaction(target, TransactionType.TRANSFER_IN, request.getAmount(),
                targetBefore, targetAfter, description);

        accountRepository.save(target);
        return accountMapper.toResponse(accountRepository.save(source));
    }

    @Transactional
    public AccountResponse freezeAccount(UUID accountId) {
        Account account = lockAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Cannot freeze a closed account");
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BusinessException("Account is already frozen");
        }
        account.setStatus(AccountStatus.FROZEN);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse unfreezeAccount(UUID accountId) {
        Account account = lockAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Cannot unfreeze a closed account");
        }
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new BusinessException("Account is not frozen");
        }
        account.setStatus(AccountStatus.ACTIVE);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse closeAccount(UUID accountId) {
        Account account = lockAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Account is already closed");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Cannot close an account with a non-zero balance");
        }
        account.setStatus(AccountStatus.CLOSED);
        return accountMapper.toResponse(accountRepository.save(account));
    }

    private Account lockAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Account " + account.getAccountNumber()
                    + " is " + account.getStatus().name().toLowerCase());
        }
    }

    private void requireSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }
    }

    private void recordTransaction(Account account, TransactionType type,
                                   BigDecimal amount, BigDecimal before, BigDecimal after,
                                   String description) {
        Transaction tx = Transaction.builder()
                .account(account)
                .reference(generateReference())
                .type(type)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .description(description)
                .build();
        transactionRepository.save(tx);
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = "ACC" + String.format("%017d",
                    Math.abs(Instant.now().toEpochMilli() * 1000 + (long) (Math.random() * 1000)));
        } while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    private String generateReference() {
        String ref;
        do {
            ref = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
        } while (transactionRepository.existsByReference(ref));
        return ref;
    }
}
