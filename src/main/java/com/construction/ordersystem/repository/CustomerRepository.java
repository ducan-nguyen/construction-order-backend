package com.construction.ordersystem.repository;

import com.construction.ordersystem.entity.Customer;
import com.construction.ordersystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByTaxCode(String taxCode);
}