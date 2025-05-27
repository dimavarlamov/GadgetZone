package com.GadgetZone.controllers;

import com.GadgetZone.service.StatisticsService;
import com.GadgetZone.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;
    private final StatisticsService statisticsService;

    public AdminController(UserService userService, StatisticsService statisticsService) {
        this.userService = userService;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{userId}/balance")
    public String updateBalance(@PathVariable Long userId,
                                @RequestParam BigDecimal amount,
                                RedirectAttributes attributes) {
        userService.updateUserBalance(userId, amount);
        attributes.addFlashAttribute("success", "Баланс успешно обновлен");
        return "redirect:/admin/users";
    }

    @GetMapping("/statistics")
    public String salesStatistics(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                  Model model) {
        if (start == null) start = LocalDate.now().minusDays(7);
        if (end == null) end = LocalDate.now();

        model.addAttribute("report", statisticsService.generateSalesReport(start, end));
        return "admin/statistics";
    }
}
