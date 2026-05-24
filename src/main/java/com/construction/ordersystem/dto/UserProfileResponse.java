package com.construction.ordersystem.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String gender;
    private String companyName;
    private String taxCode;
    private String role;
}