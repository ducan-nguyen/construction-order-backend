package com.construction.ordersystem.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class OrderRequestDTO {
    // customerId is resolved server-side from the authenticated user
    private Long customerId;

    @NotBlank
    private String deliveryAddress;
    
    private Boolean requireInvoice = false;
    
    private String notes;
    
    @NotEmpty
    private List<OrderItemRequest> items;
    
    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long productId;
        
        @NotNull @Positive
        private Integer quantity;
    }
}