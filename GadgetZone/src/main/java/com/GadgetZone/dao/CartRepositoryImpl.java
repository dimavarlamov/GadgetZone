package com.GadgetZone.repositories;

import com.GadgetZone.domain.CartItem;
import com.GadgetZone.domain.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartRepositoryImpl implements CartRepository {

    private final Connection connection;

    public CartRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addItem(int userId, int productId) {
        try {
            PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT quantity FROM cart_items WHERE user_id = ? AND product_id = ?");
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int quantity = rs.getInt("quantity") + 1;
                PreparedStatement updateStmt = connection.prepareStatement(
                        "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?");
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, productId);
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = connection.prepareStatement(
                        "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, 1)");
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, productId);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления товара в корзину", e);
        }
    }

    @Override
    public void removeItem(int userId, int productId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?");
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления товара из корзины", e);
        }
    }

    @Override
    public List<CartItem> getItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT p.id, p.name, p.price, p.description, ci.quantity " +
                            "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                            "WHERE ci.user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setDescription(rs.getString("description"));

                CartItem cartItem = new CartItem(product, rs.getInt("quantity"));
                items.add(cartItem);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения корзины", e);
        }
        return items;
    }

    @Override
    public void clearCart(int userId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM cart_items WHERE user_id = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка очистки корзины", e);
        }
    }
    @Override
    public double getTotalAmount(int userId) {
        double total = 0.0;
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT SUM(p.price * ci.quantity) AS total " +
                            "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                            "WHERE ci.user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при подсчёте суммы корзины", e);
        }
        return total;
    }

}
