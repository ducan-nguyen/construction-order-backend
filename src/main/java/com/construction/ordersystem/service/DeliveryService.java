// service/DeliveryService.java
package com.construction.ordersystem.service;

import com.construction.ordersystem.dto.DeliveryScheduleRequest;
import com.construction.ordersystem.entity.DeliverySchedule;
import com.construction.ordersystem.entity.Order;
import com.construction.ordersystem.repository.DeliveryScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {
    
    @Autowired
    private DeliveryScheduleRepository deliveryRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Transactional
    public DeliverySchedule addDeliverySchedule(Long orderId, DeliveryScheduleRequest request) {
        Order order = orderService.getOrderById(orderId);
        
        DeliverySchedule schedule = new DeliverySchedule();
        schedule.setOrder(order);
        schedule.setDeliveryDate(request.getDeliveryDate());
        schedule.setQuantity(request.getQuantity());
        schedule.setNotes(request.getNotes());
        schedule.setStatus(DeliverySchedule.DeliveryStatus.PENDING);
        
        return deliveryRepository.save(schedule);
    }
    
    @Transactional
    public DeliverySchedule updateDeliveryStatus(Long deliveryId, DeliverySchedule.DeliveryStatus status) {
        DeliverySchedule schedule = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Delivery schedule not found"));
        schedule.setStatus(status);
        return deliveryRepository.save(schedule);
    }
}