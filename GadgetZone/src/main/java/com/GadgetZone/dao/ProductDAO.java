package com.GadgetZone.dao;

import com.GadgetZone.domain.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private final Connection conn;

    public ProductDAO(Connection conn) {
        this.conn = conn;
    }

    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, description, price, stock, category_id, seller_id, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setInt(5, product.getCategoryId());
            stmt.setInt(6, product.getSellerId());
            stmt.setString(7, product.getImageUrl());
            stmt.executeUpdate();
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET description=?, price=?, stock=?, image_url=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getDescription());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getStock());
            stmt.setString(4, product.getImageUrl());
            stmt.setInt(5, product.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteProduct(int id) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM order_details WHERE product_id = ?";
        String sqlDelete = "DELETE FROM products WHERE id = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(sqlDelete)) {
                        deleteStmt.setInt(1, id);
                        deleteStmt.executeUpdate();
                    }
                } else {
                    throw new SQLException("Товар не может быть удалён: он участвует в заказах.");
                }
            }
        }
    }

    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Product> getAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        }
        return products;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getInt("category_id"),
                rs.getInt("seller_id"),
                rs.getString("image_url")
        );
    }
}