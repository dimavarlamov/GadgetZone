package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.entity.User;
import com.GadgetZone.repository.UserRepository;
import com.GadgetZone.service.FavoriteService;
import com.GadgetZone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    @GetMapping
    public String viewFavorites(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access to /favorites");
            return "redirect:/login";
        }

        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
            List<Product> favorites = favoriteService.getUserFavorites(user.getId());
            model.addAttribute("favorites", favorites);
            logger.debug("Loaded {} favorites for user: {}", favorites.size(), email);
            return "favorites";
        } catch (Exception e) {
            logger.error("Error loading favorites: {}", e.getMessage());
            model.addAttribute("error", "Ошибка при загрузке избранного");
            return "favorites";
        }
    }

    @PostMapping("/toggle")
    public String toggleFavorite(
            @RequestParam Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated attempt to toggle favorite for productId: {}", productId);
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        try {
            Long userId = userService.getUserByEmail(authentication.getName()).getId();
            favoriteService.toggleFavorite(userId, productId);
            redirectAttributes.addFlashAttribute("success", "Товар обновлен в избранном");
            logger.debug("Toggled favorite for userId: {}, productId: {}", userId, productId);
        } catch (Exception e) {
            logger.error("Error toggling favorite for productId {}: {}", productId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении избранного");
        }
        return "redirect:/" ;
    }
}