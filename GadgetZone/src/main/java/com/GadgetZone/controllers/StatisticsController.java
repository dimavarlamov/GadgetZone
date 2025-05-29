package com.GadgetZone.controllers;

import com.GadgetZone.entity.Product;
import com.GadgetZone.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seller/statistics")
@PreAuthorize("hasAuthority('SELLER')")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public String showStatistics(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            Model model
    ) {
        // Устанавливаем период по умолчанию: последний месяц
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(1);

        // Если переданы параметры startDate и endDate, используем их
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate);
        }

        // Получаем отчет о продажах
        Map<String, Object> report = statisticsService.generateSalesReport(start, end);

        // Подготавливаем данные для графика
        Map<Product, Long> productSales = (Map<Product, Long>) report.get("productSales");
        List<String> productNames = new ArrayList<>();
        List<Long> productQuantities = new ArrayList<>();

        for (Map.Entry<Product, Long> entry : productSales.entrySet()) {
            productNames.add(entry.getKey().getName());
            productQuantities.add(entry.getValue());
        }

        // Передаем данные в модель
        model.addAttribute("totalRevenue", report.get("totalRevenue"));
        model.addAttribute("totalOrders", report.get("totalOrders"));
        model.addAttribute("productSales", productSales);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("productNames", productNames);
        model.addAttribute("productQuantities", productQuantities);

        return "seller-statistics";
    }
}