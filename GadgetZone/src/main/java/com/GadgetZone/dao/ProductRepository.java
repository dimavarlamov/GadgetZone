package com.GadgetZone.dao;

import com.GadgetZone.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock, category_id, seller_id) VALUES (?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategoryId(),
            product.getSellerId()
        );
    }
}