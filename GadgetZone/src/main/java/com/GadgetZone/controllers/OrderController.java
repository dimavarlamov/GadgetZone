package com.GadgetZone.controllers;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    // Страница корзины
    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        // Получаем товары из корзины пользователя через JDBC
        List<CartItem> items = cartService.getCartItems(user.getId());
        // Вычисляем общую сумму
        BigDecimal total = cartService.calculateTotal(items);
        // Добавляем атрибуты для отображения в модели
        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        return "cart";  // Возвращаем страницу корзины
    }

    // Форма оформления заказа
    @GetMapping("/checkout")
    public String checkoutForm(Model model, @AuthenticationPrincipal User user) {
        // Получаем товары из корзины пользователя через JDBC
        List<CartItem> items = cartService.getCartItems(user.getId());
        // Если корзина пуста, перенаправляем на страницу корзины
        if (items.isEmpty()) {
            return "redirect:/orders/cart";
        }
        // Создаем пустой объект заказа для заполнения пользователем
        model.addAttribute("order", new Order());
        return "checkout";  // Возвращаем страницу оформления заказа
    }

    // Обработка оформления заказа
    @PostMapping("/checkout")
    public String processOrder(@ModelAttribute Order order,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes attributes) {
        try {
            // Создаем заказ и сохраняем его через сервис (используя JDBC)
            Order createdOrder = orderService.createOrder(user.getId(), order.getDeliveryAddress());
            // Отправляем уведомление о подтверждении заказа
            notificationService.sendOrderConfirmation(createdOrder);
            // Перенаправляем на страницу с деталями заказа
            attributes.addFlashAttribute("order", createdOrder);
            return "redirect:/orders/" + createdOrder.getId();
        } catch (InsufficientStockException e) {
            // Если недостаточно товаров на складе, перенаправляем на корзину с ошибкой
            attributes.addFlashAttribute("error", "Недостаточно товаров на складе");
        } catch (InsufficientFundsException e) {
            // Если недостаточно средств на балансе, перенаправляем на корзину с ошибкой
            attributes.addFlashAttribute("error", "Недостаточно средств на балансе");
        }
        return "redirect:/orders/cart";  // Возвращаем на корзину в случае ошибки
    }

    // Страница с деталями заказа
    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               Model model) {
        // Получаем заказ по ID из базы данных через JDBC
        Order order = orderService.getOrderForUser(id, user.getId());
        model.addAttribute("order", order);
        return "order-details";  // Страница с деталями заказа
    }
}
