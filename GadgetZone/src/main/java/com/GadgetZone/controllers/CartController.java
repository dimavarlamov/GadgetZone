package com.GadgetZone.controllers;

import com.GadgetZone.domain.CartItem;
import com.GadgetZone.domain.User;
import com.GadgetZone.service.CartService;
import com.GadgetZone.dao.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    // Добавление товара
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable int productId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            if (user != null) {
                cartService.addItem(user.getId().intValue(), productId);
            }
        }
        return "redirect:/cart";
    }

    // Удаление товара
    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable int productId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            if (user != null) {
                cartService.removeItem(user.getId().intValue(), productId);
            }
        }
        return "redirect:/cart";
    }

    // Очистка корзины
    @GetMapping("/clear")
    public String clearCart(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            if (user != null) {
                cartService.clearCart(user.getId().intValue());
            }
        }
        return "redirect:/cart";
    }

    // Показ корзины
    @GetMapping
    public String showCart(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> items = cartService.getCartItems(user.getId().intValue());
        double total = cartService.getTotalAmount(user.getId().intValue());

        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        return "cart";
    }
}