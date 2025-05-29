package com.GadgetZone.repository;

import com.GadgetZone.entity.Favorite;
import com.GadgetZone.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FavoriteRepository {
    private final JdbcTemplate jdbc;

    private static class FavoriteRowMapper implements RowMapper<Favorite> {
        @Override
        public Favorite mapRow(ResultSet rs, int rowNum) throws SQLException {
            Favorite favorite = new Favorite();
            favorite.setId(rs.getLong("id"));
            favorite.setUserId(rs.getLong("user_id"));
            favorite.setProductId(rs.getLong("product_id"));
            return favorite;
        }
    }

    public void save(Favorite favorite) {
        String sql = "INSERT INTO favorites (user_id, product_id) VALUES (?, ?)";
        jdbc.update(sql, favorite.getUserId(), favorite.getProductId());
    }

    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND product_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    public void deleteByUserIdAndProductId(Long userId, Long productId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND product_id = ?";
        jdbc.update(sql, userId, productId);
    }

    public List<Favorite> findByUserId(Long userId) {
        String sql = "SELECT * FROM favorites WHERE user_id = ?";
        return jdbc.query(sql, new FavoriteRowMapper(), userId);
    }

}
