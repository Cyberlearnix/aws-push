package com.orderservice.controller;

import com.orderservice.entity.Order;
import com.orderservice.entity.OrderStatus;
import com.orderservice.dto.*;
import com.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Order Service",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 1. Create Order
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order createdOrder = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 2. Get Order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Alternative: Get Order by Order Number
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        try {
            Order order = orderService.getOrderByNumber(orderNumber);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 3. Get Orders by User
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUser(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get Orders by User (Paginated)
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<Order>> getOrdersByUserPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy) {
        try {
            Page<Order> orders = orderService.getOrdersByUser(userId, page, size, sortBy);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 4. Update Order Status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus(), request.getNotes());
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 5. Cancel Order
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId) {
        try {
            boolean cancelled = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of(
                "success", cancelled,
                "message", "Order cancelled successfully",
                "orderId", orderId,
                "timestamp", LocalDateTime.now()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage(),
                "orderId", orderId,
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    // 6. Get All Orders (Admin)
    @GetMapping("/all")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy) {
        try {
            Page<Order> orders = orderService.getAllOrders(page, size, sortBy);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 7. Refund Order (Admin/Support)
    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Order> refundOrder(
            @PathVariable Long orderId,
            @RequestBody RefundOrderRequest request) {
        try {
            Order refundedOrder = orderService.processRefund(orderId, request.getRefundAmount(), request.getRefundReason());
            return ResponseEntity.ok(refundedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Additional helper endpoints

    // Get Orders by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get Orders by Status (Paginated)
    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<Page<Order>> getOrdersByStatusPaginated(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Order> orders = orderService.getOrdersByStatus(status, page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get user order statistics
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserOrderStats(@PathVariable Long userId) {
        try {
            long totalOrders = orderService.getOrderCountByUserAndStatus(userId, OrderStatus.COMPLETED);
            BigDecimal totalSpent = orderService.getTotalSpentByUser(userId);
            long pendingOrders = orderService.getOrderCountByUserAndStatus(userId, OrderStatus.PENDING);
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "totalOrders", totalOrders,
                "totalSpent", totalSpent,
                "pendingOrders", pendingOrders,
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Exception handling
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error", "Internal server error",
            "message", e.getMessage(),
            "timestamp", LocalDateTime.now()
        ));
    }

    // DTO Classes for request bodies
    public static class UpdateOrderStatusRequest {
        private OrderStatus status;
        private String notes;

        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class RefundOrderRequest {
        private BigDecimal refundAmount;
        private String refundReason;

        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

        public String getRefundReason() { return refundReason; }
        public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    }
}