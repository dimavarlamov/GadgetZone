package com.GadgetZone.service;

import com.GadgetZone.entity.Review;
import com.GadgetZone.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public void createReview(Long userId, Long productId, int rating, String comment) {
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
    }
}