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

        // Создаём объект заказа
        Long id = null;
        String status = "PENDING";
        Order order = new Order(id, userId, totalSum, status, null, null, address);

        // Сохраняем заказ (в таблицу orders)
        orderDAO.addOrder(order);

        // Устанавливаем ID заказа в каждый OrderDetails и сохраняем
        for (OrderDetails orderDetails : details) {
            orderDetails.setOrderId(order.getId());
            orderDAO.addOrderDetails(orderDetails);
        }

        return order;
    }

    public Order getOrderById(Long orderId) {
        return orderDAO.getOrderById(orderId);  // тут автоматически подтягиваются details + product
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
