package com.GadgetZone.repository;

import com.GadgetZone.domain.Favorite;
import com.GadgetZone.domain.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {
    private final Connection connection;

    public FavoriteRepository(Connection connection) {
        this.connection = connection;
    }

    public void addToFavorites(int userId, int productId) throws SQLException {
        String sql = "INSERT IGNORE INTO favorites (user_id, product_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    public void removeFromFavorites(int userId, int productId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    public boolean isFavorite(int userId, int productId) throws SQLException {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public List<Product> getFavoritesByUserId(int userId) throws SQLException {
        String sql = "SELECT p.* FROM products p JOIN favorites f ON p.id = f.product_id WHERE f.user_id = ?";
        List<Product> favorites = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setDescription(rs.getString("description"));
                // Добавь остальные поля, если нужно
                favorites.add(product);
            }
        }
        return favorites;
    }
}
