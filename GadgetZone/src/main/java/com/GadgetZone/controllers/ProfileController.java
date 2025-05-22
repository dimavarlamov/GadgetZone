package com.GadgetZone.controllers;

import com.GadgetZone.dao.UserRepository;
import com.GadgetZone.domain.Role;
import com.GadgetZone.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String name = authentication.getName();
        User user = userRepository.findByName(name);

        if (user.getRole() == Role.SELLER) {
            return "redirect:/seller/products/add";
        }

        model.addAttribute("user", user);
        return "profile";
    }
}