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

    @Transactional
    public Order createOrder(Long userId, List<CartItem> cartItems, String deliveryAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        validateCart(cartItems);

        Order order = buildOrder(user, deliveryAddress, cartItems);
        processOrderItems(user, order, cartItems); // Передаём user вместо userId

        return orderRepository.save(order);
    }

    private void processOrderItems(User user, Order order, List<CartItem> cartItems) {
        cartItems.forEach(item -> cartService.removeFromCart(user.getId(), item.getProductId())); // Используем user.getId()
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

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException();
            }
        });
    }

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
        return items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(ProductNotFoundException::new);
                    return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void processOrder(User user, Order order) {
        checkUserBalance(user, order.getTotalAmount());
        updateProductStock(order.getItems());
        cartService.clearCart(user.getId());
        deductUserBalance(user, order.getTotalAmount());
    }

    private void checkUserBalance(User user, BigDecimal amount) {
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
        user.setBalance(user.getBalance().subtract(amount));
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
        orderRepository.save(order);
    }
}