package com.construction.ordersystem.service;

import com.construction.ordersystem.dto.ChangePasswordRequestDTO;
import com.construction.ordersystem.dto.ProfileResponseDTO;
import com.construction.ordersystem.dto.RegisterRequest;
import com.construction.ordersystem.dto.UpdateProfileRequest;
import com.construction.ordersystem.entity.Customer;
import com.construction.ordersystem.entity.User;
import com.construction.ordersystem.exception.BusinessException;
import com.construction.ordersystem.exception.ResourceNotFoundException;
import com.construction.ordersystem.repository.CustomerRepository;
import com.construction.ordersystem.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email này đã được đăng ký", 400);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null && !request.getGender().isBlank()) {
            user.setGender(User.Gender.valueOf(request.getGender()));
        }
        user.setRole(User.Role.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // Tạo customer profile (thông tin công ty nếu có)
        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setCompanyName(request.getCompanyName());
        customer.setTaxCode(request.getTaxCode() != null && !request.getTaxCode().isBlank() ? request.getTaxCode() : null);
        customer.setPhone(request.getPhone());
        customer.setIsWholesale(request.getIsWholesale() != null ? request.getIsWholesale() : false);
        customerRepository.save(customer);

        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    public ProfileResponseDTO getProfileResponseDTO(User user) {
        Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);
        return buildDTO(user, customer);
    }

    private ProfileResponseDTO buildDTO(User user, Customer customer) {
        return ProfileResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .avatarUrl(user.getAvatarUrl())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .address(user.getAddress())
                .companyName(customer != null ? customer.getCompanyName() : null)
                .taxCode(customer != null ? customer.getTaxCode() : null)
                .isWholesale(customer != null ? customer.getIsWholesale() : null)
                .build();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public ProfileResponseDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(User.Gender.valueOf(request.getGender()));
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Cập nhật thông tin customer (công ty) — tạo mới nếu chưa có
        Customer customer = customerRepository.findByUserId(userId).orElse(null);
        if (customer == null) {
            customer = new Customer();
            customer.setUser(user);
            customer.setIsWholesale(false);
            entityManager.persist(customer);
            entityManager.flush();
        }
        if (request.getCompanyName() != null) customer.setCompanyName(request.getCompanyName());
        if (request.getTaxCode() != null) customer.setTaxCode(request.getTaxCode().isBlank() ? null : request.getTaxCode());
        if (request.getIsWholesale() != null) customer.setIsWholesale(request.getIsWholesale());
        entityManager.flush();
        return buildDTO(user, customer);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu cũ không đúng", 400);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}