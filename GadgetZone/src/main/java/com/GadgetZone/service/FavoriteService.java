package com.GadgetZone.service;

import com.GadgetZone.entity.Favorite;
import com.GadgetZone.entity.Product;
import com.GadgetZone.exceptions.ProductNotFoundException;
import com.GadgetZone.repository.FavoriteRepository;
import com.GadgetZone.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void toggleFavorite(Long userId, Long productId) {
        if (favoriteRepository.existsByUserAndProduct(userId, productId)) {
            favoriteRepository.deleteByUserAndProduct(userId, productId);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(ProductNotFoundException::new);
            favoriteRepository.save(new Favorite(userId, product.getId()));
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(favorite -> productRepository.findById(favorite.getProductId())
                        .orElseThrow(ProductNotFoundException::new))
                .collect(Collectors.toList());
    }
}
