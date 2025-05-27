package com.GadgetZone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetails {
    private Long id;
    private Long orderId;
    private Long productId;
    private BigDecimal amount;
    private BigDecimal price;
    private Product product;
}
