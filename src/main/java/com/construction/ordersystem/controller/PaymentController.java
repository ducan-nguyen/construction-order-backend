package com.construction.ordersystem.controller;

import com.construction.ordersystem.dto.PaymentRequestDTO;
import com.construction.ordersystem.dto.PaymentResponseDTO;
import com.construction.ordersystem.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
    
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponseDTO> confirmPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.confirmPayment(id));
    }
}