package com.GadgetZone.controllers;

import com.GadgetZone.domain.Cart;
import com.GadgetZone.domain.CartItem;
import com.GadgetZone.domain.Product;
import com.GadgetZone.domain.User;
import com.GadgetZone.service.CartService;
import com.GadgetZone.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Добавление товара
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable int productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            cartService.addItem(user.getId().intValue(), productId);
        }
        return "redirect:/cart";
    }

    // Удаление товара
    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable int productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            cartService.removeItem(user.getId().intValue(), productId);
        }
        return "redirect:/cart";
    }

    // Очистка корзины
    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            cartService.clearCart(user.getId().intValue());
        }
        return "redirect:/cart";
    }

    // Показ корзины
    @GetMapping
    public String showCart(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItem> items = cartService.getCartItems(user.getId().intValue());
        double total = cartService.getTotalAmount(user.getId().intValue());

        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        return "cart";
    }
}