package com.GadgetZone.dao;

import com.GadgetZone.domain.CartItem;
import java.util.List;

public interface CartRepository {
    void addItem(int userId, int productId);
    void removeItem(int userId, int productId);
    List<CartItem> getItems(int userId);
    void clearCart(int userId);
    double getTotalAmount(int userId);
}