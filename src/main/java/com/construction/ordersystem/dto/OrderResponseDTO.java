package com.construction.ordersystem.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String orderCode;
    private LocalDateTime orderDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String status;
    private String deliveryAddress;
    private Boolean requireInvoice;
    private String notes;
    private List<OrderItemDTO> orderItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long id;
        private String productName;
        private Integer quantity;
        private String unit;
        private BigDecimal priceAtTime;
        private BigDecimal subtotal;
    }
}