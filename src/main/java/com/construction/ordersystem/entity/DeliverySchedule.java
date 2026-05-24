package com.construction.ordersystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "delivery_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    private LocalDate deliveryDate;
    
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.PENDING;
    
    private String notes;
    
    public enum DeliveryStatus {
        PENDING, IN_TRANSIT, DELIVERED
    }
}