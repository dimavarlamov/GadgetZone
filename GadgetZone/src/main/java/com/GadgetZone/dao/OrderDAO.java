package com.GadgetZone.dao;

import com.GadgetZone.domain.Order;
import com.GadgetZone.domain.OrderDetails;
import com.GadgetZone.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class OrderDAO {

    private final JdbcTemplate jdbcTemplate;
    private final ProductRepository productRepository;

    public OrderDAO(JdbcTemplate jdbcTemplate, ProductRepository productRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.productRepository = productRepository;
    }

    public void addOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, sum, address, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, order.getUserId(), order.getTotalAmount(), order.getDeliveryAddress(), order.getStatus());
    }

    public void addOrderDetails(OrderDetails orderDetails) {
        String sql = "INSERT INTO orders_details (order_id, product_id, amount, price) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                orderDetails.getOrderId(),
                orderDetails.getProductId(),
                orderDetails.getAmount(),
                orderDetails.getPrice());
    }

    public Order getOrderById(Long orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        Order order = jdbcTemplate.queryForObject(sql, new Object[]{orderId}, (rs, rowNum) -> new Order(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getBigDecimal("sum"),
                rs.getString("address"),
                rs.getTimestamp("created").toLocalDateTime(),
                rs.getTimestamp("updated").toLocalDateTime(),
                rs.getString("status")
        ));

        // Подгружаем детали заказа вместе с Product
        List<OrderDetails> details = getOrderDetailsByOrderId(orderId);
        order.setOrderDetails(details);

        return order;
    }

    public List<OrderDetails> getOrderDetailsByOrderId(Long orderId) {
        String sql = "SELECT * FROM orders_details WHERE order_id = ?";
        return jdbcTemplate.query(sql, new Object[]{orderId}, (rs, rowNum) -> {
            OrderDetails details = new OrderDetails();
            details.setId(rs.getLong("id"));
            details.setOrderId(rs.getLong("order_id"));
            details.setProductId(rs.getLong("product_id"));
            details.setAmount(rs.getBigDecimal("amount"));
            details.setPrice(rs.getBigDecimal("price"));

            // Подгружаем продукт по productId
            Product product = productRepository.findById(rs.getLong("product_id"));
            details.setProduct(product);

            return details;
        });
    }

    public List<Order> getAllOrders() {
        String sql = "SELECT * FROM orders";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Order(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getBigDecimal("sum"),
                rs.getString("address"),
                rs.getTimestamp("created").toLocalDateTime(),
                rs.getTimestamp("updated").toLocalDateTime(),
                rs.getString("status")
        ));
    }

    public boolean isProductOrdered(int productId) {
        String sql = "SELECT COUNT(*) FROM orders_details WHERE product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, productId);
        return count != null && count > 0;
    }

}
