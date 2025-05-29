package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.entity.Review;
import com.GadgetZone.entity.Role;
import com.GadgetZone.entity.User;
import com.GadgetZone.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
class BuyerProductController {
    private final ProductService productService;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/products/details/{productId}")
    public String viewProductDetailsBuyer(
            @PathVariable Long productId,
            Authentication authentication,
            Model model
    ) {
        Product product = productService.getProductById(productId);
        List<Review> reviews = reviewService.getReviewsByProductId(productId);

        Long userId = authentication != null && authentication.isAuthenticated()
                ? userService.getUserByEmail(authentication.getName()).getId()
                : null;
        boolean isFavorite = userId != null && favoriteService.isFavorite(userId, productId);
        boolean isInCart = userId != null && cartService.isInCart(userId, productId);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("isInCart", isInCart);
        model.addAttribute("isBuyer", authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BUYER")));
        model.addAttribute("newReview", new Review());

        return "product-details-buyer";
    }

    @PostMapping("/products/reviews/add")
    public String addReview(
            @RequestParam Long productId,
            @ModelAttribute Review review,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        User user = userService.getUserByEmail(authentication.getName());
        if (!user.getRole().equals(Role.ROLE_BUYER)) {
            redirectAttributes.addFlashAttribute("error", "Только покупатели могут оставлять отзывы");
            return "redirect:/products/details/" + productId;
        }

        review.setUserId(user.getId());
        review.setProductId(productId);
        reviewService.saveReview(review);
        redirectAttributes.addFlashAttribute("success", "Отзыв успешно добавлен");
        return "redirect:/products/details/" + productId;
    }

    @PostMapping("/products/cart/toggle")
    public String toggleCart(
            @RequestParam Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        Long userId = userService.getUserByEmail(authentication.getName()).getId();
        cartService.toggleCart(userId, productId);
        return "redirect:/products/details/" + productId;
    }

    @PostMapping("/products/favorites/toggle")
    public String toggleFavorite(
            @RequestParam Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Требуется авторизация");
            return "redirect:/login";
        }

        Long userId = userService.getUserByEmail(authentication.getName()).getId();
        favoriteService.toggleFavorite(userId, productId);
        return "redirect:/products/details/" + productId;
    }
}