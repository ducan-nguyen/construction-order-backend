package com.construction.ordersystem.service;

import com.construction.ordersystem.dto.OrderRequestDTO;
import com.construction.ordersystem.entity.*;
import com.construction.ordersystem.exception.BusinessException;
import com.construction.ordersystem.exception.InsufficientStockException;
import com.construction.ordersystem.exception.MinOrderQuantityException;
import com.construction.ordersystem.exception.ResourceNotFoundException;
import com.construction.ordersystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired private OrderRepository              orderRepository;
    @Autowired private ProductService               productService;
    @Autowired private CustomerService              customerService;
    @Autowired private OrderStatusHistoryRepository statusHistoryRepository;
    @Autowired private PaymentRepository            paymentRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    // ==========================
    // CORE ORDER OPERATIONS
    // ==========================

    @Transactional
    public Order createOrder(OrderRequestDTO request, User user) {
        // Resolve customer from authenticated user (create one if it doesn't exist yet)
        Customer customer = customerService.createOrGetCustomerForUser(user);
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomer(customer);
        order.setUser(user);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setRequireInvoice(request.getRequireInvoice());
        order.setNotes(request.getNotes());
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderRequestDTO.OrderItemRequest itemReq : request.getItems()) {
            Product product = productService.getProductById(itemReq.getProductId());

            if (itemReq.getQuantity() < product.getMinOrderQuantity()) {
                throw new MinOrderQuantityException(
                        "Sản phẩm " + product.getName() + " yêu cầu đặt tối thiểu " +
                        product.getMinOrderQuantity() + " " + product.getUnit()
                );
            }

            BigDecimal priceAtTime = productService.getProductPriceAtDate(
                    product.getId(), LocalDateTime.now().toLocalDate()
            );
            BigDecimal itemSubtotal = priceAtTime.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPriceAtTime(priceAtTime);
            orderItem.setSubtotal(itemSubtotal);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);

            // Pessimistic lock — gọi sau khi item đã được thêm vào order
            productService.checkAndUpdateStock(product.getId(), itemReq.getQuantity());
        }

        order.setSubtotal(subtotal);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(0, RoundingMode.HALF_UP);
        order.setTaxAmount(tax);
        order.setTotalAmount(subtotal.add(tax));

        Order savedOrder = orderRepository.save(order);
        saveStatusHistory(savedOrder, Order.OrderStatus.PENDING, user.getEmail());
        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus, String changedBy) {
        Order order = getOrderById(orderId);
        Order.OrderStatus current = order.getStatus();
        if (current == Order.OrderStatus.COMPLETED ||
            current == Order.OrderStatus.CANCELLED  ||
            current == Order.OrderStatus.REFUNDED) {
            throw new BusinessException("Không thể cập nhật đơn hàng có trạng thái: " + current);
        }
        order.setStatus(newStatus);
        saveStatusHistory(order, newStatus, changedBy);
        return orderRepository.save(order);
    }

    // ── Hoàn tiền ──────────────────────────────────

    @Transactional
    public Order requestRefund(Long orderId, String reason, String userEmail) {
        Order order = getOrderById(orderId);

        // Chỉ chủ đơn mới được yêu cầu
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("Bạn không có quyền thực hiện thao tác này");
        }

        // Chỉ đơn PAID mới được yêu cầu hoàn tiền
        if (order.getStatus() != Order.OrderStatus.PAID) {
            throw new BusinessException("Chỉ đơn hàng đã thanh toán mới có thể yêu cầu hoàn tiền");
        }

        // Ghi lý do vào notes
        String existingNotes = order.getNotes() != null ? order.getNotes() + "\n" : "";
        order.setNotes(existingNotes + "[Lý do hoàn tiền]: " + reason);
        order.setStatus(Order.OrderStatus.REFUND_REQUESTED);
        saveStatusHistory(order, Order.OrderStatus.REFUND_REQUESTED, userEmail);
        return orderRepository.save(order);
    }

    @Transactional
    public Order approveRefund(Long orderId, String adminEmail) {
        Order order = getOrderById(orderId);

        if (order.getStatus() != Order.OrderStatus.REFUND_REQUESTED) {
            throw new BusinessException("Đơn hàng không ở trạng thái yêu cầu hoàn tiền");
        }

        order.setStatus(Order.OrderStatus.REFUNDED);
        saveStatusHistory(order, Order.OrderStatus.REFUNDED, adminEmail);
        orderRepository.save(order);

        // Cập nhật trạng thái thanh toán → REFUNDED
        Optional<Payment> paymentOpt = paymentRepository.findByOrder(order);
        paymentOpt.ifPresent(payment -> {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        });

        return order;
    }

    @Transactional
    public Order rejectRefund(Long orderId, String adminEmail) {
        Order order = getOrderById(orderId);

        if (order.getStatus() != Order.OrderStatus.REFUND_REQUESTED) {
            throw new BusinessException("Đơn hàng không ở trạng thái yêu cầu hoàn tiền");
        }

        // Quay lại PAID
        order.setStatus(Order.OrderStatus.PAID);
        saveStatusHistory(order, Order.OrderStatus.PAID, adminEmail);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != Order.OrderStatus.PENDING &&
            order.getStatus() != Order.OrderStatus.PAID) {
            throw new BusinessException("Chỉ đơn hàng PENDING hoặc PAID mới có thể huỷ");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        saveStatusHistory(order, Order.OrderStatus.CANCELLED, "USER");
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + id));
    }

    public Page<Order> getOrdersByUser(User user, Pageable pageable) {
        return orderRepository.findByUser(user, pageable);
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    private void saveStatusHistory(Order order, Order.OrderStatus status, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        statusHistoryRepository.save(history);
    }

    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
