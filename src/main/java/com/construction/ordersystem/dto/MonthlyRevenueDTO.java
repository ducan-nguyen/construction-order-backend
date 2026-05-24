package com.construction.ordersystem.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MonthlyRevenueDTO {
    private int year;
    private int month;
    private BigDecimal revenue;
    private Long orderCount;
}