package com.construction.ordersystem.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String companyName;
    private String taxCode;
    private Boolean isWholesale;
}