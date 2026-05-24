package com.construction.ordersystem.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String companyName;
    private String taxCode;
    private Boolean isWholesale;
}