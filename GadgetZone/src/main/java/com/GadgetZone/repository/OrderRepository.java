package com.GadgetZone.repository;

import com.GadgetZone.entity.Order;
import com.GadgetZone.entity.OrderItem;
import com.GadgetZone.entity.OrderStatus;
import com.GadgetZone.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final JdbcTemplate jdbc;
    private final ProductRepository productRepository;

    // Маппер для заказов
    private static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setTotalAmount(rs.getBigDecimal("total_amount"));
            order.setStatus(OrderStatus.valueOf(rs.getString("status")));
            order.setDeliveryAddress(rs.getString("delivery_address"));
            order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            return order;
        }
    }

    // Сохранение заказа в базу данных
    @Transactional
    public Order save(Order order) {
        String sql = "INSERT INTO orders (user_id, total_amount, status, delivery_address, order_date) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql,
                order.getUser().getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getDeliveryAddress(),
                order.getOrderDate());

        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        order.setId(id);
        saveOrderItems(order);
        return order;
    }

    // Сохранение элементов заказа (связь с продуктами)
    private void saveOrderItems(Order order) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
        order.getItems().forEach(item -> {
            jdbc.update(sql,
                    order.getId(),
                    item.getProduct().getId(),
                    item.getQuantity());
        });
    }

    // Получение заказа по ID и userId
    public Optional<Order> findByIdAndUserId(Long orderId, Long userId) {
        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ?";
        try {
            return Optional.of(jdbc.queryForObject(sql, new OrderRowMapper(), orderId, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Проверка: есть ли заказ с таким productId для любого пользователя
    public boolean existsByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE product_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, productId);
        return count != null && count > 0;
    }

    // Проверка: есть ли заказ с данным товаром у конкретного пользователя
    public boolean existsByUserAndProduct(Long userId, Long productId) {
        String sql = """
            SELECT COUNT(*) FROM order_items oi
            JOIN orders o ON oi.order_id = o.id
            WHERE o.user_id = ? AND oi.product_id = ?
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    // Получение всех заказов пользователя
    public List<Order> findByUserId(Long userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        List<Order> orders = jdbc.query(sql, new OrderRowMapper(), userId);
        orders.forEach(order -> order.setItems(getOrderItems(order.getId())));
        return orders;
    }

    // Получение всех заказов в промежутке дат
    public List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ?";
        List<Order> orders = jdbc.query(sql, new OrderRowMapper(), start, end);
        orders.forEach(order -> order.setItems(getOrderItems(order.getId())));
        return orders;
    }

    // Получение всех товаров для заказа по orderId
    private List<OrderItem> getOrderItems(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        return jdbc.query(sql, new OrderItemRowMapper(productRepository), orderId);
    }
}

// Маппер для элементов заказа
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
