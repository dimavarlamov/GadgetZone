package com.GadgetZone.controllers;

import com.GadgetZone.entity.Role;
import com.GadgetZone.entity.User;
import com.GadgetZone.service.UserService;
import com.GadgetZone.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        model.addAttribute("user", user);

        if (user.getRole() == Role.ROLE_SELLER) {
            model.addAttribute("dashboardLink", "/seller/dashboard");
        } else if (user.getRole() == Role.ROLE_ADMIN) {
            model.addAttribute("dashboardLink", "/admin/dashboard");
        }

        return "profile";
    }
}