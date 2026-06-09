package com.example.accountservice.transaction.mapper;

import com.example.accountservice.transaction.dto.response.TransactionResponse;
import com.example.accountservice.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "accountId", source = "account.id")
    TransactionResponse toResponse(Transaction transaction);
}
