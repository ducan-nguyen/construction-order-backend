package com.construction.ordersystem.repository;

import com.construction.ordersystem.entity.DeliverySchedule;
import com.construction.ordersystem.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryScheduleRepository extends JpaRepository<DeliverySchedule, Long> {
    List<DeliverySchedule> findByOrder(Order order);
    List<DeliverySchedule> findByStatus(DeliverySchedule.DeliveryStatus status);
}