package com.GadgetZone.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}