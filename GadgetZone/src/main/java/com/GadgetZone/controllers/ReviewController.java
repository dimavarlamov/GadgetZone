package com.GadgetZone.controllers;

import com.GadgetZone.entity.User;
import com.GadgetZone.service.OrderService;
import com.GadgetZone.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final OrderService orderService;

    @PostMapping("/{productId}")
    public String submitReview(@PathVariable Long productId,
                               @RequestParam int rating,
                               @RequestParam(required = false) String comment,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes attributes) {
        if (!orderService.hasUserPurchasedProduct(user.getId(), productId)) {
            attributes.addFlashAttribute("error", "Для оставления отзыва нужно приобрести товар");
            return "redirect:/products/" + productId;
        }

        reviewService.createReview(user.getId(), productId, rating, comment);
        attributes.addFlashAttribute("success", "Отзыв успешно добавлен");
        return "redirect:/products/" + productId;
    }
}