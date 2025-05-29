package com.GadgetZone.service;

import com.GadgetZone.entity.CartItem;
import com.GadgetZone.entity.Product;
import com.GadgetZone.exceptions.InsufficientStockException;
import com.GadgetZone.exceptions.ProductNotFoundException;
import com.GadgetZone.repository.CartRepository;
import com.GadgetZone.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItem addToCart(Long userId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);

        if (product.getStock() < quantity) {
            throw new InsufficientStockException();
        }

        CartItem existingItem = cartRepository.findByUserId(userId).stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartRepository.save(existingItem);
            return existingItem;
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);
            return cartItem;
        }
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }


    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        return items.stream().map(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> {
                        log.error("Product not found for productId: {}", item.getProductId());
                        return new ProductNotFoundException();
                    });
            item.setProduct(product);
            log.debug("Loaded product for cart item: id={}, name={}", item.getId(), product.getName());
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    public BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(ProductNotFoundException::new);
                    return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void toggleCart(Long userId, Long productId) {
        if (cartRepository.existsByUserIdAndProductId(userId, productId)) {
            cartRepository.deleteByUserIdAndProductId(userId, productId);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(ProductNotFoundException::new);
            if (product.getStock() < 1) {
                throw new InsufficientStockException();
            }
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(1);
            cartRepository.save(cartItem);
        }
    }

    @Transactional
    public CartItem updateQuantity(Long userId, Long cartItemId, int increment) {
        CartItem item = cartRepository.findByUserId(userId).stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(ProductNotFoundException::new);

        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(ProductNotFoundException::new);

        int newQuantity = item.getQuantity() + increment;
        if (newQuantity < 0) {
            newQuantity = 0;
        }
        if (newQuantity > product.getStock()) {
            throw new InsufficientStockException();
        }

        if (newQuantity == 0) {
            cartRepository.deleteByUserIdAndProductId(userId, item.getProductId());
            return null;
        } else {
            item.setQuantity(newQuantity);
            cartRepository.save(item);
            item.setProduct(product);
            return item;
        }
    }

    public boolean isInCart(Long userId, Long productId) {
        return cartRepository.existsByUserIdAndProductId(userId, productId);
    }

    public int getCartCount(Long userId) {
        return cartRepository.findByUserId(userId).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Transactional
    public void removeFromCartById(Long userId, Long cartItemId) {
        CartItem cartItem = cartRepository.findByIdAndUserId(cartItemId, userId);
        if (cartItem == null) {
            throw new IllegalArgumentException("Элемент корзины не найден");
        }
        cartRepository.deleteByIdAndUserId(cartItemId, userId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getSelectedItems(Long userId, List<Long> selectedIds) {
        return cartRepository.findByUserId(userId).stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(ProductNotFoundException::new);
                    item.setProduct(product);
                    return item;
                })
                .collect(Collectors.toList());
    }
}