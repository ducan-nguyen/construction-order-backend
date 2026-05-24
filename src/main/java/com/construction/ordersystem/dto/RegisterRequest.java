package com.construction.ordersystem.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6)
    private String password;

    @NotBlank
    private String fullName;

    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;

    // Customer info (optional)
    private String companyName;
    private String taxCode;
    private Boolean isWholesale;
}