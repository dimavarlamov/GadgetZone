package com.GadgetZone.service;

import com.GadgetZone.entity.Category;
import com.GadgetZone.entity.Product;

import com.GadgetZone.exceptions.ProductInUseException;
import com.GadgetZone.exceptions.ProductNotFoundException;
import com.GadgetZone.repository.OrderRepository;
import com.GadgetZone.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long productId, Product productDetails, Long sellerId) {
        Product product = getProductById(productId);
        if (!product.getSellerId().equals(sellerId)) {
            throw new AccessDeniedException("Not product owner");
        }
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());

        return productRepository.save(product);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.search(query, pageable);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        if (orderRepository.existsByProductId(productId)) {
            throw new ProductInUseException();
        }
        productRepository.delete(productId);
    }
    public Page<Product> searchAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.searchAdvanced(query, category, minPrice, maxPrice, pageable);
    }
}
