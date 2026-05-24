package com.construction.ordersystem.controller;

import com.construction.ordersystem.dto.DeliveryScheduleRequest;
import com.construction.ordersystem.entity.DeliverySchedule;
import com.construction.ordersystem.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<DeliverySchedule> addDeliverySchedule(
            @PathVariable Long orderId,
            @Valid @RequestBody DeliveryScheduleRequest request) {
        return ResponseEntity.ok(deliveryService.addDeliverySchedule(orderId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<DeliverySchedule> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @RequestParam DeliverySchedule.DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(deliveryId, status));
    }
}
