package com.GadgetZone.repository;

import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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

    public void save(CartItem cartItem) {
        if (cartItem.getId() == null) {
            String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
            jdbc.update(sql, cartItem.getUserId(), cartItem.getProductId(), cartItem.getQuantity());
            // Retrieve the generated ID
            Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            if (id == null) {
                throw new RuntimeException("Failed to retrieve generated ID for cart item");
            }
            cartItem.setId(id);
        } else {
            String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
            jdbc.update(sql, cartItem.getQuantity(), cartItem.getId());
        }
    }

    public List<CartItem> findByUserId(Long userId) {
        String sql = "SELECT * FROM cart_items WHERE user_id = ?";
        return jdbc.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(CartItem.class));
    }

    public void deleteByUserIdAndProductId(Long userId, Long productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        jdbc.update(sql, userId, productId);
    }

    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        jdbc.update(sql, userId);
    }

    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE user_id = ? AND product_id = ?";
        Integer count = jdbc.queryForObject(sql, new Object[]{userId, productId}, Integer.class);
        return count != null && count > 0;
    }

    public CartItem findByIdAndUserId(Long cartItemId, Long userId) {
        String sql = "SELECT * FROM cart_items WHERE id = ? AND user_id = ?";
        List<CartItem> items = jdbc.query(sql, new Object[]{cartItemId, userId}, new BeanPropertyRowMapper<>(CartItem.class));
        return items.isEmpty() ? null : items.get(0);
    }

    public void deleteByIdAndUserId(Long cartItemId, Long userId) {
        String sql = "DELETE FROM cart_items WHERE id = ? AND user_id = ?";
        jdbc.update(sql, cartItemId, userId);
    }

    private static class CartItemRowMapper implements RowMapper<CartItem> {
        @Override
        public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            CartItem cartItem = new CartItem();
            cartItem.setId(rs.getLong("id"));
            cartItem.setUserId(rs.getLong("user_id"));
            cartItem.setProductId(rs.getLong("product_id"));
            cartItem.setQuantity(rs.getInt("quantity"));
            return cartItem;
        }
    }
}
