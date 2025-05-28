package com.GadgetZone.repository;

import com.GadgetZone.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ProductRepository {
    private final JdbcTemplate jdbc;

    public Product save(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock, category_id, seller_id, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory().getId(),
                product.getSellerId(),
                product.getImageUrl());

        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        product.setId(id);
        return product;
    }

    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new ProductRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Product> findBySellerId(Long sellerId) {
        String sql = "SELECT * FROM products WHERE seller_id = ?";
        return jdbc.query(sql, new ProductRowMapper(), sellerId);
    }

    public List<Product> search(String query, int page, int size) {
        String sql = "SELECT * FROM products WHERE name LIKE ? LIMIT ? OFFSET ?";
        return jdbc.query(sql, new ProductRowMapper(), "%" + query + "%", size, page * size);
    }

    public long countSearchResults(String query) {
        String sql = "SELECT COUNT(*) FROM products WHERE name LIKE ?";
        return jdbc.queryForObject(sql, Long.class, "%" + query + "%");
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM products WHERE id = ?", id);
    }

    public List<Product> searchAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append("AND (name LIKE ? OR description LIKE ?) ");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        }

        if (category != null && !category.isBlank()) {
            sql.append("AND category_id = (SELECT id FROM categories WHERE name = ?) ");
            params.add(category);
        }

        if (minPrice != null) {
            sql.append("AND price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND price <= ? ");
            params.add(maxPrice);
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbc.query(sql.toString(), new ProductRowMapper(), params.toArray());
    }

    public long countSearchAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append("AND (name LIKE ? OR description LIKE ?) ");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        }

        if (category != null && !category.isBlank()) {
            sql.append("AND category_id = (SELECT id FROM categories WHERE name = ?) ");
            params.add(category);
        }

        if (minPrice != null) {
            sql.append("AND price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND price <= ? ");
            params.add(maxPrice);
        }

        return jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    // Добавленный метод countAdvanced, делегирующий вызов countSearchAdvanced
    public long countAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        return countSearchAdvanced(query, category, minPrice, maxPrice);
    }

    private static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Product.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .price(rs.getBigDecimal("price"))
                    .stock(rs.getInt("stock"))
                    .sellerId(rs.getLong("seller_id"))
                    .imageUrl(rs.getString("image_url"))
                    .build();
        }
    }
}
