package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.entity.Review;
import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.ProductInUseException;
import com.GadgetZone.repository.CategoryRepository;
import com.GadgetZone.service.FileStorageService;
import com.GadgetZone.service.ProductService;
import com.GadgetZone.service.ReviewService;
import com.GadgetZone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/seller/my-products")
@PreAuthorize("hasAuthority('SELLER')")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    // Добавление товара
    @GetMapping("/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        return "add-product";
    }

    @PostMapping("/add")
    public String addProduct(
            @ModelAttribute @Valid Product product,
            BindingResult result,
            @RequestParam("image") MultipartFile imageFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.product", result);
            redirectAttributes.addFlashAttribute("product", product);
            redirectAttributes.addFlashAttribute("categories", categoryRepository.findAll());
            return "redirect:/seller/my-products/add";
        }

        try {
            if (imageFile.isEmpty()) {
                throw new IllegalArgumentException("Необходимо загрузить изображение");
            }
            String imagePath = fileStorageService.storeFile(imageFile);
            product.setImageUrl(imagePath);

            User user = getAuthenticatedUser(authentication);
            product.setSellerId(user.getId());
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("success", "Товар успешно добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/my-products";
    }

    // Редактирование товара
    @GetMapping("/edit/{id}")
    public String editProductForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = getAuthenticatedUser(authentication);
            Product product = productService.getProductById(id);
            checkProductOwnership(user, product);

            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepository.findAll());
            return "edit-product";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/my-products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute @Valid Product product,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.product", result);
            redirectAttributes.addFlashAttribute("product", product);
            redirectAttributes.addFlashAttribute("categories", categoryRepository.findAll());
            return "redirect:/seller/my-products/edit/" + id;
        }

        try {
            User user = getAuthenticatedUser(authentication);
            Product existingProduct = productService.getProductById(id);
            checkProductOwnership(user, existingProduct);

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileStorageService.storeFile(imageFile);
                product.setImageUrl(imageUrl);
            }

            productService.updateProduct(id, product, user.getId());
            redirectAttributes.addFlashAttribute("success", "Товар успешно обновлён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/my-products";
    }

    // Управление товарами
    @GetMapping
    public String showSellerProducts(
            Authentication authentication,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = getAuthenticatedUser(authentication);

            int pageSize = 9;
            List<Product> products;
            long total;

            if (search != null && !search.isEmpty()) {
                products = productService.searchProductsBySeller(search, user.getId(), page-1, pageSize);
                total = productService.countSearchResultsBySeller(search, user.getId());
            } else {
                products = productService.getProductsBySellerPaginated(user.getId(), page-1, pageSize);
                total = productService.countSellerProducts(user.getId());
            }

            model.addAttribute("products", products);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) total / pageSize));
            model.addAttribute("search", search);

            return "seller-products";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    // Другие методы
    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Товар успешно удалён");
        } catch (ProductInUseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/my-products";
    }

    @GetMapping("/management")
    public String manageProducts(
            @AuthenticationPrincipal User user,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("products", productService.getProductsBySeller(user.getId()));
            model.addAttribute("categories", categoryRepository.findAll());
            return "manage-products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/my-products";
        }
    }

    @PostMapping("/update-stock/{id}")
    public String updateStock(
            @PathVariable Long id,
            @RequestParam int stock,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = getAuthenticatedUser(authentication);
            productService.updateStock(id, stock, user.getId());
            redirectAttributes.addFlashAttribute("success", "Остатки успешно обновлены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/my-products/management";
    }

    @GetMapping("/details/{productId}")
    public String viewProductDetails(
            @PathVariable Long productId,
            Model model
    ) {
        Product product = productService.getProductById(productId);
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        return "product-details";
    }

    // Вспомогательные методы
    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Требуется авторизация");
        }
        return userService.getUserByEmail(authentication.getName());
    }

    private void checkProductOwnership(User user, Product product) {
        if (!product.getSellerId().equals(user.getId())) {
            throw new AccessDeniedException("Нет прав на редактирование этого товара");
        }
    }
}
