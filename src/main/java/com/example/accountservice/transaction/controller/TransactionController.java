package com.example.accountservice.transaction.controller;

import com.example.accountservice.common.response.ApiResponse;
import com.example.accountservice.transaction.dto.response.TransactionResponse;
import com.example.accountservice.transaction.entity.TransactionType;
import com.example.accountservice.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @PathVariable UUID accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TransactionResponse> page;

        if (from != null && to != null) {
            page = transactionService.getTransactionsByDateRange(accountId, from, to, pageable);
        } else if (type != null) {
            page = transactionService.getTransactionsByType(accountId, type, pageable);
        } else {
            page = transactionService.getTransactions(accountId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @PathVariable UUID transactionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                transactionService.getTransactionById(transactionId)));
    }

    @GetMapping("/transactions/reference/{reference}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getByReference(
            @PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.ok(
                transactionService.getTransactionByReference(reference)));
    }
}
