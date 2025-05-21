package com.GadgetZone.controllers;

import com.GadgetZone.domain.dto.UserDTO;
import com.GadgetZone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDTO,
                               BindingResult result,
                               Model model) {
        if (!userDTO.getPassword().equals(userDTO.getMatchingPassword())) {
            result.rejectValue("matchingPassword", null, "Пароли не совпадают");
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            boolean success = userService.save(userDTO);
            if (!success) {
                model.addAttribute("error", "Ошибка при сохранении пользователя");
                return "register";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        return "redirect:/login";
    }
}