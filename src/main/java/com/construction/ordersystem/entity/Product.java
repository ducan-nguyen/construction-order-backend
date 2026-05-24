package com.construction.ordersystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    private Unit unit;
    
    private BigDecimal price;

    /** Giá khuyến mãi — null = không có sale */
    @Column(name = "discount_price")
    private BigDecimal discountPrice;

    private Integer stockQuantity;
    
    private Integer minOrderQuantity = 1;
    
    private String imageUrl;
    
    private boolean active = true;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum Category {
        CEMENT, STEEL, BRICK, SAND, STONE, PAINT, TILE, PIPE, ELECTRICAL, OTHER
    }
    
    public enum Unit {
        KG, TON, M2, BOX, PIECE, LITER, BAG
    }
}