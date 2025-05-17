package com.GadgetZone.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private Long id;  // Идентификатор заказа
    private Long userId;  // Идентификатор пользователя
    private BigDecimal sum;  // Сумма заказа
    private String address;  // Адрес доставки
    private LocalDateTime created;  // Дата и время создания заказа
    private LocalDateTime updated;  // Дата и время последнего обновления
    private List<OrderDetails> details;  // Список товаров в заказе
    private String status;  // Статус заказа (например, "PENDING", "COMPLETED")

    // Конструктор без параметров (по необходимости)
    public Order() {}

    // Конструктор с параметрами
    public Order(Long userId, BigDecimal sum, String address, LocalDateTime created, LocalDateTime updated, String status) {
        this.userId = userId;
        this.sum = sum;
        this.address = address;
        this.created = created;
        this.updated = updated;
        this.status = status;
    }

    // Геттеры и сеттеры для всех полей

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public List<OrderDetails> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetails> details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
