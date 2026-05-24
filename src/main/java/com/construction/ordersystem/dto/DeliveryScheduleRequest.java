package com.construction.ordersystem.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class DeliveryScheduleRequest {
    @NotNull @Future
    private LocalDate deliveryDate;
    
    @NotNull @Positive
    private Integer quantity;
    
    private String notes;
}