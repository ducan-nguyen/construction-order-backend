package com.construction.ordersystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
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
    public static class OrderItemDTO {
        private Long id;
        private String productName;
        private Integer quantity;
        private String unit;
        private BigDecimal priceAtTime;
        private BigDecimal subtotal;
    }
}