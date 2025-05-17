package com.GadgetZone.controllers;

import com.GadgetZone.domain.Product;
import com.GadgetZone.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final ProductService productService;

    public CartController(ProductService productService) {
        this.productService = productService;
    }

    // Получение корзины из сессии
    private Map<Integer, Integer> getCart(HttpSession session) {
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    // Отображение содержимого корзины
    @GetMapping
    public String showCart(Model model, HttpSession session) {
        Map<Integer, Integer> cart = getCart(session);
        List<Product> products = new ArrayList<>();
        double total = 0.0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            if (product != null) {
                int quantity = entry.getValue();
                products.add(product);
                total += product.getPrice() * quantity;
            }
        }

        model.addAttribute("cartItems", products);
        model.addAttribute("quantities", cart);
        model.addAttribute("total", total);
        return "cart"; // шаблон cart.html
    }

    // Добавление товара в корзину
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable int productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        Map<Integer, Integer> cart = getCart(session);
        cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
        return "redirect:/cart";
    }

    // Удаление товара из корзины
    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable int productId, HttpSession session) {
        Map<Integer, Integer> cart = getCart(session);
        cart.remove(productId);
        return "redirect:/cart";
    }

    // Очистка всей корзины
    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        return "redirect:/cart";
    }
}
