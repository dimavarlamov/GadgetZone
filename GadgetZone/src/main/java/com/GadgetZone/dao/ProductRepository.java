package com.GadgetZone.dao;

import com.GadgetZone.domain.Product;
import java.util.List;
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

    public List<Product> findAll() {
        return jdbc.query("SELECT * FROM products", (rs, rowNum) ->
            new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getInt("category_id"),
                rs.getInt("seller_id")
                )
            );
    }
}