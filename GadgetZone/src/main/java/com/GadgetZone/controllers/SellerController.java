package com.GadgetZone.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class SellerController {

    @GetMapping("/dashboard")
    public String sellerDashboard(Model model) {
        model.addAttribute("section", "dashboard");
        return "seller-dashboard";
    }

}