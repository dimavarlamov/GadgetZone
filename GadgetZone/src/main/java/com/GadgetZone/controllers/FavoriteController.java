package com.GadgetZone.controllers;

import com.GadgetZone.dao.UserRepository;
import com.GadgetZone.domain.User;
import com.GadgetZone.domain.Product;
import com.GadgetZone.service.FavoriteService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserRepository userRepository;

    public FavoriteController(FavoriteService favoriteService, UserRepository userRepository) {
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewFavorites(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        try {
            List<Product> favorites = favoriteService.getFavorites(user.getId());
            model.addAttribute("favorites", favorites);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "favorites";
    }
}