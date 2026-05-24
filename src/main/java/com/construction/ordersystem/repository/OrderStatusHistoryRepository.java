package com.construction.ordersystem.repository;

import com.construction.ordersystem.entity.Order;
import com.construction.ordersystem.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderOrderByChangedAtDesc(Order order);
}