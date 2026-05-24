package com.construction.ordersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {

    // ===== 1. KPI tổng quan =====
    private Long totalOrders;
    private Long totalUsers;
    private Long totalProducts;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Long totalCustomers;
    private Long lowStockProducts;

    private Long newOrders;        // đơn mới
    private Long completedOrders;  // đơn hoàn thành
    private Long cancelledOrders;  // đơn hủy

    // ===== 2. Biểu đồ doanh thu =====
    private List<RevenueChartItem> revenueChart;

    // ===== 3. Top sản phẩm bán chạy =====
    private List<TopProductItem> topProducts;

    // ===== 4. Trạng thái đơn hàng =====
    private List<OrderStatusItem> orderStatusStats;


    // ================= INNER CLASSES =================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueChartItem {
        private String label;        // ví dụ: "Jan", "Feb" or "2026-04"
        private BigDecimal revenue;  // doanh thu
        private Long orders;         // số đơn
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductItem {
        private Long productId;
        private String productName;
        private Long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusItem {
        private String status; // PENDING, COMPLETED, CANCELLED...
        private Long count;
    }
}