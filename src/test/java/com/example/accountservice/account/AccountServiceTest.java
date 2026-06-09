package com.example.accountservice.account;

import com.example.accountservice.account.dto.request.DepositRequest;
import com.example.accountservice.account.dto.request.TransferRequest;
import com.example.accountservice.account.dto.request.WithdrawRequest;
import com.example.accountservice.account.dto.response.AccountResponse;
import com.example.accountservice.account.entity.Account;
import com.example.accountservice.account.entity.AccountStatus;
import com.example.accountservice.account.mapper.AccountMapper;
import com.example.accountservice.account.repository.AccountRepository;
import com.example.accountservice.account.service.AccountService;
import com.example.accountservice.common.exception.BusinessException;
import com.example.accountservice.common.exception.ResourceNotFoundException;
import com.example.accountservice.transaction.repository.TransactionRepository;
import com.example.accountservice.user.entity.User;
import com.example.accountservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock UserRepository userRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock AccountMapper accountMapper;
    @InjectMocks AccountService accountService;

    private UUID userId;
    private UUID accountId;
    private User user;
    private Account account;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        user = User.builder().id(userId).firstName("Jane").lastName("Doe")
                .email("jane@example.com").phone("+1234567890").build();

        account = Account.builder()
                .id(accountId)
                .owner(user)
                .accountNumber("ACC12345678901234567")
                .balance(BigDecimal.valueOf(1000))
                .status(AccountStatus.ACTIVE)
                .build();

        accountResponse = AccountResponse.builder()
                .id(accountId)
                .accountNumber("ACC12345678901234567")
                .balance(account.getBalance())
                .status(AccountStatus.ACTIVE)
                .userId(userId)
                .build();
    }

    @Test
    @DisplayName("openAccount - success for first account")
    void openAccount_firstAccount_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        AccountResponse result = accountService.openAccount(userId);

        assertThat(result).isNotNull();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("openAccount - user can open multiple accounts")
    void openAccount_multipleAccounts_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.openAccount(userId);
        AccountResponse second = accountService.openAccount(userId);

        assertThat(second).isNotNull();
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("openAccount - user not found throws ResourceNotFoundException")
    void openAccount_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountService.openAccount(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    @DisplayName("getAccountsByUserId - returns all accounts for user")
    void getAccountsByUserId_returnsList() {
        Account second = Account.builder()
                .id(UUID.randomUUID()).owner(user)
                .accountNumber("ACC_SECOND")
                .balance(BigDecimal.valueOf(500))
                .status(AccountStatus.ACTIVE).build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findAllByOwnerIdWithUser(userId)).thenReturn(List.of(account, second));
        when(accountMapper.toResponse(any())).thenReturn(accountResponse);

        List<AccountResponse> results = accountService.getAccountsByUserId(userId);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("getAccountsByUserId - user not found throws")
    void getAccountsByUserId_userNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThatThrownBy(() -> accountService.getAccountsByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getAccountsByUserId - returns empty list when no accounts")
    void getAccountsByUserId_noAccounts() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(accountRepository.findAllByOwnerIdWithUser(userId)).thenReturn(List.of());

        List<AccountResponse> results = accountService.getAccountsByUserId(userId);
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("deposit - adds amount to balance")
    void deposit_success() {
        DepositRequest req = new DepositRequest();
        req.setAmount(BigDecimal.valueOf(500));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.existsByReference(any())).thenReturn(false);
        when(transactionRepository.save(any())).thenReturn(null);
        when(accountMapper.toResponse(any())).thenReturn(accountResponse);

        accountService.deposit(accountId, req);

        assertThat(account.getBalance()).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("deposit - frozen account throws BusinessException")
    void deposit_frozenAccount() {
        account.setStatus(AccountStatus.FROZEN);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        DepositRequest req = new DepositRequest();
        req.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> accountService.deposit(accountId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("frozen");
    }

    @Test
    @DisplayName("withdraw - deducts amount from balance")
    void withdraw_success() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(BigDecimal.valueOf(200));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.existsByReference(any())).thenReturn(false);
        when(transactionRepository.save(any())).thenReturn(null);
        when(accountMapper.toResponse(any())).thenReturn(accountResponse);

        accountService.withdraw(accountId, req);

        assertThat(account.getBalance()).isEqualByComparingTo("800");
    }

    @Test
    @DisplayName("withdraw - insufficient balance throws BusinessException")
    void withdraw_insufficientBalance() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(BigDecimal.valueOf(9999));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(accountId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    @DisplayName("transfer - moves funds between two accounts (even same owner)")
    void transfer_success_sameOwnerDifferentAccounts() {
        Account target = Account.builder()
                .id(UUID.randomUUID())
                .owner(user)   // same user, different account
                .accountNumber("TARGET_ACC")
                .balance(BigDecimal.valueOf(100))
                .status(AccountStatus.ACTIVE)
                .build();

        TransferRequest req = new TransferRequest();
        req.setTargetAccountNumber("TARGET_ACC");
        req.setAmount(BigDecimal.valueOf(300));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("TARGET_ACC")).thenReturn(Optional.of(target));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.existsByReference(any())).thenReturn(false);
        when(transactionRepository.save(any())).thenReturn(null);
        when(accountMapper.toResponse(any())).thenReturn(accountResponse);

        accountService.transfer(accountId, req);

        assertThat(account.getBalance()).isEqualByComparingTo("700");
        assertThat(target.getBalance()).isEqualByComparingTo("400");
    }

    @Test
    @DisplayName("transfer - same account ID throws BusinessException")
    void transfer_sameAccountId() {
        Account sameAccount = Account.builder()
                .id(accountId)
                .accountNumber("ACC12345678901234567")
                .balance(BigDecimal.valueOf(500))
                .status(AccountStatus.ACTIVE)
                .build();

        TransferRequest req = new TransferRequest();
        req.setTargetAccountNumber("ACC12345678901234567");
        req.setAmount(BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("ACC12345678901234567"))
                .thenReturn(Optional.of(sameAccount));

        assertThatThrownBy(() -> accountService.transfer(accountId, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("same account");
    }

    @Test
    @DisplayName("closeAccount - non-zero balance throws BusinessException")
    void closeAccount_nonZeroBalance() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        assertThatThrownBy(() -> accountService.closeAccount(accountId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("non-zero balance");
    }

    @Test
    @DisplayName("closeAccount - success when balance is zero")
    void closeAccount_success() {
        account.setBalance(BigDecimal.ZERO);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(accountMapper.toResponse(any())).thenReturn(accountResponse);

        accountService.closeAccount(accountId);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }
}
