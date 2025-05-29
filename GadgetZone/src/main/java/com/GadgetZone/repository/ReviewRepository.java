package com.GadgetZone.repository;

import com.GadgetZone.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {
    private final JdbcTemplate jdbc;

    public void save(Review review) {
        String sql = "INSERT INTO reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, review.getUserId(), review.getProductId(), review.getRating(), review.getComment());
    }

    public List<Review> findByProductId(Long productId) {
        String sql = "SELECT * FROM reviews WHERE product_id = ?";
        return jdbc.query(sql, new ReviewRowMapper(), productId);
    }

    private static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Review review = new Review();
            review.setId(rs.getLong("id"));
            review.setUserId(rs.getLong("user_id"));
            review.setProductId(rs.getLong("product_id"));
            review.setRating(rs.getInt("rating"));
            review.setComment(rs.getString("comment"));
            return review;
        }
    }
}