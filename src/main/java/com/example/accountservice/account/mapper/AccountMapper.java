package com.example.accountservice.account.mapper;

import com.example.accountservice.account.dto.response.AccountResponse;
import com.example.accountservice.account.dto.response.AccountSummary;
import com.example.accountservice.account.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "userId", source = "owner.id")
    @Mapping(target = "ownerFirstName", source = "owner.firstName")
    @Mapping(target = "ownerLastName", source = "owner.lastName")
    AccountResponse toResponse(Account account);

    AccountSummary toSummary(Account account);
}
