package com.example.clubreview.repository;

import com.example.clubreview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByClubIdAndUserId(Long clubId, Long userId);
}
