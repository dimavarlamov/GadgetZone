package com.GadgetZone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.NEW;

    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    private String deliveryAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Transient
    private Date orderDateAsDate;

    public Date getOrderDateAsDate() {
        if (orderDate != null) {
            return Date.from(orderDate.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
}