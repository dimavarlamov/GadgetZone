package com.GadgetZone.service;

import com.GadgetZone.entity.Product;
import com.GadgetZone.exceptions.ProductInUseException;
import com.GadgetZone.exceptions.ProductNotFoundException;
import com.GadgetZone.repository.OrderRepository;
import com.GadgetZone.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Product createProduct(Product product) {
        logger.debug("Creating product: {}", product.getName());
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
        product.setCategory(productDetails.getCategory());
        if (productDetails.getImageUrl() != null) {
            product.setImageUrl(productDetails.getImageUrl());
        }
        Product savedProduct = productRepository.save(product);
        logger.debug("Updated product: id={}, name={}", savedProduct.getId(), savedProduct.getName());
        return savedProduct;
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsBySellerPaginated(Long sellerId, int page, int size) {
        return productRepository.findBySellerIdPaginated(sellerId, page, size);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProductsBySeller(String query, Long sellerId, int page, int size) {
        return productRepository.searchBySeller(query, sellerId, page, size);
    }

    @Transactional(readOnly = true)
    public long countSellerProducts(Long sellerId) {
        return productRepository.countBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public long countSearchResultsBySeller(String query, Long sellerId) {
        return productRepository.countSearchBySeller(query, sellerId);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query, int page, int size) {
        logger.debug("Searching products with query={}, page={}, size={}", query, page, size);
        List<Product> products = productRepository.search(query, page, size);
        logger.debug("Found {} products", products.size());
        return products;
    }

    @Transactional(readOnly = true)
    public long countSearchResults(String query) {
        return productRepository.countSearchResults(query);
    }

    @Transactional(readOnly = true)
    public List<Product> searchAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        logger.debug("Advanced search: query={}, category={}, minPrice={}, maxPrice={}, page={}, size={}",
                query, category, minPrice, maxPrice, page, size);
        List<Product> products = productRepository.searchAdvanced(query, category, minPrice, maxPrice, page, size);
        logger.debug("Found {} products", products.size());
        return products;
    }

    @Transactional(readOnly = true)
    public long countAdvanced(String query, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Counting advanced search results: query={}, category={}, minPrice={}, maxPrice={}",
                query, category, minPrice, maxPrice);
        long count = productRepository.countSearchAdvanced(query, category, minPrice, maxPrice);
        logger.debug("Total count: {}", count);
        return count;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        if (orderRepository.existsByProductId(productId)) {
            throw new ProductInUseException();
        }
        productRepository.delete(productId);
    }

    @Transactional
    public void updateStock(Long productId, int newStock, Long sellerId) {
        Product product = getProductById(productId);
        if (!product.getSellerId().equals(sellerId)) {
            throw new AccessDeniedException("Not product owner");
        }
        product.setStock(newStock);
        productRepository.save(product);
    }
}