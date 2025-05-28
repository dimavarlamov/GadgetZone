package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.repository.CategoryRepository;
import com.GadgetZone.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

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
                          @RequestParam(defaultValue = "1") int page,
                          Model model) {

        int pageSize = 9;
        List<Product> products = productService.searchAdvanced(
                query,
                category,
                minPrice,
                maxPrice,
                page - 1, // важно: в сервисе индексация с нуля
                pageSize
        );

        long total = productService.countAdvanced(query, category, minPrice, maxPrice);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        model.addAttribute("products", products);
        model.addAttribute("query", query);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "products";
    }
}
