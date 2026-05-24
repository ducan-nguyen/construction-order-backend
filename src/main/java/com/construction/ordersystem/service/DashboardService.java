package com.construction.ordersystem.service;

import com.construction.ordersystem.dto.DashboardStatsDTO;
import com.construction.ordersystem.entity.Order;
import com.construction.ordersystem.entity.User;
import com.construction.ordersystem.repository.OrderRepository;
import com.construction.ordersystem.repository.ProductRepository;
import com.construction.ordersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public DashboardStatsDTO getDashboardStats() {
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalCustomers = userRepository.countByRole(User.Role.USER);
        long lowStockProducts = productRepository.countByStockQuantityLessThan(10);

        // Doanh thu thực = đơn đã thanh toán (PAID, PROCESSING, SHIPPING, COMPLETED)
        List<Order.OrderStatus> paidStatuses = List.of(
                Order.OrderStatus.PAID,
                Order.OrderStatus.PROCESSING,
                Order.OrderStatus.SHIPPING,
                Order.OrderStatus.COMPLETED);
        BigDecimal totalRevenue = Optional.ofNullable(
                orderRepository.sumTotalAmountByStatuses(paidStatuses))
                .orElse(BigDecimal.ZERO);

        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long newOrders = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long completedOrders = orderRepository.countByStatus(Order.OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByStatus(Order.OrderStatus.CANCELLED);

        // Biểu đồ 6 tháng
        List<DashboardStatsDTO.RevenueChartItem> revenueChart = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            int year = monthStart.getYear();
            int month = monthStart.getMonthValue();
            BigDecimal revenue = orderRepository.sumTotalAmountByMonth(year, month);
            if (revenue == null) revenue = BigDecimal.ZERO;
            long ordersCount = orderRepository.countByMonth(year, month);
            revenueChart.add(DashboardStatsDTO.RevenueChartItem.builder()
                    .label(monthStart.getMonth().toString().substring(0, 3) + "/" + year)
                    .revenue(revenue)
                    .orders(ordersCount)
                    .build());
        }

        // Top 5 sản phẩm
        List<DashboardStatsDTO.TopProductItem> topProducts = new ArrayList<>();
        List<Object[]> topProductResults = orderRepository.findTopSellingProducts(PageRequest.of(0, 5));
        for (Object[] row : topProductResults) {
            topProducts.add(DashboardStatsDTO.TopProductItem.builder()
                    .productId(((Number) row[0]).longValue())
                    .productName((String) row[1])
                    .quantitySold(((Number) row[2]).longValue())
                    .revenue((BigDecimal) row[3])
                    .build());
        }

        // Trạng thái đơn hàng
        List<DashboardStatsDTO.OrderStatusItem> orderStatusStats = Arrays.stream(Order.OrderStatus.values())
                .map(status -> DashboardStatsDTO.OrderStatusItem.builder()
                        .status(status.name())
                        .count(orderRepository.countByStatus(status))
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsDTO.builder()
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .totalCustomers(totalCustomers)
                .lowStockProducts(lowStockProducts)
                .newOrders(newOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .revenueChart(revenueChart)
                .topProducts(topProducts)
                .orderStatusStats(orderStatusStats)
                .build();
    }
}