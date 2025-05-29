package com.GadgetZone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    @Column
    private Integer quantity;

    // Конструктор для Cart
    public CartItem(Product product, int quantity) {
        this.productId = product.getId();
        this.quantity = quantity;
    }

    @Transient // Это поле не сохраняется в БД, а заполняется в сервисах
    private Product product;
}