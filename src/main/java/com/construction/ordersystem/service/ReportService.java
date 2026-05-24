package com.construction.ordersystem.service;

import com.construction.ordersystem.dto.MonthlyRevenueDTO;
import com.construction.ordersystem.entity.Order;
import com.construction.ordersystem.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    // Báo cáo doanh thu theo tháng trong một năm (12 tháng)
    public List<MonthlyRevenueDTO> getMonthlyRevenue(int year) {
        List<MonthlyRevenueDTO> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal revenue = orderRepository.sumTotalAmountByMonth(year, month);
            if (revenue == null) revenue = BigDecimal.ZERO;
            long orderCount = orderRepository.countByMonth(year, month);
            result.add(MonthlyRevenueDTO.builder()
                    .year(year)
                    .month(month)
                    .revenue(revenue)
                    .orderCount(orderCount)
                    .build());
        }
        return result;
    }

    // Báo cáo doanh thu theo ngày trong khoảng thời gian (từ -> đến)
    public Map<LocalDate, BigDecimal> getDailyRevenue(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.plusDays(1).atStartOfDay();
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);

        Map<LocalDate, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            dailyRevenue.put(date, BigDecimal.ZERO);
        }

        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.COMPLETED || order.getStatus() == Order.OrderStatus.PAID) {
                LocalDate orderDate = order.getOrderDate().toLocalDate();
                dailyRevenue.merge(orderDate, order.getTotalAmount(), BigDecimal::add);
            }
        }
        return dailyRevenue;
    }

    // Top khách hàng theo tổng chi tiêu
    public List<Object[]> getTopCustomers(int limit) {
        return orderRepository.findTopCustomersByTotalSpent(PageRequest.of(0, limit)).getContent();
    }

}