package com.orderservice.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "course_title")
    private String courseTitle;
    
    @Column(name = "course_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal coursePrice;
    
    @Column(name = "quantity")
    private Integer quantity = 1;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;
    
    // Constructors
    public OrderItem() {}
    
    public OrderItem(Long courseId, String courseTitle, BigDecimal coursePrice, Integer quantity) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.coursePrice = coursePrice;
        this.quantity = quantity;
        this.totalPrice = coursePrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Calculate total price based on course price and quantity minus discount
    public void calculateTotalPrice() {
        BigDecimal subtotal = coursePrice.multiply(BigDecimal.valueOf(quantity));
        this.totalPrice = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    
    public BigDecimal getCoursePrice() { return coursePrice; }
    public void setCoursePrice(BigDecimal coursePrice) { 
        this.coursePrice = coursePrice;
        calculateTotalPrice();
    }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        calculateTotalPrice();
    }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { 
        this.discountAmount = discountAmount;
        calculateTotalPrice();
    }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}