package com.GadgetZone.controllers;

import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.Product;
import com.GadgetZone.entity.User;
import com.GadgetZone.repository.CategoryRepository;
import com.GadgetZone.service.CartService;
import com.GadgetZone.service.FavoriteService;
import com.GadgetZone.service.ProductService;
import com.GadgetZone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping({"", "/"})
    public String homePage(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "") String search,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           Authentication authentication) {
        int pageSize = 9;
        List<Product> products;
        long total;

        // Получаем все категории для формы фильтров
        model.addAttribute("categories", categoryRepository.findAll());

        // Выполняем поиск с учетом фильтров
        if (!search.isEmpty() || category != null || minPrice != null || maxPrice != null) {
            products = productService.searchAdvanced(search, category, minPrice, maxPrice, page, pageSize);
            total = productService.countAdvanced(search, category, minPrice, maxPrice);
        } else {
            products = productService.searchProducts(search, page, pageSize);
            total = productService.countSearchResults(search);
        }

        // Инициализируем cartQuantities, cartCount и favoriteStatus
        Map<Long, Integer> cartQuantities = new HashMap<>();
        int cartCount = 0;
        Map<Long, Boolean> favoriteStatus = new HashMap<>();
        List<CartItem> cartItems = new ArrayList<>(); // Пустой список по умолчанию

        // Если пользователь авторизован, загружаем корзину и избранное
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.getUserByEmail(authentication.getName());
            Long userId = user.getId();
            cartItems = cartService.getCartItems(userId);
            for (CartItem item : cartItems) {
                cartQuantities.put(item.getProductId(), item.getQuantity());
            }
            cartCount = cartService.getCartCount(userId);
            // Заполняем favoriteStatus для продуктов
            for (Product product : products) {
                favoriteStatus.put(product.getId(), favoriteService.isFavorite(userId, product.getId()));
            }
        }

        // Добавляем атрибуты в модель
        model.addAttribute("products", products);
        model.addAttribute("cartItems", cartItems); // Добавляем cartItems в модель
        model.addAttribute("cartQuantities", cartQuantities);
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("favoriteStatus", favoriteStatus);
        model.addAttribute("searchQuery", search);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", (int) Math.ceil((double) total / pageSize));

        return "index";
    }
}