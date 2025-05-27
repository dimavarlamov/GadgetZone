package com.GadgetZone.controllers;

import com.GadgetZone.repository.UserRepository;
import com.GadgetZone.entity.User;
import com.GadgetZone.entity.Product;
import com.GadgetZone.service.FavoriteService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Product> favorites = favoriteService.getUserFavorites(user.getId());
        model.addAttribute("favorites", favorites);
        return "favorites";
    }
}