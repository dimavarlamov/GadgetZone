package com.GadgetZone.entity;

import com.GadgetZone.repository.ProductRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private List<CartItem> items = new ArrayList<>();
    @Autowired
    private ProductRepository productRepository; // Временное решение, лучше использовать сервис

    public void addItem(Product product, int quantity) {
        items.add(new CartItem(product, quantity));
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new IllegalStateException("Product not found"));
                    return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}