package com.construction.ordersystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private BigDecimal amount;
    
    private String transactionCode;
    
    private LocalDateTime paymentDate;
    
    private String notes;
    
    public enum PaymentMethod {
        COD,           // Thanh toán khi nhận hàng
        BANK_TRANSFER, // Chuyển khoản ngân hàng
        MOMO,          // Ví Momo
        CREDIT_CARD,   // Thẻ tín dụng
        INSTALLMENT    // Trả góp
    }
    
    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
    }
}