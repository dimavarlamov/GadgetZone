package com.GadgetZone.repository;

import com.GadgetZone.entity.Category;
import com.GadgetZone.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final JdbcTemplate jdbc;

    public List<Product> search(String query, int page, int size) {
        String sql = "SELECT p.*, c.title as category_title " +
                "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.name LIKE ? LIMIT ? OFFSET ?";
        return jdbc.query(sql, new Object[]{"%" + (query != null ? query : "") + "%", size, page * size}, new ProductRowMapper());
    }

    public long countSearchResults(String query) {
        String sql = "SELECT COUNT(*) FROM products WHERE name LIKE ?";
        return jdbc.queryForObject(sql, new Object[]{"%" + (query != null ? query : "") + "%"}, Long.class);
    }

    public List<Product> searchAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.title as category_title " +
                        "FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            sql.append(" AND p.name LIKE ?");
            params.add("%" + query + "%");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND c.title = ?");
            params.add(category);
        }
        if (minPrice != null) {
            sql.append(" AND p.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.price <= ?");
            params.add(maxPrice);
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbc.query(sql.toString(), params.toArray(), new ProductRowMapper());
    }

    public long countSearchAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p LEFT JOIN categories c ON p.category_id = c.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            sql.append(" AND p.name LIKE ?");
            params.add("%" + query + "%");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND c.title = ?");
            params.add(category);
        }
        if (minPrice != null) {
            sql.append(" AND p.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.price <= ?");
            params.add(maxPrice);
        }

        return jdbc.queryForObject(sql.toString(), params.toArray(), Long.class);
    }

    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.*, c.title as category_title " +
                "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new Object[]{id}, new ProductRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Product> findBySellerId(Long sellerId) {
        String sql = "SELECT p.*, c.title as category_title " +
                "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.seller_id = ?";
        return jdbc.query(sql, new Object[]{sellerId}, new ProductRowMapper());
    }

    public List<Product> findBySellerIdPaginated(Long sellerId, int page, int size) {
        String sql = "SELECT p.*, c.title as category_title " +
                "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.seller_id = ? LIMIT ? OFFSET ?";
        return jdbc.query(sql, new Object[]{sellerId, size, page * size}, new ProductRowMapper());
    }

    public List<Product> searchBySeller(String query, Long sellerId, int page, int size) {
        String sql = "SELECT p.*, c.title as category_title " +
                "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.seller_id = ? AND p.name LIKE ? LIMIT ? OFFSET ?";
        return jdbc.query(sql, new Object[]{sellerId, "%" + (query != null ? query : "") + "%", size, page * size}, new ProductRowMapper());
    }

    public long countBySellerId(Long sellerId) {
        String sql = "SELECT COUNT(*) FROM products WHERE seller_id = ?";
        return jdbc.queryForObject(sql, new Object[]{sellerId}, Long.class);
    }

    public long countSearchBySeller(String query, Long sellerId) {
        String sql = "SELECT COUNT(*) FROM products WHERE seller_id = ? AND name LIKE ?";
        return jdbc.queryForObject(sql, new Object[]{sellerId, "%" + (query != null ? query : "") + "%"}, Long.class);
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            String sql = "INSERT INTO products (name, description, price, stock, category_id, seller_id, image_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbc.update(sql, product.getName(), product.getDescription(), product.getPrice(),
                    product.getStock(), product.getCategory().getId(), product.getSellerId(), product.getImageUrl());
            Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            product.setId(id);
        } else {
            String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, " +
                    "category_id = ?, image_url = ? WHERE id = ?";
            jdbc.update(sql, product.getName(), product.getDescription(), product.getPrice(),
                    product.getStock(), product.getCategory().getId(), product.getImageUrl(), product.getId());
        }
        return product;
    }

    public void delete(Long productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbc.update(sql, productId);
    }

    private static class ProductRowMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Category category = new Category();
            category.setId(rs.getLong("category_id"));
            category.setTitle(rs.getString("category_title"));

            Product product = Product.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .price(rs.getBigDecimal("price"))
                    .stock(rs.getInt("stock"))
                    .category(category)
                    .sellerId(rs.getLong("seller_id"))
                    .imageUrl(rs.getString("image_url"))
                    .build();
            return product;
        }
    }
}