package com.construction.ordersystem.controller;

import com.construction.ordersystem.dto.MonthlyRevenueDTO;
import com.construction.ordersystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Báo cáo doanh thu theo tháng (tất cả 12 tháng trong năm)
     */
    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<MonthlyRevenueDTO>> getMonthlyRevenue(@RequestParam int year) {
        return ResponseEntity.ok(reportService.getMonthlyRevenue(year));
    }

    /**
     * Báo cáo doanh thu theo ngày trong khoảng thời gian
     */
    @GetMapping("/daily-revenue")
    public ResponseEntity<Map<LocalDate, java.math.BigDecimal>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getDailyRevenue(from, to));
    }

    /**
     * Top khách hàng chi tiêu nhiều nhất
     */
    @GetMapping("/top-customers")
    public ResponseEntity<List<Object[]>> getTopCustomers(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reportService.getTopCustomers(limit));
    }

}