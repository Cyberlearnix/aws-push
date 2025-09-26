package com.orderservice.service;

import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.entity.OrderStatus;
import com.orderservice.repository.OrderRepository;
import com.orderservice.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(CreateOrderRequest request) {
        // Generate unique order number
        String orderNumber = generateOrderNumber();
        
        // Calculate total amount from items
        BigDecimal totalAmount = request.getItems().stream()
            .map(item -> {
                BigDecimal itemTotal = item.getCoursePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                return itemTotal.subtract(item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(request.getUserId());
        order.setUserEmail(request.getUserEmail());
        order.setTotalAmount(totalAmount);
        order.setCurrency(request.getCurrency());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(request.getNotes());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Add order items
        for (OrderItemDTO itemDTO : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setCourseId(itemDTO.getCourseId());
            orderItem.setCourseTitle(itemDTO.getCourseTitle());
            orderItem.setCoursePrice(itemDTO.getCoursePrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setDiscountAmount(itemDTO.getDiscountAmount());
            
            BigDecimal itemTotal = itemDTO.getCoursePrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            orderItem.setTotalPrice(itemTotal.subtract(itemDTO.getDiscountAmount() != null ? itemDTO.getDiscountAmount() : BigDecimal.ZERO));
            
            order.addOrderItem(orderItem);
        }

        return orderRepository.save(order);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Page<Order> getOrdersByUser(Long userId, int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepository.findByUserId(userId, pageable);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String notes) {
        Order order = getOrderById(orderId);
        
        // Validate status transition
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }
        
        order.setStatus(newStatus);
        if (notes != null && !notes.trim().isEmpty()) {
            order.setNotes(notes);
        }
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    public boolean cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be cancelled. Current status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        return true;
    }

    public Page<Order> getAllOrders(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepository.findAllOrderByCreatedAtDesc(pageable);
    }

    public Order processRefund(Long orderId, BigDecimal refundAmount, String refundReason) {
        Order order = getOrderById(orderId);
        
        if (!order.canBeRefunded()) {
            throw new RuntimeException("Order cannot be refunded. Current status: " + order.getStatus());
        }
        
        if (refundAmount.compareTo(order.getTotalAmount().subtract(order.getRefundAmount())) > 0) {
            throw new RuntimeException("Refund amount exceeds remaining refundable amount");
        }
        
        order.setRefundAmount(order.getRefundAmount().add(refundAmount));
        order.setRefundReason(refundReason);
        order.setRefundedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Update status to REFUNDED if fully refunded
        if (order.getRefundAmount().compareTo(order.getTotalAmount()) >= 0) {
            order.setStatus(OrderStatus.REFUNDED);
        }
        
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Page<Order> getOrdersByStatus(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByStatus(status, pageable);
    }

    public long getOrderCountByUserAndStatus(Long userId, OrderStatus status) {
        return orderRepository.countByUserIdAndStatus(userId, status);
    }

    public BigDecimal getTotalSpentByUser(Long userId) {
        Double total = orderRepository.sumTotalAmountByUserIdAndStatus(userId, OrderStatus.COMPLETED);
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
            case PROCESSING:
                return newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
            case PAID:
                return newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED;
            case COMPLETED:
                return newStatus == OrderStatus.REFUNDED;
            case CANCELLED:
            case REFUNDED:
                return false; // Terminal states
            default:
                return false;
        }
    }
}