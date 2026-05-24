package com.construction.ordersystem.repository;

import com.construction.ordersystem.entity.Payment;
import com.construction.ordersystem.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
}