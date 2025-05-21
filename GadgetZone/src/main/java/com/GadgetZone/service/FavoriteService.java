package com.GadgetZone.service;

import com.GadgetZone.domain.Product;
import com.GadgetZone.repository.FavoriteRepository;

import java.sql.SQLException;
import java.util.List;

public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public void toggleFavorite(int userId, int productId) throws SQLException {
        if (favoriteRepository.isFavorite(userId, productId)) {
            favoriteRepository.removeFromFavorites(userId, productId);
        } else {
            favoriteRepository.addToFavorites(userId, productId);
        }
    }

    public boolean isFavorite(int userId, int productId) throws SQLException {
        return favoriteRepository.isFavorite(userId, productId);
    }

    public List<Product> getFavorites(int userId) throws SQLException {
        return favoriteRepository.getFavoritesByUserId(userId);
    }
}
