package com.construction.ordersystem.service;

import com.construction.ordersystem.dto.PaymentRequestDTO;
import com.construction.ordersystem.dto.PaymentResponseDTO;
import com.construction.ordersystem.entity.Order;
import com.construction.ordersystem.entity.Payment;
import com.construction.ordersystem.exception.BusinessException;
import com.construction.ordersystem.exception.ResourceNotFoundException;
import com.construction.ordersystem.repository.OrderRepository;
import com.construction.ordersystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderRepository   orderRepository;

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + request.getOrderId()));

        if (order.getStatus() == Order.OrderStatus.PAID) {
            throw new BusinessException("Đơn hàng đã được thanh toán");
        }
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessException("Đơn hàng đã bị huỷ, không thể thanh toán");
        }

        Payment payment = paymentRepository.findByOrder(order).orElseGet(() -> {
            Payment p = new Payment();
            p.setOrder(order);
            p.setAmount(order.getTotalAmount());
            return p;
        });

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new BusinessException("Thanh toán đã hoàn thành cho đơn hàng này");
        }

        payment.setMethod(request.getMethod());
        payment.setTransactionCode(generateTransactionCode());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setNotes(request.getNotes());

        boolean isImmediatePayment = false;
        switch (request.getMethod()) {
            case MOMO, CREDIT_CARD -> {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                isImmediatePayment = true;
            }
            default -> payment.setStatus(Payment.PaymentStatus.PENDING);
        }

        Payment savedPayment = paymentRepository.save(payment);

        // Cập nhật order status trong cùng transaction
        if (isImmediatePayment) {
            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);
        }

        return convertToDTO(savedPayment, order);
    }

    @Transactional
    public PaymentResponseDTO confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thanh toán với id: " + paymentId));

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new BusinessException("Thanh toán đã được xác nhận trước đó");
        }

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now());

        Order order = payment.getOrder();
        order.setStatus(Order.OrderStatus.PAID);

        // Lưu cả 2 trong cùng 1 transaction
        paymentRepository.save(payment);
        orderRepository.save(order);

        return convertToDTO(payment, order);
    }

    private String generateTransactionCode() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentResponseDTO convertToDTO(Payment payment, Order order) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .transactionCode(payment.getTransactionCode())
                .paymentDate(payment.getPaymentDate())
                .qrCodeUrl(generateQRCode(payment))
                .momoQRUrl(generateMomoQR(payment))
                .notes(payment.getNotes())
                .build();
    }

    private String generateQRCode(Payment payment) {
        if (payment.getMethod() == Payment.PaymentMethod.BANK_TRANSFER) {
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=Bank:VCB|Account:1234567890|Amount:"
                    + payment.getAmount() + "|Desc:" + payment.getTransactionCode();
        }
        return null;
    }

    private String generateMomoQR(Payment payment) {
        if (payment.getMethod() == Payment.PaymentMethod.MOMO) {
            return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=MOMO|"
                    + payment.getTransactionCode() + "|Amount:" + payment.getAmount();
        }
        return null;
    }
}
