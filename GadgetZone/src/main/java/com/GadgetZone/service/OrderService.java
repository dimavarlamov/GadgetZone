package com.GadgetZone.service;

import com.GadgetZone.entity.*;
import com.GadgetZone.exceptions.*;
import com.GadgetZone.repository.OrderRepository;
import com.GadgetZone.repository.ProductRepository;
import com.GadgetZone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public Order createOrder(Long userId, List<CartItem> cartItems, String deliveryAddress, String phoneNumber) {
        logger.info("Создание заказа для userId={}. Элементы корзины: {}", userId, cartItems);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateCart(cartItems);
        validatePhoneNumber(phoneNumber);

        Order order = buildOrder(user, deliveryAddress, phoneNumber, cartItems);
        logger.info("Создан заказ с totalAmount={}", order.getTotalAmount());
        processOrderItems(user, order, cartItems);

        Order savedOrder = orderRepository.save(order, cartItems);
        logger.info("Заказ сохранён с id={}", savedOrder.getId());
        return savedOrder;
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\+7 \\d{3} \\d{3} \\d{2} \\d{2}")) {
            throw new IllegalArgumentException("Неверный формат номера телефона. Используйте: +7 XXX XXX XX XX");
        }
    }

    private void processOrderItems(User user, Order order, List<CartItem> cartItems) {
        logger.info("Обработка элементов заказа для userId={}. TotalAmount={}", user.getId(), order.getTotalAmount());
        cartItems.forEach(item -> cartService.removeFromCart(user.getId(), item.getProductId()));
        checkUserBalance(user, order.getTotalAmount());
        updateProductStock(order.getItems());
        deductUserBalance(user, order.getTotalAmount());
    }

    public boolean hasUserPurchasedProduct(Long userId, Long productId) {
        return orderRepository.existsByUserAndProduct(userId, productId);
    }

    private void validateCart(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        cartItems.forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(ProductNotFoundException::new);
            logger.info("Проверка товара: productId={}, price={}, quantity={}", item.getProductId(), product.getPrice(), item.getQuantity());
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException();
            }
        });
    }

    private Order buildOrder(User user, String address, String phoneNumber, List<CartItem> cartItems) {
        Order order = Order.builder()
                .user(user)
                .deliveryAddress(address)
                .phoneNumber(phoneNumber)
                .status(OrderStatus.NEW)
                .items(convertToOrderItems(cartItems))
                .totalAmount(calculateTotal(cartItems))
                .orderDate(LocalDateTime.now())
                .build();
        logger.info("Собран заказ: totalAmount={}", order.getTotalAmount());
        return order;
    }

    private List<OrderItem> convertToOrderItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(ProductNotFoundException::new);
                    return new OrderItem(product, item.getQuantity());
                })
                .collect(Collectors.toList());
    }

    public Order getOrderForUser(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ProductNotFoundException("Order not found"));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        BigDecimal total = items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(ProductNotFoundException::new);
                    BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    logger.info("Расчёт: productId={}, price={}, quantity={}, itemTotal={}", item.getProductId(), product.getPrice(), item.getQuantity(), itemTotal);
                    return itemTotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.info("Общая сумма заказа: {}", total);
        return total;
    }

    private void checkUserBalance(User user, BigDecimal amount) {
        logger.info("Проверка баланса userId={}: balance={}, required={}", user.getId(), user.getBalance(), amount);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
    }

    private void updateProductStock(List<OrderItem> items) {
        items.forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        });
    }

    private void deductUserBalance(User user, BigDecimal amount) {
        BigDecimal oldBalance = user.getBalance();
        user.setBalance(user.getBalance().subtract(amount));
        logger.info("Списание для userId={}: oldBalance={}, amount={}, newBalance={}", user.getId(), oldBalance, amount, user.getBalance());
        userRepository.save(user);
    }

    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ProductNotFoundException("Order not found"));
        order.setStatus(status);
        orderRepository.update(order);
    }
}