package com.GadgetZone.controllers;

import com.GadgetZone.entity.Order;
import com.GadgetZone.entity.User;
import com.GadgetZone.service.OrderService;
import com.GadgetZone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal User user, Model model) {
        User profile = userService.getUserById(user.getId());
        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        model.addAttribute("user", profile);
        model.addAttribute("orders", orders);
        return "profile";  // Имя шаблона (profile.html)
    }
}
