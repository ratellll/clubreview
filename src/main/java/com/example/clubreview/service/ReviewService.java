package com.example.clubreview.service;


import com.example.clubreview.domain.Club;
import com.example.clubreview.domain.Review;
import com.example.clubreview.repository.ClubRepository;
import com.example.clubreview.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ClubRepository clubRepository;

    public ReviewService(ReviewRepository reviewRepository, ClubRepository clubRepository) {
        this.reviewRepository = reviewRepository;
        this.clubRepository = clubRepository;
    }

    public Review addReview(Long clubId, Long userId, Review review) {
        Optional<Review> existingReview = reviewRepository.findByClubIdAndUserId(clubId, userId);
        if (existingReview.isPresent()) {
            throw new IllegalStateException("이미 클럽에 대한 리뷰가 존재합니다.");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("해당 클럽은 존재하지않습니다."));

        review.setClub(club);
        Review savedReview = reviewRepository.save(review);

        double newAverageRating = club.getReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        club.setAverageRating(newAverageRating);
        clubRepository.save(club);

        return savedReview;
    }
}

