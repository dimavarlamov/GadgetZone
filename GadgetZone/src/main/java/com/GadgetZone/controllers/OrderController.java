package com.GadgetZone.controllers;

import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.Order;
import com.GadgetZone.entity.OrderStatus;
import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.InsufficientFundsException;
import com.GadgetZone.exceptions.InsufficientStockException;
import com.GadgetZone.service.CartService;
import com.GadgetZone.service.NotificationService;
import com.GadgetZone.service.OrderService;
import com.GadgetZone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final UserService userService;

    // Форма оформления заказа
    @GetMapping("/checkout")
    public String checkoutForm(Model model,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = getAuthenticatedUser(authentication);

        List<CartItem> items = cartService.getCartItems(user.getId());
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Корзина пуста");
            return "redirect:/cart";
        }
        model.addAttribute("order", new Order());
        model.addAttribute("cartItems", items);
        model.addAttribute("total", cartService.calculateTotal(items));
        return "checkout";
    }

    // Обработка оформления заказа
    @PostMapping("/checkout")
    public String processOrder(@ModelAttribute Order order,
                               RedirectAttributes attributes,
                               Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            attributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }
        User user = getAuthenticatedUser(authentication);

        try {
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            if (cartItems.isEmpty()) {
                attributes.addFlashAttribute("error", "Корзина пуста");
                return "redirect:/cart";
            }
            Order createdOrder = orderService.createOrder(user.getId(), cartItems, order.getDeliveryAddress(), order.getPhoneNumber());
            notificationService.sendOrderConfirmation(createdOrder);
            attributes.addFlashAttribute("success", "Заказ успешно оформлен");
            return "redirect:/orders/" + createdOrder.getId();
        } catch (InsufficientStockException | InsufficientFundsException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    // Страница с деталями заказа
    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }
        User user = getAuthenticatedUser(authentication);

        Order order = orderService.getOrderForUser(id, user.getId());
        model.addAttribute("order", order);
        return "order-details";
    }

    // Просмотр заказов продавца
    @GetMapping("/seller/orders")
    public String viewSellerOrders(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        logger.info("Loading orders for seller with email: {}", email);

        List<Order> orders = orderService.getOrdersBySeller(user.getId());
        orders.forEach(order -> {
            if (order.getOrderDate() != null) {
                order.setOrderDateAsDate(order.getOrderDateAsDate());
            }
        });

        logger.info("Found {} orders for sellerId: {}", orders.size(), user.getId());
        model.addAttribute("orders", orders);
        return "manage-orders";
    }

    // Просмотр заказов покупателя
    @GetMapping("/orders")
    public String viewOrders(Authentication authentication, Model model, RedirectAttributes attributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            attributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }
        User user = getAuthenticatedUser(authentication);

        try {
            List<Order> orders = orderService.getOrdersByUserId(user.getId());
            model.addAttribute("orders", orders);
            model.addAttribute("cartCount", cartService.getCartCount(user.getId()));
            return "orders";
        } catch (Exception e) {
            logger.error("Error loading orders for userId={}: {}", user.getId(), e.getMessage());
            attributes.addFlashAttribute("error", "Ошибка при загрузке заказов");
            return "redirect:/";
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Требуется авторизация");
        }
        return userService.getUserByEmail(authentication.getName());
    }

    @GetMapping("/seller/orders/{id}")
    public String sellerOrderDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        logger.info("Loading order details for orderId: {} and sellerId: {}", id, user.getId());

        Order order = orderService.getOrderById(id);
        if (order == null) {
            logger.error("Order not found for orderId: {}", id);
            redirectAttributes.addFlashAttribute("error", "Заказ не найден");
            return "redirect:/seller/orders";
        }

        boolean hasSellerProducts = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSellerId().equals(user.getId()));
        if (!hasSellerProducts) {
            logger.error("Seller {} does not have access to order {}", user.getId(), id);
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому заказу");
            return "redirect:/seller/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values()); // Для формы изменения статуса
        logger.info("Order details loaded for orderId: {}", id);
        return "seller-order-details";
    }

    @PostMapping("/seller/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("status") OrderStatus status,
                                    Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        logger.info("Updating status for orderId: {} to status: {} by sellerId: {}", id, status, user.getId());

        Order order = orderService.getOrderById(id);
        if (order == null) {
            logger.error("Order not found for orderId: {}", id);
            redirectAttributes.addFlashAttribute("error", "Заказ не найден");
            return "redirect:/seller/orders";
        }

        boolean hasSellerProducts = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSellerId().equals(user.getId()));
        if (!hasSellerProducts) {
            logger.error("Seller {} does not have access to order {}", user.getId(), id);
            redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому заказу");
            return "redirect:/seller/orders";
        }

        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Статус заказа успешно обновлён на " + status);
            logger.info("Order status updated for orderId: {} to {}", id, status);
        } catch (Exception e) {
            logger.error("Failed to update order status for orderId: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении статуса заказа");
        }

        return "redirect:/seller/orders/" + id;
    }

}