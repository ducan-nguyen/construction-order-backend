package com.construction.ordersystem.service;

import com.construction.ordersystem.dto.ChangePasswordRequestDTO;
import com.construction.ordersystem.dto.RegisterRequest;
import com.construction.ordersystem.entity.Customer;
import com.construction.ordersystem.entity.User;
import com.construction.ordersystem.exception.BusinessException;
import com.construction.ordersystem.repository.CustomerRepository;
import com.construction.ordersystem.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Unit Tests")
class UserServiceTest {

    @Mock private UserRepository     userRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private PasswordEncoder    passwordEncoder;
    @Mock private EntityManager      entityManager;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private ChangePasswordRequestDTO changePwRequest;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("user@test.com");
        existingUser.setPassword("$2a$12$hashedPassword");
        existingUser.setFullName("Test User");
        existingUser.setRole(User.Role.USER);
        existingUser.setEnabled(true);

        changePwRequest = new ChangePasswordRequestDTO();
        changePwRequest.setOldPassword("oldPass123");
        changePwRequest.setNewPassword("newPass456");
        changePwRequest.setConfirmPassword("newPass456");
    }

    // ══════════════════════════════════════════════
    // changePassword
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("changePassword: thành công khi mật khẩu cũ đúng")
    void changePassword_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPass123", existingUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass456")).thenReturn("$2a$12$newHashedPassword");

        assertThatNoException().isThrownBy(() ->
            userService.changePassword(1L, changePwRequest)
        );

        verify(passwordEncoder).encode("newPass456");
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getPassword()).isEqualTo("$2a$12$newHashedPassword");
    }

    @Test
    @DisplayName("changePassword: thất bại khi mật khẩu cũ sai")
    void changePassword_fail_wrongOldPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPass123", existingUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() ->
            userService.changePassword(1L, changePwRequest)
        )
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Mật khẩu cũ không đúng");

        // Không được encode hay lưu gì khi password sai
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════
    // registerUser
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("registerUser: thành công và tự động tạo Customer profile")
    void registerUser_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setPhone("0900000000");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$encodedNew");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.registerUser(request);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
        assertThat(result.isEnabled()).isTrue();
        // Phải tạo Customer profile đi kèm
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("registerUser: thất bại khi email đã tồn tại")
    void registerUser_fail_duplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");
        request.setFullName("Duplicate");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() ->
            userService.registerUser(request)
        )
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("đã được đăng ký");

        // Không được lưu user khi email trùng
        verify(userRepository, never()).save(any());
    }
}
