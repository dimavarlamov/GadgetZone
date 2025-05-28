package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.ProductInUseException;
import com.GadgetZone.repository.CategoryRepository;
import com.GadgetZone.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/seller/products")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    // Обновлённый метод для отображения страницы с поиском и пагинацией
    @GetMapping
    public String showHomePage(@RequestParam(name = "search", required = false) String search,
                               @RequestParam(name = "page", defaultValue = "1") int page,
                               Model model) {
        int pageSize = 9;
        List<Product> products = productService.searchProducts(search, page - 1, pageSize);
        long total = productService.countSearchResults(search);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        return "home";
    }

    @GetMapping("/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "seller/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute Product product,
                             BindingResult result,
                             @AuthenticationPrincipal User user,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "seller/add-product";
        }
        product.setSellerId(user.getId());
        productService.createProduct(product);
        return "redirect:/seller/products";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal User user,
                                  Model model) {
        Product product = productService.getProductById(id);
        if (!product.getSellerId().equals(user.getId())) {
            throw new AccessDeniedException("Нет прав на редактирование");
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        return "seller/edit-product";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute Product product,
                                BindingResult result,
                                @AuthenticationPrincipal User user,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "seller/edit-product";
        }
        productService.updateProduct(id, product, user.getId());
        return "redirect:/seller/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return "redirect:/seller/products?success=true";
        } catch (ProductInUseException e) {
            return "redirect:/seller/products?error=" + e.getMessage();
        }
    }
}
