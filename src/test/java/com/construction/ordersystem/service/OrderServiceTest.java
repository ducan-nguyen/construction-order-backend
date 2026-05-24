package com.construction.ordersystem.service;

import com.construction.ordersystem.entity.*;
import com.construction.ordersystem.exception.BusinessException;
import com.construction.ordersystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService — Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository              orderRepository;
    @Mock private ProductService               productService;
    @Mock private CustomerService              customerService;
    @Mock private OrderStatusHistoryRepository statusHistoryRepository;
    @Mock private PaymentRepository            paymentRepository;

    @InjectMocks
    private OrderService orderService;

    private User owner;
    private User otherUser;
    private Order paidOrder;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("user@test.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@test.com");

        paidOrder = new Order();
        paidOrder.setId(10L);
        paidOrder.setOrderCode("ORD-TEST01");
        paidOrder.setUser(owner);
        paidOrder.setStatus(Order.OrderStatus.PAID);
        paidOrder.setTotalAmount(new BigDecimal("1000000"));
        paidOrder.setOrderItems(new ArrayList<>());
        paidOrder.setDeliverySchedules(new ArrayList<>());
        paidOrder.setStatusHistories(new ArrayList<>());
    }

    // ══════════════════════════════════════════════
    // requestRefund
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("requestRefund: thành công khi đơn PAID và đúng chủ")
    void requestRefund_success() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.requestRefund(10L, "Đặt nhầm sản phẩm", "user@test.com");

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.REFUND_REQUESTED);
        assertThat(result.getNotes()).contains("Đặt nhầm sản phẩm");
        verify(statusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("requestRefund: thất bại khi không phải chủ đơn")
    void requestRefund_fail_notOwner() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() ->
            orderService.requestRefund(10L, "Lý do", "other@test.com")
        )
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("không có quyền");
    }

    @Test
    @DisplayName("requestRefund: thất bại khi đơn chưa thanh toán (PENDING)")
    void requestRefund_fail_orderNotPaid() {
        paidOrder.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() ->
            orderService.requestRefund(10L, "Lý do", "user@test.com")
        )
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("đã thanh toán");
    }

    // ══════════════════════════════════════════════
    // approveRefund
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("approveRefund: thành công — đơn REFUNDED, payment REFUNDED")
    void approveRefund_success() {
        paidOrder.setStatus(Order.OrderStatus.REFUND_REQUESTED);
        Payment payment = new Payment();
        payment.setStatus(Payment.PaymentStatus.SUCCESS);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.findByOrder(paidOrder)).thenReturn(Optional.of(payment));

        Order result = orderService.approveRefund(10L, "admin@construction.com");

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.REFUNDED);
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("approveRefund: thất bại khi đơn không ở trạng thái REFUND_REQUESTED")
    void approveRefund_fail_wrongStatus() {
        // Đơn đang PAID, chưa có yêu cầu hoàn tiền
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() ->
            orderService.approveRefund(10L, "admin@construction.com")
        ).isInstanceOf(BusinessException.class);
    }

    // ══════════════════════════════════════════════
    // rejectRefund
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("rejectRefund: thành công — đơn quay lại PAID")
    void rejectRefund_success() {
        paidOrder.setStatus(Order.OrderStatus.REFUND_REQUESTED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.rejectRefund(10L, "admin@construction.com");

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PAID);
    }

    @Test
    @DisplayName("rejectRefund: thất bại khi đơn không ở trạng thái REFUND_REQUESTED")
    void rejectRefund_fail_wrongStatus() {
        paidOrder.setStatus(Order.OrderStatus.PROCESSING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() ->
            orderService.rejectRefund(10L, "admin@construction.com")
        ).isInstanceOf(BusinessException.class);
    }

    // ══════════════════════════════════════════════
    // cancelOrder
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("cancelOrder: thành công khi đơn PENDING")
    void cancelOrder_success_fromPending() {
        paidOrder.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancelOrder(10L);

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelOrder: thất bại khi đơn đang PROCESSING")
    void cancelOrder_fail_fromProcessing() {
        paidOrder.setStatus(Order.OrderStatus.PROCESSING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(10L))
            .isInstanceOf(BusinessException.class);
    }

    // ══════════════════════════════════════════════
    // updateOrderStatus — guard cho terminal states
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("updateOrderStatus: không cho cập nhật đơn đã COMPLETED")
    void updateOrderStatus_blocked_whenCompleted() {
        paidOrder.setStatus(Order.OrderStatus.COMPLETED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() ->
            orderService.updateOrderStatus(10L, Order.OrderStatus.PROCESSING, "admin")
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("updateOrderStatus: không cho cập nhật đơn đã REFUNDED")
    void updateOrderStatus_blocked_whenRefunded() {
        paidOrder.setStatus(Order.OrderStatus.REFUNDED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() ->
            orderService.updateOrderStatus(10L, Order.OrderStatus.PROCESSING, "admin")
        ).isInstanceOf(BusinessException.class);
    }
}
