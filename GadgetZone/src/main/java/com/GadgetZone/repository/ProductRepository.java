package com.GadgetZone.repository;

import com.GadgetZone.entity.Product;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;


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

    public Page<Product> search(String query, Pageable pageable) {
        String sql = "SELECT * FROM products WHERE name LIKE ? LIMIT ? OFFSET ?";
        List<Product> products = jdbc.query(
                sql,
                new ProductRowMapper(),
                "%" + query + "%",
                pageable.getPageSize(),
                pageable.getOffset()
        );
        return new PageImpl<>(products, pageable, countSearchResults(query));
    }

    private long countSearchResults(String query) {
        String sql = "SELECT COUNT(*) FROM products WHERE name LIKE ?";
        return jdbc.queryForObject(sql, Long.class, "%" + query + "%");
    }

    @Transactional
    public void delete(Long id) {
        jdbc.update("DELETE FROM products WHERE id = ?", id);
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
    public Page<Product> searchAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append("AND (name LIKE ? OR description LIKE ?) ");
            countSql.append("AND (name LIKE ? OR description LIKE ?) ");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        }

        if (category != null && !category.isBlank()) {
            sql.append("AND category_id = (SELECT id FROM categories WHERE name = ?) ");
            countSql.append("AND category_id = (SELECT id FROM categories WHERE name = ?) ");
            params.add(category);
        }

        if (minPrice != null) {
            sql.append("AND price >= ? ");
            countSql.append("AND price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append("AND price <= ? ");
            countSql.append("AND price <= ? ");
            params.add(maxPrice);
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<Product> products = jdbc.query(sql.toString(), new ProductRowMapper(), params.toArray());
        long total = jdbc.queryForObject(countSql.toString(), Long.class, params.subList(0, params.size() - 2).toArray());

        return new PageImpl<>(products, pageable, total);
    }
}
