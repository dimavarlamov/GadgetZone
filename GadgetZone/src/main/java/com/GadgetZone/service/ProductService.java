package com.GadgetZone.service;

import com.GadgetZone.dao.ProductRepository;
import com.GadgetZone.dao.OrderDAO;
import com.GadgetZone.domain.Product;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderDAO orderDAO;

    public ProductService(ProductRepository productRepository, OrderDAO orderDAO) {
        this.productRepository = productRepository;
        this.orderDAO = orderDAO;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void addProduct(@Validated Product product) {
        validateProduct(product);
        productRepository.save(product);
    }

    public void updateProduct(Product product) {
        if (product == null || product.getId() == 0) {
            throw new IllegalArgumentException("Неверный ID продукта.");
        }

        Product existingProduct = productRepository.findById(product.getId());
        if (existingProduct == null) {
            throw new IllegalArgumentException("Товар не найден!");
        }

        validateProduct(product);

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setCategoryId(product.getCategoryId());
        existingProduct.setSellerId(product.getSellerId());

        productRepository.save(existingProduct);
    }

    public void deleteProduct(int id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("Товар не найден!");
        }


        productRepository.delete(id);
    }

    public Product getProductById(int id) {
        return productRepository.findById(id);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название товара обязательно!");
        }
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Цена должна быть больше нуля!");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным!");
        }
        if (product.getImageUrl() == null || product.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Ссылка на изображение обязательна!");
        }
        if (orderDAO.isProductOrdered(product.getId())) {
            throw new IllegalArgumentException("Этот товар уже был заказан. Редактирование ограничено!");
        }
    }
}