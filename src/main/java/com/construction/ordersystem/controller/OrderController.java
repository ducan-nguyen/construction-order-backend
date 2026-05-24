package com.construction.ordersystem.controller;

import com.construction.ordersystem.dto.OrderRequestDTO;
import com.construction.ordersystem.dto.OrderResponseDTO;
import com.construction.ordersystem.entity.Order;
import com.construction.ordersystem.entity.User;
import com.construction.ordersystem.service.OrderService;
import com.construction.ordersystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Quản lý đơn hàng — tạo, xem, huỷ, hoàn tiền")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());
        Order order = orderService.createOrder(request, user);
        return ResponseEntity.ok(convertToDTO(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(convertToDTO(order));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userService.findByEmail(userDetails.getUsername());
        Page<Order> orders = orderService.getOrdersByUser(user, PageRequest.of(page, size));
        Page<OrderResponseDTO> response = orders.map(this::convertToDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderService.getAllOrders(PageRequest.of(page, size));
        return ResponseEntity.ok(orders.map(this::convertToDTO));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        // Gọi cancelOrder thay vì updateOrderStatus
        Order order = orderService.cancelOrder(id);
        return ResponseEntity.ok(convertToDTO(order));
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.updateOrderStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(convertToDTO(order));
    }

    // ── Hoàn tiền ──────────────────────────────────────────────────────

    @Operation(summary = "Yêu cầu hoàn tiền", description = "Khách hàng gửi yêu cầu hoàn tiền cho đơn đã thanh toán (PAID)")
    @PostMapping("/{id}/request-refund")
    public ResponseEntity<OrderResponseDTO> requestRefund(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String reason = body.getOrDefault("reason", "Không có lý do");
        Order order = orderService.requestRefund(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(convertToDTO(order));
    }

    @PutMapping("/admin/{id}/approve-refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> approveRefund(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.approveRefund(id, userDetails.getUsername());
        return ResponseEntity.ok(convertToDTO(order));
    }

    @PutMapping("/admin/{id}/reject-refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> rejectRefund(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.rejectRefund(id, userDetails.getUsername());
        return ResponseEntity.ok(convertToDTO(order));
    }

    // Helper method
    private OrderResponseDTO convertToDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderDate(order.getOrderDate())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .deliveryAddress(order.getDeliveryAddress())
                .requireInvoice(order.getRequireInvoice())
                .notes(order.getNotes())
                .orderItems(order.getOrderItems().stream()
                        .map(item -> OrderResponseDTO.OrderItemDTO.builder()
                                .id(item.getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .unit(item.getProduct().getUnit().toString())
                                .priceAtTime(item.getPriceAtTime())
                                .subtotal(item.getSubtotal())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}