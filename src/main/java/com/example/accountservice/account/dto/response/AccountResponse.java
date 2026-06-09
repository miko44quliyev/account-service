package com.example.accountservice.account.dto.response;

import com.example.accountservice.account.entity.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AccountResponse {

    private UUID id;

    private String accountNumber;

    private BigDecimal balance;

    private AccountStatus status;

    private UUID userId;

    private String ownerFirstName;

    private String ownerLastName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
