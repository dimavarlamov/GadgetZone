package com.GadgetZone.service;

import com.GadgetZone.exceptions.InsufficientStockException;
import com.GadgetZone.exceptions.ProductNotFoundException;
import com.GadgetZone.exceptions.ProductAlreadyInCartException;  // Импортируем новое исключение
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
        // Проверка, есть ли уже товар в корзине
        if (cartRepository.existsByUserAndProduct(userId, productId)) {
            throw new ProductAlreadyInCartException(); // Если товар уже в корзине, выбрасываем исключение
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);  // Проверка на существование товара

        if (product.getStock() < quantity) {
            throw new InsufficientStockException();  // Проверка на достаточное количество товара
        }

        cartRepository.addItem(userId, productId, quantity);  // Добавление товара в корзину
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartRepository.removeItem(userId, productId);  // Удаление товара из корзины
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId);  // Получение всех товаров в корзине
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.clear(userId);  // Очистка корзины
    }

    public BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))  // Подсчёт общей стоимости товаров
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
