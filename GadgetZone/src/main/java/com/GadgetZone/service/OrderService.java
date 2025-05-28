package com.GadgetZone.service;

import com.GadgetZone.entity.*;
import com.GadgetZone.exceptions.*;
import com.GadgetZone.repository.OrderRepository;
import com.GadgetZone.repository.ProductRepository;
import com.GadgetZone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    // Создание нового заказа
    @Transactional
    public Order createOrder(Long userId, String deliveryAddress) {
        // Получаем пользователя по ID
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // Получаем товары из корзины пользователя через сервис корзины
        List<CartItem> cartItems = cartService.getCartItems(userId);
        validateCart(cartItems);

        // Строим заказ
        Order order = buildOrder(user, deliveryAddress, cartItems);
        processOrder(user, order);

        // Сохраняем заказ через репозиторий JDBC
        return orderRepository.save(order);
    }

    // Проверка, купил ли пользователь товар
    public boolean hasUserPurchasedProduct(Long userId, Long productId) {
        return orderRepository.existsByUserAndProduct(userId, productId);
    }

    // Проверка корзины пользователя
    private void validateCart(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        cartItems.forEach(item -> {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(ProductNotFoundException::new);

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException();
            }
        });
    }

    // Строим заказ
    private Order buildOrder(User user, String address, List<CartItem> cartItems) {
        return Order.builder()
                .user(user)
                .deliveryAddress(address)
                .status(OrderStatus.NEW)
                .items(convertToOrderItems(cartItems))
                .totalAmount(calculateTotal(cartItems))
                .orderDate(LocalDateTime.now())
                .build();
    }

    // Преобразуем элементы корзины в элементы заказа
    private List<OrderItem> convertToOrderItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> new OrderItem(item.getProduct(), item.getQuantity()))
                .collect(Collectors.toList());
    }

    // Получаем заказ по ID пользователя
    public Order getOrderForUser(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new UserNotFoundException());
    }

    // Получаем все заказы пользователя
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // Вычисляем общую сумму заказа
    private BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Обрабатываем заказ
    private void processOrder(User user, Order order) {
        checkUserBalance(user, order.getTotalAmount());
        updateProductStock(order.getItems());
        cartService.clearCart(user.getId());
        deductUserBalance(user, order.getTotalAmount());
    }

    // Проверка баланса пользователя
    private void checkUserBalance(User user, BigDecimal amount) {
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
    }

    // Обновляем остатки товаров
    private void updateProductStock(List<OrderItem> items) {
        items.forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product); // Используется JDBC для сохранения данных о товаре
        });
    }

    // Списание средств с баланса пользователя
    private void deductUserBalance(User user, BigDecimal amount) {
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user); // Используется JDBC для сохранения пользователя
    }
}
