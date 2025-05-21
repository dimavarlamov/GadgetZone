package com.GadgetZone.dao;

import com.GadgetZone.domain.CartItem;
import com.GadgetZone.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartRepositoryImpl implements CartRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CartRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addItem(int userId, int productId) {
        Integer quantity = jdbcTemplate.queryForObject(
                "SELECT quantity FROM cart_items WHERE user_id = ? AND product_id = ?",
                new Object[]{userId, productId},
                (rs, rowNum) -> rs.getInt("quantity")
        );

        if (quantity != null) {
            jdbcTemplate.update(
                    "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?",
                    quantity + 1, userId, productId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, 1)",
                    userId, productId
            );
        }
    }

    @Override
    public void removeItem(int userId, int productId) {
        jdbcTemplate.update(
                "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?",
                userId, productId
        );
    }

    @Override
    public List<CartItem> getItems(int userId) {
        return jdbcTemplate.query(
                "SELECT p.id, p.name, p.price, p.description, ci.quantity " +
                        "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                        "WHERE ci.user_id = ?",
                new Object[]{userId},
                cartItemRowMapper
        );
    }

    @Override
    public void clearCart(int userId) {
        jdbcTemplate.update(
                "DELETE FROM cart_items WHERE user_id = ?",
                userId
        );
    }

    @Override
    public double getTotalAmount(int userId) {
        Double total = jdbcTemplate.queryForObject(
                "SELECT SUM(p.price * ci.quantity) AS total " +
                        "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                        "WHERE ci.user_id = ?",
                new Object[]{userId},
                Double.class
        );
        return total != null ? total : 0.0;
    }

    private final RowMapper<CartItem> cartItemRowMapper = (rs, rowNum) -> {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getDouble("price"));
        product.setDescription(rs.getString("description"));
        int quantity = rs.getInt("quantity");
        return new CartItem(product, quantity);
    };
}