package com.GadgetZone.controllers;

import com.GadgetZone.domain.Product;
import com.GadgetZone.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/seller/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Показать список товаров продавца
    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product-list"; // шаблон product-list.html
    }

    // Форма добавления товара
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product"; // шаблон add-product.html
    }

    // Обработка добавления товара
    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") @Valid Product product,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "add-product";
        }
        try {
            productService.addProduct(product);
            return "redirect:/seller/products";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "add-product";
        }
    }

    // Форма редактирования товара
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/seller/products";
        }
        model.addAttribute("product", product);
        return "edit-product"; // шаблон edit-product.html
    }

    // Обработка редактирования товара
    @PostMapping("/edit")
    public String updateProduct(@ModelAttribute("product") @Valid Product product,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "edit-product";
        }
        try {
            productService.updateProduct(product);
            return "redirect:/seller/products";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "edit-product";
        }
    }

    // Удаление товара
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return "redirect:/seller/products";
    }
}
