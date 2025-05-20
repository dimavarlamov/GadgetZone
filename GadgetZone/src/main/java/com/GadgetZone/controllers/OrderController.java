package com.GadgetZone.controllers;

import com.GadgetZone.domain.Cart;
import com.GadgetZone.domain.Order;
import com.GadgetZone.domain.OrderStatus;
import com.GadgetZone.domain.User;
import com.GadgetZone.service.CartService;
import com.GadgetZone.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    public OrderController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    // Страница с корзиной
    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        Cart cart = cartService.getCartForUser(user);
        model.addAttribute("cart", cart);
        return "cart"; // шаблон cart.html
    }

    // Оформление заказа (форма для ввода данных доставки)
    @GetMapping("/checkout")
    public String checkout(@AuthenticationPrincipal User user, Model model) {
        Cart cart = cartService.getCartForUser(user);
        if (cart.getItems().isEmpty()) {
            model.addAttribute("error", "Ваша корзина пуста!");
            return "redirect:/orders/cart";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("order", new Order());
        return "checkout"; // шаблон checkout.html
    }

    // Подтверждение заказа
    @PostMapping("/checkout")
    public String confirmOrder(@ModelAttribute Order order, @AuthenticationPrincipal User user, Model model) {
        Cart cart = cartService.getCartForUser(user);
        if (cart.getItems().isEmpty()) {
            model.addAttribute("error", "Ваша корзина пуста!");
            return "redirect:/orders/cart";
        }

        // Заполнение полей заказа
        order.setUserId((long) user.getId());
        order.setTotalAmount(cartService.calculateTotal(cart));
        order.setOrderDetails(cartService.getOrderDetailsFromCart(cart));
        order.setStatus("PENDING");

        // Сохранение заказа
        orderService.createOrder(order.getUserId(), order.getDeliveryAddress(), order.getOrderDetails());

        // Очистка корзины
        cartService.clearCart(user);

        // Отправка заказа в шаблон
        model.addAttribute("order", order);
        return "order-confirmation";
    }

    // Просмотр заказа
    @GetMapping("/view/{orderId}")
    public String viewOrder(@PathVariable Long orderId, @AuthenticationPrincipal User user, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals((long) user.getId())) {
            model.addAttribute("error", "Заказ не найден.");
            return "redirect:/orders";
        }
        model.addAttribute("order", order);
        return "order-details";
    }
}
