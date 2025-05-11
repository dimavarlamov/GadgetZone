package com.GadgetZone.dao;

import com.GadgetZone.domain.Order;
import com.GadgetZone.domain.OrderDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderDAO {

    private final JdbcTemplate jdbcTemplate;

    public OrderDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, sum, address, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, order.getUserId(), order.getSum(), order.getAddress(), order.getStatus());
    }

    public void addOrderDetails(OrderDetails orderDetails) {
        String sql = "INSERT INTO orders_details (order_id, product_id, amount, price) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, orderDetails.getOrderId(), orderDetails.getProductId(), orderDetails.getAmount(), orderDetails.getPrice());
    }

    public Order getOrderById(Long orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{orderId}, (rs, rowNum) -> {
            return new Order(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getBigDecimal("sum"),
                    rs.getString("address"),
                    rs.getTimestamp("created").toLocalDateTime(),
                    rs.getTimestamp("updated").toLocalDateTime(),
                    rs.getString("status")
            );
        });
    }

    public List<Order> getAllOrders() {
        String sql = "SELECT * FROM orders";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new Order(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getBigDecimal("sum"),
                    rs.getString("address"),
                    rs.getTimestamp("created").toLocalDateTime(),
                    rs.getTimestamp("updated").toLocalDateTime(),
                    rs.getString("status")
            );
        });
    }
}
