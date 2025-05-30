package com.GadgetZone.controllers;

import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.Order;
import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.InsufficientFundsException;
import com.GadgetZone.exceptions.InsufficientStockException;
import com.GadgetZone.service.CartService;
import com.GadgetZone.service.FavoriteService;
import com.GadgetZone.service.OrderService;
import com.GadgetZone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final FavoriteService favoriteService;
    private final OrderService orderService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Требуется авторизация");
            return ResponseEntity.status(401).body(response);
        }

        try {
            User user = getAuthenticatedUser(authentication);
            CartItem item = cartService.addToCart(user.getId(), productId, quantity);
            if (item.getId() == null) {
                throw new RuntimeException("Failed to generate cart item ID");
            }
            response.put("success", true);
            response.put("quantity", item.getQuantity());
            response.put("cartItemId", item.getId());
            response.put("productId", productId);
            response.put("cartCount", cartService.getCartCount(user.getId()));
            logger.debug("Added to cart: userId={}, productId={}, quantity={}", user.getId(), productId, quantity);
            return ResponseEntity.ok(response);
        } catch (InsufficientStockException e) {
            response.put("success", false);
            response.put("message", "Недостаточно товара на складе");
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error adding to cart: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Ошибка при добавлении в корзину");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam Long cartItemId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Требуется авторизация");
            return ResponseEntity.status(401).body(response);
        }

        try {
            User user = getAuthenticatedUser(authentication);
            cartService.removeFromCartById(user.getId(), cartItemId);
            List<CartItem> items = cartService.getCartItems(user.getId());
            response.put("success", true);
            response.put("totalAmount", cartService.calculateTotal(items));
            response.put("cartCount", cartService.getCartCount(user.getId()));
            logger.debug("Removed from cart: userId={}, cartItemId={}", user.getId(), cartItemId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error removing from cart: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Ошибка при удалении из корзины");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam Long cartItemId,
            @RequestParam int increment,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Требуется авторизация");
            return ResponseEntity.status(401).body(response);
        }

        try {
            User user = getAuthenticatedUser(authentication);
            CartItem item = cartService.updateQuantity(user.getId(), cartItemId, increment);
            List<CartItem> items = cartService.getCartItems(user.getId());
            response.put("success", true);
            response.put("quantity", item != null ? item.getQuantity() : 0);
            response.put("totalAmount", cartService.calculateTotal(items));
            response.put("cartCount", cartService.getCartCount(user.getId()));
            if (item != null) {
                response.put("item", Map.of("price", item.getProduct().getPrice()));
            }
            logger.debug("Updated quantity: userId={}, cartItemId={}, increment={}", user.getId(), cartItemId, increment);
            return ResponseEntity.ok(response);
        } catch (InsufficientStockException e) {
            response.put("success", false);
            response.put("message", "Недостаточно товара на складе");
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error updating quantity: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Ошибка при обновлении количества");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping
    public String viewCart(Authentication authentication, Model model, RedirectAttributes attributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            attributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        try {
            User user = getAuthenticatedUser(authentication);
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            BigDecimal total = cartService.calculateTotal(cartItems);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", total);
            model.addAttribute("cartCount", cartService.getCartCount(user.getId()));
            logger.debug("Viewing cart for userId={}: {} items", user.getId(), cartItems.size());
            return "cart";
        } catch (Exception e) {
            logger.error("Error viewing cart: {}", e.getMessage());
            attributes.addFlashAttribute("error", "Ошибка при загрузке корзины");
            return "redirect:/";
        }
    }

    @PostMapping("/checkout")
    public String checkoutForm(@RequestParam List<Long> selectedIds, Authentication authentication, Model model, RedirectAttributes attributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            attributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        try {
            User user = getAuthenticatedUser(authentication);
            List<CartItem> selectedItems = cartService.getSelectedItems(user.getId(), selectedIds);
            if (selectedItems.isEmpty()) {
                attributes.addFlashAttribute("error", "Выберите хотя бы один товар");
                return "redirect:/cart";
            }
            BigDecimal total = cartService.calculateTotal(selectedItems);
            String deliveryDateTime = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            model.addAttribute("selectedItems", selectedItems);
            model.addAttribute("total", total);
            model.addAttribute("deliveryDateTime", deliveryDateTime);
            model.addAttribute("cartCount", cartService.getCartCount(user.getId()));
            logger.debug("Checkout form for userId={}: {} items selected", user.getId(), selectedItems.size());
            return "checkout";
        } catch (Exception e) {
            logger.error("Error preparing checkout: {}", e.getMessage());
            attributes.addFlashAttribute("error", "Ошибка при оформлении заказа");
            return "redirect:/cart";
        }
    }

    @PostMapping("/order/place")
    public String placeOrder(@RequestParam("selectedItems") List<Long> selectedItemIds,
                             @RequestParam("deliveryAddress") String deliveryAddress,
                             @RequestParam("phoneNumber") String phoneNumber,
                             RedirectAttributes attributes,
                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            attributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }
        User user = getAuthenticatedUser(authentication);
        try {


            List<CartItem> selectedItems = cartService.getSelectedItems(user.getId(), selectedItemIds);

            // Проверяем, что есть элементы
            if (selectedItems.isEmpty()) {
                attributes.addFlashAttribute("error", "Выберите хотя бы один товар");
                return "redirect:/cart";
            }

            // Создаём заказ через OrderService
            Order order = orderService.createOrder(user.getId(), selectedItems, deliveryAddress, phoneNumber);

            // Очищаем корзину
            cartService.clearCart(user.getId());

            logger.info("Order created successfully: orderId={}, userId={}", order.getId(), user.getId());
            attributes.addFlashAttribute("success", "Заказ успешно оформлен!");
            return "redirect:/orders";
        } catch (InsufficientFundsException e) {
            logger.error("Insufficient funds for userId={}: {}", user.getId(), e.getMessage());
            attributes.addFlashAttribute("error", "Недостаточно средств на балансе");
            return "redirect:/cart";
        } catch (InsufficientStockException e) {
            logger.error("Insufficient stock for userId={}: {}", user.getId(), e.getMessage());
            attributes.addFlashAttribute("error", "Недостаточно товара на складе");
            return "redirect:/cart";
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for userId={}: {}", user.getId(), e.getMessage());
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            logger.error("Error placing order for userId={}: {}", user.getId(), e.getMessage());
            attributes.addFlashAttribute("error", "Ошибка при оформлении заказа");
            return "redirect:/cart";
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Требуется авторизация");
        }
        return userService.getUserByEmail(authentication.getName());
    }
}