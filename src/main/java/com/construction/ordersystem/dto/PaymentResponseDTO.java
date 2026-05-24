package com.construction.ordersystem.dto;

import lombok.Builder;
import lombok.Data;
import com.construction.ordersystem.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDTO {
    private Long id;
    private Long orderId;
    private String orderCode;
    private Payment.PaymentMethod method;
    private Payment.PaymentStatus status;
    private BigDecimal amount;
    private String transactionCode;
    private LocalDateTime paymentDate;
    private String qrCodeUrl;
    private String momoQRUrl;
    private String notes;
}