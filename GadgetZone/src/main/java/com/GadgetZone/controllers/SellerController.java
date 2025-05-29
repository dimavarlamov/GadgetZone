package com.GadgetZone.controllers;

import com.GadgetZone.entity.User;
import com.GadgetZone.repository.CategoryRepository;
import com.GadgetZone.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasAuthority('SELLER')")
@RequiredArgsConstructor
public class SellerController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/dashboard")
    public String sellerDashboard() {
        return "seller-dashboard";
    }

    @GetMapping("/my-products/manage") // Новый URL
    public String manageProducts(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("products", productService.getProductsBySeller(user.getId()));
        return "manage-products";
    }

}