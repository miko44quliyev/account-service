package com.example.accountservice.user;

import com.example.accountservice.common.exception.ConflictException;
import com.example.accountservice.common.exception.ResourceNotFoundException;
import com.example.accountservice.user.dto.request.CreateUserRequest;
import com.example.accountservice.user.dto.request.UpdateUserRequest;
import com.example.accountservice.user.dto.response.UserResponse;
import com.example.accountservice.user.entity.User;
import com.example.accountservice.user.mapper.UserMapper;
import com.example.accountservice.user.repository.UserRepository;
import com.example.accountservice.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @InjectMocks UserService userService;

    private CreateUserRequest createRequest;
    private User user;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        createRequest = new CreateUserRequest();
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Doe");
        createRequest.setEmail("jane@example.com");
        createRequest.setPhone("+1234567890");

        user = User.builder()
                .id(userId)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .phone("+1234567890")
                .build();

        userResponse = UserResponse.builder()
                .id(userId)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .build();
    }

    @Test
    @DisplayName("createUser - success")
    void createUser_success() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(createRequest);

        assertThat(result).isEqualTo(userResponse);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("createUser - duplicate email throws ConflictException")
    void createUser_duplicateEmail_throws() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("getUserById - success")
    void getUserById_success() {
        when(userRepository.findByIdWithAccounts(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("getUserById - not found throws ResourceNotFoundException")
    void getUserById_notFound() {
        when(userRepository.findByIdWithAccounts(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateUser - success")
    void updateUser_success() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Updated");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser(userId, req);
        assertThat(result).isNotNull();
        verify(userMapper).updateUser(req, user);
    }

    @Test
    @DisplayName("updateUser - email conflict throws")
    void updateUser_emailConflict() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("other@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("deleteUser - success")
    void deleteUser_success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        userService.deleteUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("deleteUser - not found throws")
    void deleteUser_notFound() {
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
