package com.GadgetZone.service;

import com.GadgetZone.dao.OrderDAO;
import com.GadgetZone.domain.Order;
import com.GadgetZone.domain.OrderDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderDAO orderDAO;

    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    public Order createOrder(Long userId, String address, List<OrderDetails> details) {
        // Вычисляем сумму заказа
        BigDecimal totalSum = calculateTotalSum(details);

        // Создаём заказ
        Order order = new Order(userId, totalSum, address, null, null, "PENDING");

        // Сохраняем заказ в базе данных
        orderDAO.addOrder(order);

        // Добавляем детали заказа
        for (OrderDetails orderDetails : details) {
            orderDetails.setOrderId(order.getId());  // Устанавливаем id заказа в детали
            orderDAO.addOrderDetails(orderDetails);
        }

        return order;
    }

    public Order getOrderById(Long orderId) {
        return orderDAO.getOrderById(orderId);
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    private BigDecimal calculateTotalSum(List<OrderDetails> details) {
        BigDecimal totalSum = BigDecimal.ZERO;
        for (OrderDetails orderDetails : details) {
            BigDecimal itemTotal = orderDetails.getPrice().multiply(orderDetails.getAmount());
            totalSum = totalSum.add(itemTotal);
        }
        return totalSum;
    }
}
