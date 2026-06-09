package com.example.accountservice.transaction.dto.response;

import com.example.accountservice.transaction.entity.TransactionStatus;
import com.example.accountservice.transaction.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {

    private UUID id;

    private String reference;

    private TransactionType type;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String description;

    private TransactionStatus status;

    private UUID accountId;

    private LocalDateTime createdAt;
}
