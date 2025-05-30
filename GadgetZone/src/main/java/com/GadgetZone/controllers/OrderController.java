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

        List<Order> orders = orderService.getOrdersBySeller(user.getId());
        model.addAttribute("orders", orders);
        return "manage-orders";
    }

    // Обновление статуса заказа
    @PostMapping("/seller/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus status,
                                    RedirectAttributes attributes) {
        orderService.updateOrderStatus(id, status);
        attributes.addFlashAttribute("success", "Статус заказа обновлён");
        return "redirect:/seller/orders/" + id;
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
}