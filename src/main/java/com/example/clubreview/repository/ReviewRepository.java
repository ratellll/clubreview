package com.example.clubreview.repository;

import com.example.clubreview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 특정 클럽의 모든 리뷰 조회
    List<Review> findByClubId(Long clubId);

    // 특정 유저가 특정 클럽에 남긴 리뷰 조회 (클럽당 하나의 리뷰만 허용)
    Optional<Review> findByUserIdAndClubId(Long userId, Long clubId);

    List<Review> findByUserId(Long userId);

}
