package com.GadgetZone.controllers;

import com.GadgetZone.domain.Cart;
import com.GadgetZone.domain.Product;
import com.GadgetZone.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final ProductService productService;

    public CartController(ProductService productService) {
        this.productService = productService;
    }

    // Получение корзины из сессии
    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    // Добавление товара в корзину
    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable int productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        Cart cart = getCart(session);
        Product product = productService.getProductById(productId);
        if (product != null) {
            cart.addItem(product, quantity);
        }
        return "redirect:/cart";
    }

    // Удаление товара из корзины
    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable int productId, HttpSession session) {
        Cart cart = getCart(session);
        cart.getItems().removeIf(item -> item.getProduct().getId() == productId);
        return "redirect:/cart";
    }

    // Очистка корзины
    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        Cart cart = getCart(session);
        cart.clear();
        return "redirect:/cart";
    }
    @Controller
    @RequestMapping("/order")
    public class OrderController {

        @PostMapping
        public String processOrder(HttpSession session) {
            Object user = session.getAttribute("user"); // или "currentUser", в зависимости от логики

            if (user == null) {
                return "redirect:/login";
            }

            // Здесь логика оформления заказа, если пользователь авторизован
            return "orderConfirmation"; // JSP-страница с подтверждением
        }
    }
    @GetMapping
    public String showCart(Model model, HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // перенаправляем на страницу входа
        }
        Cart cart = getCart(session);
        model.addAttribute("cartItems", cart.getItems());
        model.addAttribute("total", cart.getTotalPrice());
        return "cart";
    }

}
