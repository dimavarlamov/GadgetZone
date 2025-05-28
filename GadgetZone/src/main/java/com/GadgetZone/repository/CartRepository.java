package com.GadgetZone.repository;

import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CartRepository {
    private final JdbcTemplate jdbc;

    // Метод для добавления товара в корзину
    public void addItem(Long userId, Long productId, int quantity) {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbc.update(sql, userId, productId, quantity);
    }

    // Метод для получения всех товаров в корзине
    public List<CartItem> findByUserId(Long userId) {
        String sql = "SELECT ci.*, p.* FROM cart_items ci " +
                "JOIN products p ON ci.product_id = p.id " +
                "WHERE ci.user_id = ?";
        return jdbc.query(sql, new CartItemRowMapper(), userId);
    }

    // Метод для удаления товара из корзины
    public void removeItem(Long userId, Long productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        jdbc.update(sql, userId, productId);
    }

    // Метод для очистки корзины
    public void clear(Long userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        jdbc.update(sql, userId);
    }

    // Метод для проверки существования товара в корзине
    public boolean existsByUserAndProduct(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE user_id = ? AND product_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    // Маппер для CartItem
    private static class CartItemRowMapper implements RowMapper<CartItem> {
        @Override
        public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = Product.builder()
                    .id(rs.getLong("product_id"))
                    .name(rs.getString("name"))
                    .price(rs.getBigDecimal("price"))
                    .build();

            return new CartItem(
                    product,
                    rs.getInt("quantity")
            );
        }
    }
}
