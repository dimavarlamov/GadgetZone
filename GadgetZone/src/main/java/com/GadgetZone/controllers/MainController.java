package com.GadgetZone.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.GadgetZone.entity.Product;
import com.GadgetZone.service.ProductService;

@Controller
public class MainController {
    private final ProductService productService;

    public MainController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"", "/"})
    public String homePage(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "") String search) {
        int pageSize = 9;
        List<Product> products = productService.searchProducts(search, page, pageSize);
        model.addAttribute("products", products);
        model.addAttribute("searchQuery", search);
        return "index";
    }
}
