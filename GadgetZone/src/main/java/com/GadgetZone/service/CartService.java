package com.GadgetZone.service;

import com.GadgetZone.dao.CartRepository;
import com.GadgetZone.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ConcurrentHashMap<Long, Cart> userCarts = new ConcurrentHashMap<>();

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void addItem(int userId, int productId) {
        cartRepository.addItem(userId, productId);
    }

    public void removeItem(int userId, int productId) {
        cartRepository.removeItem(userId, productId);
    }

    public List<CartItem> getCartItems(int userId) {
        return cartRepository.getItems(userId);
    }

    public void clearCart(int userId) {
        cartRepository.clearCart(userId);
    }

    public double getTotalAmount(int userId) {
        return cartRepository.getTotalAmount(userId);
    }

    public Cart getCartForUser(User user) {
        return userCarts.computeIfAbsent(user.getId(), k -> new Cart());
    }

    public void clearCart(User user) {
        userCarts.remove(user.getId());
    }

    public List<OrderDetails> getOrderDetailsFromCart(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    OrderDetails details = new OrderDetails();
                    details.setProductId((long) item.getProduct().getId());
                    details.setPrice(BigDecimal.valueOf(item.getProduct().getPrice()));
                    details.setAmount(BigDecimal.valueOf(item.getQuantity()));
                    return details;
                }).collect(Collectors.toList());
    }

    public BigDecimal calculateTotal(Cart cart) {
        return cart.getTotalPrice();
    }
}