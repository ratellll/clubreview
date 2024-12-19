package com.example.clubreview.service;


import com.example.clubreview.dto.ReviewDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.exception.ReviewNotFoundException;
import com.example.clubreview.repository.ClubRepository;
import com.example.clubreview.repository.ReviewRepository;
import com.example.clubreview.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ClubRepository clubRepository;



    // 특정 클럽의 모든 리뷰 조회
    public List<Review> getReviewsByClubId(Long clubId) {
        return reviewRepository.findByClubId(clubId);
    }
    // 리뷰 ID로 특정 리뷰 조회
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰가 존재하지않습니다"));
    }

    // 리뷰 생성
    @Transactional
    public Review addReview(Long clubId, User user, int rating , String comment, LocalDateTime createTime) {
        if (reviewRepository.findByUserIdAndClubId(user.getId(), clubId).isPresent()) {
            throw new DuplicateReviewException("이미 해당 클럽에 리뷰를 남기셨습니다.");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new DuplicateReviewException("해당 클럽이 존재하지 않습니다"));

        Review review = new Review();
        review.setUser(user);
        review.setClub(club);
        review.setComment(comment);
        review.setRating(rating);
        review.setCreateTime(createTime);

        return reviewRepository.save(review);
    }

    //유저 리뷰 권한확인
    @Transactional
    public void userReviewAccess(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 리뷰입니다"));
        if (!review.getUser().getUsername().equals(username)) {
            throw new SecurityException("본인의 리뷰만 수정 및 삭제가 가능합니다.");
        }
    }

    //유저 리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, ReviewDto reviewDto,String currentUser) {
        Review review = findReviewOrThrow(reviewId);
        if (!review.getUser().getUsername().equals(currentUser)) {
            throw new SecurityException("본인의 리뷰만 수정 및 삭제가 가능합니다");
        }
        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        reviewRepository.save(review);
    }

    //유저 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다"));
        reviewRepository.delete(review);
    }

    // 어드민 리뷰 수정
    @Transactional
    public Review adminUpdateReview(Long reviewId, String comment, int rating) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        review.setComment(comment);
        review.setRating(rating);
        review.setUpdateTime(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // 어드민 리뷰 삭제
    @Transactional
    public void adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다"));
        reviewRepository.delete(review);
    }

    private Review findReviewOrThrow(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));
    }
}

