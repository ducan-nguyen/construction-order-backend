package com.construction.ordersystem.repository;

import com.construction.ordersystem.entity.Product;
import com.construction.ordersystem.entity.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Optional;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long> {
    @Query("SELECT pph FROM ProductPriceHistory pph WHERE pph.product = :product AND pph.effectiveDate <= :date ORDER BY pph.effectiveDate DESC")
    Optional<ProductPriceHistory> findLatestPriceBeforeDate(Product product, LocalDate date);
}