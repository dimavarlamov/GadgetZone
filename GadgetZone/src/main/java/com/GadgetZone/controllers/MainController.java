package com.GadgetZone.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.GadgetZone.entity.Product;
import com.GadgetZone.service.ProductService;
import org.springframework.web.bind.annotation.RequestParam;

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
        Page<Product> products = productService.searchProducts(search, PageRequest.of(page, 9));
        model.addAttribute("products", products);
        model.addAttribute("searchQuery", search);
        return "index";
    }
}