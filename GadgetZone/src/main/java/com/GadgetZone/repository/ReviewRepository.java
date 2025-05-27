package com.GadgetZone.repository;

import com.GadgetZone.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {
    private final JdbcTemplate jdbc;

    public void save(Review review) {
        String sql = """
            INSERT INTO reviews (user_id, product_id, rating, comment, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;
        jdbc.update(sql,
                review.getUserId(),
                review.getProductId(),
                review.getRating(),
                review.getComment(),
                LocalDateTime.now());
    }
}