package com.GadgetZone.service;

import com.GadgetZone.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final Map<Long, Cart> userCarts = new ConcurrentHashMap<>();

    public Cart getCartForUser(User user) {
        return userCarts.computeIfAbsent(user.getId(), k -> new Cart());
    }

    public void clearCart(User user) {
        userCarts.remove(user.getId());
    }

    public List<OrderDetails> getOrderDetailsFromCart(Cart cart) {
        return cart.getItems().stream().map(item -> {
            OrderDetails details = new OrderDetails();
            details.setProductId((long) item.getProduct().getId());
            details.setPrice(BigDecimal.valueOf(item.getProduct().getPrice()));
            details.setAmount(BigDecimal.valueOf(item.getQuantity()));
            return details;
        }).collect(Collectors.toList());
    }

    public java.math.BigDecimal calculateTotal(Cart cart) {
        return cart.getTotalPrice();
    }
}