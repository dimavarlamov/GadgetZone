package com.GadgetZone.controller;

import com.GadgetZone.domain.User;
import com.GadgetZone.service.FavoriteService;
import com.GadgetZone.domain.Product;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/toggle")
    public String toggleFavorite(@RequestParam int productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            favoriteService.toggleFavorite(user.getId(), productId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/products/" + productId;
    }

    @GetMapping
    public String viewFavorites(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            List<Product> favorites = favoriteService.getFavorites(user.getId());
            model.addAttribute("favorites", favorites);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "favorites";
    }
}
