package com.GadgetZone.domain;

import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.*;

@Data
@Builder
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
}