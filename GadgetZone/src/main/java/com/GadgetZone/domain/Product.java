package com.GadgetZone.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class Product {
    private int id;

    @NotBlank(message = "Название товара обязательно!")
    private String name;

    private String description;

    @DecimalMin(value = "0.01", message = "Цена должна быть больше нуля!")
    private double price;

    @Min(value = 0, message = "Количество не может быть отрицательным!")
    private int stock;

    private int categoryId;
    private int sellerId;
    private String imageUrl;

    public Product(int id, String name, String description, double price, int stock, int categoryId, int sellerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.sellerId = sellerId;
    }
}