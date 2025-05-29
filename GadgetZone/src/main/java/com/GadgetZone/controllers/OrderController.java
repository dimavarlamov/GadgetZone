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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final UserService userService;

    // Страница корзины
    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User user, Model model, RedirectAttributes redirectAttributes) {
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        List<CartItem> items = cartService.getCartItems(user.getId());
        BigDecimal total = cartService.calculateTotal(items);
        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        return "cart";
    }

    // Форма оформления заказа
    @GetMapping("/checkout")
    public String checkoutForm(Model model,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes redirectAttributes) {
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        List<CartItem> items = cartService.getCartItems(user.getId());
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Корзина пуста");
            return "redirect:/seller/orders/cart";
        }
        model.addAttribute("order", new Order());
        model.addAttribute("cartItems", items); // Добавляем товары для отображения
        model.addAttribute("total", cartService.calculateTotal(items));
        return "checkout";
    }

    // Обработка оформления заказа
    @PostMapping("/checkout")
    public String processOrder(@ModelAttribute Order order,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes attributes) {
        if (user == null) {
            attributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        try {
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            if (cartItems.isEmpty()) {
                attributes.addFlashAttribute("error", "Корзина пуста");
                return "redirect:/seller/orders/cart";
            }
            Order createdOrder = orderService.createOrder(user.getId(), cartItems, order.getDeliveryAddress());
            notificationService.sendOrderConfirmation(createdOrder);
            attributes.addFlashAttribute("success", "Заказ успешно оформлен");
            return "redirect:/seller/orders/" + createdOrder.getId();
        } catch (InsufficientStockException | InsufficientFundsException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/orders/cart";
        }
    }

    // Страница с деталями заказа
    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        Order order = orderService.getOrderForUser(id, user.getId());
        model.addAttribute("order", order);
        return "order-details";
    }

    // Просмотр заказов продавца
    @GetMapping
    public String viewSellerOrders(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        List<Order> orders = orderService.getOrdersBySeller(user.getId());
        model.addAttribute("orders", orders);
        return "manage-orders";
    }

    // Обновление статуса заказа
    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus status,
                                    RedirectAttributes attributes) {
        orderService.updateOrderStatus(id, status);
        attributes.addFlashAttribute("success", "Статус заказа обновлён");
        return "redirect:/seller/orders/" + id;
    }
}