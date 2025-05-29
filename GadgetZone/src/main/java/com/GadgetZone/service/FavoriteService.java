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
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favoriteRepository.save(favorite);
        }
    }

    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    public List<Favorite> getFavoritesByUserId(Long userId) {
        return favoriteRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Product> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(favorite -> productRepository.findById(favorite.getProductId())
                        .orElseThrow(ProductNotFoundException::new))
                .collect(Collectors.toList());
    }
}
