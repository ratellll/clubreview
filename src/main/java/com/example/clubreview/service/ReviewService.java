package com.example.clubreview.service;


import com.example.clubreview.dto.review.ReviewDto;
import com.example.clubreview.dto.review.ReviewStatistics;
import com.example.clubreview.entity.Club;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.repository.ClubRepository;
import com.example.clubreview.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ClubService clubService;

    public List<Review> getReviewsByClubId(Long clubId) {
        log.debug("클럽 리뷰 조회: {}", clubId);
        return reviewRepository.findByClubIdWithUser(clubId);
    }

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));
    }

    // 사용자 ID로 자신이 작성한 리뷰만 조회
    public List<Review> getUserReviews(Long userId) {
        log.debug("사용자 리뷰 조회: {}", userId);
        return reviewRepository.findByUserIdWithClub(userId);
    }

    public List<Review> getUserReviewsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("사용자 기간별 리뷰 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        return reviewRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public List<Review> getUserReviewsByMinRating(Long userId, int minRating) {
        log.debug("사용자 평점별 리뷰 조회: userId={}, minRating={}", userId, minRating);
        return reviewRepository.findByUserIdAndMinRating(userId, minRating);
    }

    @Transactional
    public Review addReview(Long clubId, User user, int rating, String comment, LocalDateTime createTime) {
        log.info("리뷰 생성: 클럽 {}, 사용자 {}", clubId, user.getUsername());

        // 중복 리뷰 체크
        if (reviewRepository.findByUserIdAndClubId(user.getId(), clubId).isPresent()) {
            throw new DataIntegrityViolationException("이미 해당 클럽에 리뷰를 남기셨습니다.");
        }

        Club club = clubService.getClubByIdOrThrow(clubId);

        Review review = Review.create(club, user, comment, rating);
        Review savedReview = reviewRepository.save(review);

        // 클럽 평균 평점 업데이트
        clubService.updateClubAverageRating(clubId);

        log.info("리뷰 생성 완료: {}", savedReview.getId());
        return savedReview;
    }

    @Transactional
    public void userReviewAccess(Long reviewId, String username) {
        Review review = getReviewById(reviewId);
        if (!review.getUser().getUsername().equals(username)) {
            throw new SecurityException("본인의 리뷰만 수정 및 삭제가 가능합니다.");
        }
    }

    @Transactional
    public Review updateReview(Long reviewId, ReviewDto reviewDto, String currentUser) {
        log.info("리뷰 수정: {}", reviewId);

        Review review = getReviewById(reviewId);

        // 권한 체크
        if (!review.getUser().getUsername().equals(currentUser)) {
            throw new SecurityException("본인의 리뷰만 수정 및 삭제가 가능합니다.");
        }

        Review updatedReview = review.updateContent(reviewDto.getComment(), reviewDto.getRating());
        Review savedReview = reviewRepository.save(updatedReview);

        // 클럽 평균 평점 업데이트
        clubService.updateClubAverageRating(review.getClub().getId());

        log.info("리뷰 수정 완료: {}", savedReview.getId());
        return savedReview;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        log.info("리뷰 삭제: {}", reviewId);

        Review review = getReviewById(reviewId);
        Long clubId = review.getClub().getId();

        reviewRepository.delete(review);

        // 클럽 평균 평점 업데이트
        clubService.updateClubAverageRating(clubId);

        log.info("리뷰 삭제 완료: {}", reviewId);
    }

    @Transactional
    public Review adminUpdateReview(Long reviewId, String comment, int rating) {
        log.info("관리자 리뷰 수정: {}", reviewId);

        Review review = getReviewById(reviewId);
        Review updatedReview = review.updateContent(comment, rating);
        Review savedReview = reviewRepository.save(updatedReview);

        // 클럽 평균 평점 업데이트
        clubService.updateClubAverageRating(review.getClub().getId());

        log.info("관리자 리뷰 수정 완료: {}", savedReview.getId());
        return savedReview;
    }

    @Transactional
    public void adminDeleteReview(Long reviewId) {
        log.info("관리자 리뷰 삭제: {}", reviewId);

        Review review = getReviewById(reviewId);
        Long clubId = review.getClub().getId();

        reviewRepository.delete(review);

        // 클럽 평균 평점 업데이트
        clubService.updateClubAverageRating(clubId);

        log.info("관리자 리뷰 삭제 완료: {}", reviewId);
    }

    @Transactional
    public void deleteUserReviews(Long userId) {
        log.info("사용자 리뷰 일괄 삭제: {}", userId);

        List<Review> userReviews = reviewRepository.findByUserId(userId);
        Set<Long> clubIds = userReviews.stream()
                .map(review -> review.getClub().getId())
                .collect(Collectors.toSet());

        reviewRepository.deleteByUserId(userId);

        // 영향받는 클럽들의 평균 평점 업데이트
        clubIds.forEach(clubService::updateClubAverageRating);

        log.info("사용자 리뷰 일괄 삭제 완료: {} (영향받은 클럽: {}개)", userId, clubIds.size());
    }

    public ReviewStatistics getUserReviewStatistics(Long userId) {
        List<Review> userReviews = getUserReviews(userId);

        if (userReviews.isEmpty()) {
            return ReviewStatistics.empty();
        }

        double averageRating = userReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Map<Integer, Long> ratingDistribution = userReviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getRating,
                        Collectors.counting()
                ));

        return ReviewStatistics.builder()
                .totalReviews(userReviews.size())
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .build();
    }
}