package com.GadgetZone.controllers;

import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.InsufficientStockException;
import com.GadgetZone.service.CartService;
import com.GadgetZone.repository.UserRepository;

import com.GadgetZone.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final FavoriteService favoriteService;

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @AuthenticationPrincipal User user,
                            RedirectAttributes attributes) {
        try {
            cartService.addToCart(user.getId(), productId, quantity);
            attributes.addFlashAttribute("success", "Товар добавлен в корзину");
        } catch (InsufficientStockException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 @AuthenticationPrincipal User user,
                                 RedirectAttributes attributes) {
        cartService.removeFromCart(user.getId(), productId);
        attributes.addFlashAttribute("success", "Товар удален из корзины");
        return "redirect:/cart";
    }

    @PostMapping("/favorite/{productId}")
    public String toggleFavorite(@PathVariable Long productId,
                                 @AuthenticationPrincipal User user,
                                 RedirectAttributes attributes) {
        favoriteService.toggleFavorite(user.getId(), productId);
        attributes.addFlashAttribute("success", "Избранное обновлено");
        return "redirect:/products/" + productId;
    }
}