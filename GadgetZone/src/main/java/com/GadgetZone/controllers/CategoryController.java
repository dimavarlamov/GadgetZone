package com.GadgetZone.controllers;

import com.GadgetZone.entity.Category;
import com.GadgetZone.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasAuthority('SELLER')")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("newCategory", new Category()); // Добавьте эту строку
        return "manage-categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/seller/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return "redirect:/seller/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
        model.addAttribute("category", category);
        return "edit-category";
    }

    @PostMapping("/categories/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category) {
        category.setId(id);
        categoryRepository.save(category);
        return "redirect:/seller/categories";
    }
}