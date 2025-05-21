package com.GadgetZone.dao;

import com.GadgetZone.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FavoriteRepository {
    private final JdbcTemplate jdbcTemplate;

    public FavoriteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addToFavorites(Long userId, int productId) {
        String sql = "INSERT IGNORE INTO favorites (user_id, product_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, productId);
    }

    public void removeFromFavorites(Long userId, int productId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND product_id = ?";
        jdbcTemplate.update(sql, userId, productId);
    }

    public boolean isFavorite(Long userId, int productId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND product_id = ?";
        Integer result = jdbcTemplate.query(
                sql,
                new Object[]{userId, productId},
                rs -> rs.next() ? 1 : null
        );
        return result != null;
    }

    public List<Product> getFavoritesByUserId(Long userId) {
        String sql = "SELECT p.* FROM products p JOIN favorites f ON p.id = f.product_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) -> {
            Product product = new Product();
            product.setId(rs.getInt("id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            product.setCategoryId(rs.getInt("category_id"));
            product.setSellerId(rs.getInt("seller_id"));
            product.setImageUrl(rs.getString("image_url"));
            return product;
        });
    }
}