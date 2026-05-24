package com.construction.ordersystem.dto;

import lombok.Data;
import com.construction.ordersystem.entity.Payment;
import jakarta.validation.constraints.NotNull;

@Data
public class PaymentRequestDTO {
    @NotNull
    private Long orderId;
    
    @NotNull
    private Payment.PaymentMethod method;
    
    private String notes;
}