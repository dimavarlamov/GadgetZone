package com.GadgetZone.service;

import com.GadgetZone.entity.Order;
import com.GadgetZone.entity.OrderItem;
import com.GadgetZone.entity.Product;
import com.GadgetZone.repository.OrderRepository;
import com.GadgetZone.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public Map<String, Object> generateSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

        Map<String, Object> report = new HashMap<>();

        // Общая выручка
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);

        // Продажи по товарам
        Map<Product, Long> productSales = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getProduct,
                        Collectors.summingLong(OrderItem::getQuantity)
                ));
        report.put("productSales", productSales);

        // Дополнительная статистика
        report.put("totalOrders", orders.size());
        report.put("startDate", startDate);
        report.put("endDate", endDate);

        return report;
    }
}