package com.GadgetZone.repository;

import com.GadgetZone.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
    private final JdbcTemplate jdbc;
    private final ProductRepository productRepository;

    private static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setTotalAmount(rs.getBigDecimal("total_amount"));
            order.setStatus(OrderStatus.valueOf(rs.getString("status")));
            order.setDeliveryAddress(rs.getString("delivery_address"));
            order.setPhoneNumber(rs.getString("phone_number"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            return order;
        }
    }

    @Transactional
    public Order save(Order order, List<CartItem> cartItems) {
        String sql = "INSERT INTO orders (user_id, total_amount, status, delivery_address, order_date, phone_number) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                order.getUser().getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getDeliveryAddress(),
                order.getOrderDate(),
                order.getPhoneNumber());

        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (id == null) {
            throw new RuntimeException("Failed to retrieve order ID");
        }
        logger.info("Saved order with id={}", id);
        order.setId(id);
        if (cartItems != null && !cartItems.isEmpty()) {
            saveOrderItems(id, cartItems);
        }
        return order;
    }

    @Transactional
    public void update(Order order) {
        String sql = "UPDATE orders SET status = ?, total_amount = ?, delivery_address = ?, phone_number = ? WHERE id = ?";
        jdbc.update(sql,
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getPhoneNumber(),
                order.getId());
        logger.info("Updated order with id={}", order.getId());
    }

    private void saveOrderItems(Long orderId, List<CartItem> items) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        items.forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            jdbc.update(sql, orderId, item.getProductId(), item.getQuantity(), product.getPrice());
        });
    }

    public Optional<Order> findByIdAndUserId(Long orderId, Long userId) {
        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ?";
        try {
            return Optional.of(jdbc.queryForObject(sql, new OrderRowMapper(), orderId, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE product_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, productId);
        return count != null && count > 0;
    }

    public boolean existsByUserAndProduct(Long userId, Long productId) {
        String sql = """
            SELECT COUNT(*) FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            WHERE o.user_id = ? AND oi.product_id = ?
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    public List<Order> findByUserId(Long userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        List<Order> orders = jdbc.query(sql, new OrderRowMapper(), userId);
        orders.forEach(order -> order.setItems(getOrderItems(order.getId())));
        return orders;
    }

    public List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ?";
        List<Order> orders = jdbc.query(sql, new OrderRowMapper(), start, end);
        orders.forEach(order -> order.setItems(getOrderItems(order.getId())));
        return orders;
    }

    private List<OrderItem> getOrderItems(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        return jdbc.query(sql, new OrderItemRowMapper(productRepository), orderId);
    }

    public List<Order> findBySellerId(Long sellerId) {
        String sql = """
        SELECT DISTINCT o.* FROM orders o
        JOIN order_items oi ON o.id = oi.order_id
        JOIN products p ON oi.product_id = p.id
        WHERE p.seller_id = ?
        """;
        List<Order> orders = jdbc.query(sql, new OrderRowMapper(), sellerId);
        orders.forEach(order -> order.setItems(getOrderItems(order.getId())));
        return orders;
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try {
            Order order = jdbc.queryForObject(sql, new OrderRowMapper(), id);
            if (order != null) {
                order.setItems(getOrderItems(order.getId()));
            }
            return Optional.ofNullable(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

class OrderItemRowMapper implements RowMapper<OrderItem> {
    private final ProductRepository productRepository;

    public OrderItemRowMapper(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public OrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product product = productRepository.findById(rs.getLong("product_id"))
                .orElseThrow(() -> new SQLException("Product not found"));
        return new OrderItem(product, rs.getInt("quantity"));
    }
}