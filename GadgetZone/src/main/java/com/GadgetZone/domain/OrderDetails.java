package com.GadgetZone.domain;

import java.math.BigDecimal;

public class OrderDetails {
    private Long id;
    private Long orderId;
    private Long productId;
    private BigDecimal amount;
    private BigDecimal price;

 
    private Product product;

    public OrderDetails() {}

    public OrderDetails(Long orderId, Long productId, BigDecimal amount, BigDecimal price) {
        this.orderId = orderId;
        this.productId = productId;
        this.amount = amount;
        this.price = price;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
