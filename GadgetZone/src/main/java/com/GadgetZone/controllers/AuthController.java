package com.GadgetZone.controllers;

import com.GadgetZone.service.EmailService;
import com.GadgetZone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



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
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "unverified", required = false) String email,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        if (success != null) {
            model.addAttribute("success", "Регистрация успешно завершена!");
        }
        if (email != null) {
            model.addAttribute("unverifiedEmail", email);
            model.addAttribute("error", "Аккаунт не подтвержден");
        }
        return "login";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam String email) {
        userService.resendVerificationEmail(email);
        return "redirect:/auth/login?resent";
    }

}

//    @PostMapping("/resend-verification")
//    public String resendVerification(@RequestParam("email") String email,
//                                   RedirectAttributes redirectAttributes) {
//        boolean sent = userService.resendVerificationEmail(email);
//        if (sent) {
//            redirectAttributes.addFlashAttribute("success",
//                "Новое письмо с подтверждением отправлено на вашу почту.");
//        } else {
//            redirectAttributes.addFlashAttribute("error",
//                "Не удалось отправить письмо. Пожалуйста, проверьте email или обратитесь в поддержку.");
//        }
//        return "redirect:/auth/login";
//    }