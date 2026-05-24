package com.construction.ordersystem.repository;

import com.construction.ordersystem.entity.Order;
import com.construction.ordersystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser(User user, Pageable pageable);
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(Order.OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN :statuses")
    BigDecimal sumTotalAmountByStatuses(@Param("statuses") List<Order.OrderStatus> statuses);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status <> :excludedStatus")
    BigDecimal sumTotalAmountExcludingStatus(@Param("excludedStatus") Order.OrderStatus excludedStatus);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month AND o.status IN ('PAID','PROCESSING','SHIPPING','COMPLETED')")
    BigDecimal sumTotalAmountByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(o) FROM Order o WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month AND o.status IN ('PAID','PROCESSING','SHIPPING','COMPLETED')")
    long countByMonth(@Param("year") int year, @Param("month") int month);

    /** Lấy doanh thu + số đơn gộp theo tháng — chỉ tính đơn đã thanh toán */
    @Query("SELECT YEAR(o.orderDate), MONTH(o.orderDate), SUM(o.totalAmount), COUNT(o) " +
           "FROM Order o " +
           "WHERE o.orderDate >= :startDate AND o.status IN ('PAID','PROCESSING','SHIPPING','COMPLETED') " +
           "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
           "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<Object[]> findMonthlyRevenueFrom(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE DATE(o.orderDate) = :date")
    BigDecimal sumTotalAmountByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT oi.product.id, p.name, SUM(oi.quantity), SUM(oi.subtotal) " +
           "FROM OrderItem oi JOIN oi.product p " +
           "GROUP BY oi.product.id, p.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query(value = "SELECT u.id, u.email, u.full_name, SUM(o.total_amount) as total_spent " +
                   "FROM users u JOIN orders o ON u.id = o.user_id " +
                   "WHERE o.status = 'COMPLETED' " +
                   "GROUP BY u.id, u.email, u.full_name " +
                   "ORDER BY total_spent DESC",
           countQuery = "SELECT COUNT(DISTINCT u.id) FROM users u JOIN orders o ON u.id = o.user_id WHERE o.status = 'COMPLETED'",
           nativeQuery = true)
    Page<Object[]> findTopCustomersByTotalSpent(Pageable pageable);
}
