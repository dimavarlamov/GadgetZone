package com.GadgetZone.dao;

import com.GadgetZone.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock, category_id, seller_id, image_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryId(),
                product.getSellerId(),
                product.getImageUrl()
        );
    }

    public List<Product> findAll() {
        String sql = "SELECT * FROM products";
        return jdbc.query(sql, (rs, rowNum) -> new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getInt("category_id"),
                rs.getInt("seller_id"),
                rs.getString("image_url")
        ));
    }

    public Product findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        return jdbc.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getInt("category_id"),
                rs.getInt("seller_id"),
                rs.getString("image_url")
        ));
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, category_id = ?, seller_id = ?, image_url = ? WHERE id = ?";
        jdbc.update(sql,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryId(),
                product.getSellerId(),
                product.getImageUrl(),
                product.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbc.update(sql, id);
    }
}
