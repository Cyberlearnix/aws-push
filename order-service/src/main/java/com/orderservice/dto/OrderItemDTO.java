package com.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class OrderItemDTO {
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    private String courseTitle;
    
    @NotNull(message = "Course price is required")
    @Positive(message = "Course price must be positive")
    private BigDecimal coursePrice;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;
    
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    // Getters and setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    
    public BigDecimal getCoursePrice() { return coursePrice; }
    public void setCoursePrice(BigDecimal coursePrice) { this.coursePrice = coursePrice; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}