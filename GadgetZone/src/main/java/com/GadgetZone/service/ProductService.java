package com.GadgetZone.service;

import com.GadgetZone.dao.ProductRepository;
import com.GadgetZone.domain.Product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void addProduct(@Validated Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название товара обязательно!");
        }
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Цена должна быть больше нуля!");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным!");
        }
        if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
            throw new IllegalArgumentException("Ссылка на изображение обязательна!");
        }

        productRepository.addProduct(product);
    }
}