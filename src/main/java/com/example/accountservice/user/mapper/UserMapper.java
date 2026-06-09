package com.example.accountservice.user.mapper;

import com.example.accountservice.user.dto.request.CreateUserRequest;
import com.example.accountservice.user.dto.request.UpdateUserRequest;
import com.example.accountservice.user.dto.response.UserResponse;
import com.example.accountservice.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "accountCount", expression = "java(user.getAccounts() != null ? user.getAccounts().size() : 0)")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUser(UpdateUserRequest request, @MappingTarget User user);
}
