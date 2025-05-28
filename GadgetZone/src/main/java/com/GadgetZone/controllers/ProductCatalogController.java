package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.repository.CategoryRepository;
import com.GadgetZone.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/products")
    public String catalog(@RequestParam(required = false) String query,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {

        Page<Product> products = productService.searchAdvanced(
                query,
                category,
                minPrice,
                maxPrice,
                PageRequest.of(page, 9)
        );

        model.addAttribute("products", products);
        model.addAttribute("query", query);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("categories", categoryRepository.findAll());

        return "products";
    }
}
