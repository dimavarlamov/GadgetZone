package com.GadgetZone.controllers;

import com.GadgetZone.entity.Cart;
import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.Order;
import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.InsufficientFundsException;
import com.GadgetZone.exceptions.InsufficientStockException;
import com.GadgetZone.service.CartService;
import com.GadgetZone.service.NotificationService;
import com.GadgetZone.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final NotificationService notificationService;


    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        List<CartItem> items = cartService.getCartItems(user.getId());
        BigDecimal total = cartService.calculateTotal(items);
        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkoutForm(Model model, @AuthenticationPrincipal User user) {
        List<CartItem> items = cartService.getCartItems(user.getId());
        if (items.isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("order", new Order());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processOrder(@ModelAttribute Order order,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes attributes) {
        try {
            Order createdOrder = orderService.createOrder(user.getId(), order.getDeliveryAddress());
            notificationService.sendOrderConfirmation(createdOrder);
            attributes.addFlashAttribute("order", createdOrder);
            return "redirect:/orders/" + createdOrder.getId();
        } catch (InsufficientStockException e) {
            attributes.addFlashAttribute("error", "Недостаточно товаров на складе");
        } catch (InsufficientFundsException e) {
            attributes.addFlashAttribute("error", "Недостаточно средств на балансе");
        }
        return "redirect:/cart";
    }

    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               Model model) {
        Order order = orderService.getOrderForUser(id, user.getId());
        model.addAttribute("order", order);
        return "order-details";
    }
}
