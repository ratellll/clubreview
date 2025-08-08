package com.example.clubreview.controller;

import com.example.clubreview.dto.ApiResponse;
import com.example.clubreview.dto.review.ReviewCreateRequest;
import com.example.clubreview.dto.review.ReviewResponse;
import com.example.clubreview.dto.review.ReviewUpdateRequest;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.security.CustomUserDetails;
import com.example.clubreview.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication) {

        log.info("리뷰 생성 요청: 클럽 {}, 사용자 {}", request.getClubId(), authentication.getName());

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            LocalDateTime createTime = LocalDateTime.now();

            Review review = reviewService.addReview(
                    request.getClubId(),
                    user,
                    request.getRating(),
                    request.getComment(),
                    createTime
            );

            ReviewResponse response = ReviewResponse.from(review);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("리뷰가 성공적으로 등록되었습니다.", response));

        } catch (DataIntegrityViolationException e) {
            log.warn("리뷰 생성 실패 - 중복: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            log.warn("리뷰 생성 실패 - 클럽 없음: {}", request.getClubId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 생성 중 오류가 발생했습니다."));
        }
    }

    // 사용자 리뷰 수정
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateUserReview(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ReviewUpdateRequest request,
            Authentication authentication) {

        log.info("리뷰 수정 요청: {}, 사용자 {}", id, authentication.getName());

        try {
            // 권한 체크
            reviewService.userReviewAccess(id, authentication.getName());

            // ReviewDto 생성
            com.example.clubreview.dto.review.ReviewDto reviewDto =
                    com.example.clubreview.dto.review.ReviewDto.builder()
                            .comment(request.getComment())
                            .rating(request.getRating())
                            .build();

            Review updatedReview = reviewService.updateReview(id, reviewDto, authentication.getName());
            ReviewResponse response = ReviewResponse.from(updatedReview);

            return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 수정되었습니다.", response));

        } catch (SecurityException e) {
            log.warn("리뷰 수정 실패 - 권한 없음: {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            log.warn("리뷰 수정 실패 - 리뷰 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 수정 중 오류가 발생했습니다."));
        }
    }

    // 사용자 리뷰 삭제
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteUserReview(
            @PathVariable @Positive Long id,
            Authentication authentication) {

        log.info("리뷰 삭제 요청: {}, 사용자 {}", id, authentication.getName());

        try {
            // 권한 체크
            reviewService.userReviewAccess(id, authentication.getName());

            reviewService.deleteReview(id);

            return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 삭제되었습니다."));

        } catch (SecurityException e) {
            log.warn("리뷰 삭제 실패 - 권한 없음: {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            log.warn("리뷰 삭제 실패 - 리뷰 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 삭제 중 오류가 발생했습니다."));
        }
    }

    // 관리자 리뷰 수정
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReviewByAdmin(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {

        log.info("관리자 리뷰 수정 요청: {}", id);

        try {
            Review updatedReview = reviewService.adminUpdateReview(id, request.getComment(), request.getRating());
            ReviewResponse response = ReviewResponse.from(updatedReview);

            return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 수정되었습니다.", response));

        } catch (EntityNotFoundException e) {
            log.warn("관리자 리뷰 수정 실패 - 리뷰 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 리뷰 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 수정 중 오류가 발생했습니다."));
        }
    }

    // 관리자 리뷰 삭제
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReviewByAdmin(@PathVariable @Positive Long id) {
        log.info("관리자 리뷰 삭제 요청: {}", id);

        try {
            reviewService.adminDeleteReview(id);
            return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 삭제되었습니다."));

        } catch (EntityNotFoundException e) {
            log.warn("관리자 리뷰 삭제 실패 - 리뷰 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("관리자 리뷰 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 삭제 중 오류가 발생했습니다."));
        }
    }

    // 리뷰 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable @Positive Long id) {
        try {
            Review review = reviewService.getReviewById(id);
            ReviewResponse response = ReviewResponse.from(review);

            return ResponseEntity.ok(ApiResponse.success("리뷰 조회 성공", response));

        } catch (EntityNotFoundException e) {
            log.warn("리뷰 조회 실패 - 리뷰 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("리뷰 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 조회 중 오류가 발생했습니다."));
        }
    }
}