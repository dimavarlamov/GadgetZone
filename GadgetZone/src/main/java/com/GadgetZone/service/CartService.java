package com.GadgetZone.service;

import com.GadgetZone.exceptions.InsufficientStockException;
import com.GadgetZone.exceptions.ProductNotFoundException;
import com.GadgetZone.repository.CartRepository;
import com.GadgetZone.entity.*;
import com.GadgetZone.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (product.getStock() < quantity) {
            throw new InsufficientStockException();
        }

        cartRepository.addItem(userId, productId, quantity);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartRepository.removeItem(userId, productId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.clear(userId);
    }

    public BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
