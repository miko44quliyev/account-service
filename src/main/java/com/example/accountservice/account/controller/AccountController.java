package com.example.accountservice.account.controller;

import com.example.accountservice.account.dto.request.DepositRequest;
import com.example.accountservice.account.dto.request.TransferRequest;
import com.example.accountservice.account.dto.request.WithdrawRequest;
import com.example.accountservice.account.dto.response.AccountResponse;
import com.example.accountservice.account.service.AccountService;
import com.example.accountservice.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AccountResponse>> openAccount(@PathVariable UUID userId) {
        AccountResponse response = accountService.openAccount(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account opened successfully", response));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccount(accountId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccountsByUserId(userId)));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<ApiResponse<AccountResponse>> deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Deposit successful",
                accountService.deposit(accountId, request)));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<ApiResponse<AccountResponse>> withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Withdrawal successful",
                accountService.withdraw(accountId, request)));
    }

    @PostMapping("/{accountId}/transfer")
    public ResponseEntity<ApiResponse<AccountResponse>> transfer(
            @PathVariable UUID accountId,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Transfer successful",
                accountService.transfer(accountId, request)));
    }

    @PostMapping("/{accountId}/freeze")
    public ResponseEntity<ApiResponse<AccountResponse>> freeze(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ApiResponse.ok("Account frozen",
                accountService.freezeAccount(accountId)));
    }

    @PostMapping("/{accountId}/unfreeze")
    public ResponseEntity<ApiResponse<AccountResponse>> unfreeze(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ApiResponse.ok("Account unfrozen",
                accountService.unfreezeAccount(accountId)));
    }

    @PostMapping("/{accountId}/close")
    public ResponseEntity<ApiResponse<AccountResponse>> close(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ApiResponse.ok("Account closed",
                accountService.closeAccount(accountId)));
    }
}
