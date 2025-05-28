package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.entity.User;
import com.GadgetZone.repository.UserRepository;
import com.GadgetZone.service.FavoriteService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    @GetMapping
    public String viewFavorites(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        List<Product> favorites = favoriteService.getUserFavorites(user.getId());
        model.addAttribute("favorites", favorites);
        return "favorites"; // thymeleaf-шаблон favorites.html
    }

    @PostMapping("/toggle")
    public String toggleFavorite(@RequestParam Long productId,
                                 Authentication authentication,
                                 RedirectAttributes attributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        favoriteService.toggleFavorite(user.getId(), productId);
        attributes.addFlashAttribute("success", "Избранное обновлено");

        return "redirect:/favorites";
    }
}
