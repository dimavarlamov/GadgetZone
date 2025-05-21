package com.GadgetZone.controllers;

import com.GadgetZone.dao.UserRepository;
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

        String email = authentication.getName(); // email как логин
        User user = userRepository.findByEmail(email);

        model.addAttribute("user", user);
        return "profile";
    }
}
