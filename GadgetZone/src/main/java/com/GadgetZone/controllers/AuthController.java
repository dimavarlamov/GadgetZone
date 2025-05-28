package com.GadgetZone.controllers;

import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.EmailExistsException;
import com.GadgetZone.service.EmailService;
import com.GadgetZone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final EmailService emailService;
    private final UserService userService;

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return "redirect:/auth/login?verified=true";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "unverified", required = false) String unverifiedEmail,
                        @RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (unverifiedEmail != null) {
            model.addAttribute("unverifiedEmail", unverifiedEmail);
            model.addAttribute("error",
                    "Аккаунт не подтвержден. Пожалуйста, проверьте почту или запросите новое письмо с подтверждением.");
        } else if (error != null) {
            if (!model.containsAttribute("error")) {
                model.addAttribute("error", "Неверное имя пользователя или пароль");
            }
        }
        return "login";
    }
}