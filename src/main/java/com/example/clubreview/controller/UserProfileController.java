package com.example.clubreview.controller;

import com.example.clubreview.dto.MyPageDto;
import com.example.clubreview.dto.ApiResponse;
import com.example.clubreview.dto.review.ReviewResponse;
import com.example.clubreview.dto.review.ReviewStatistics;
import com.example.clubreview.dto.user.UserResponse;
import com.example.clubreview.dto.user.UserUpdateRequest;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.security.CustomUserDetails;
import com.example.clubreview.service.MyPageService;
import com.example.clubreview.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserProfileController {

    private final MyPageService myPageService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPageDto>> getMyPage(Authentication authentication) {
        try {
            MyPageDto myPageData = myPageService.getMyPageData(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("마이페이지 조회 성공", myPageData));
        } catch (EntityNotFoundException e) {
            log.warn("마이페이지 조회 실패 - 사용자 없음: {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("마이페이지 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("마이페이지 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            List<Review> reviews = reviewService.getUserReviews(userDetails.getUser().getId());

            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(ReviewResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("내 리뷰 목록 조회 성공", reviewResponses));
        } catch (Exception e) {
            log.error("내 리뷰 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/reviews/statistics")
    public ResponseEntity<ApiResponse<ReviewStatistics>> getMyReviewStatistics(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            ReviewStatistics statistics = reviewService.getUserReviewStatistics(userDetails.getUser().getId());

            return ResponseEntity.ok(ApiResponse.success("리뷰 통계 조회 성공", statistics));
        } catch (Exception e) {
            log.error("리뷰 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("리뷰 통계 조회 중 오류가 발생했습니다."));
        }
    }

    // ⭐ 수정: reviewService에 메서드 추가하여 해결
    @GetMapping("/reviews/period")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviewsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            List<Review> reviews = reviewService.getUserReviewsByDateRange(
                    userDetails.getUser().getId(), startDate, endDate);

            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(ReviewResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("기간별 리뷰 조회 성공", reviewResponses));
        } catch (Exception e) {
            log.error("기간별 리뷰 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("기간별 리뷰 조회 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/nickName")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickName(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        log.info("닉네임 변경 요청: {}", authentication.getName());

        try {
            User updatedUser = myPageService.updateNickName(authentication.getName(), request.getNickName());
            UserResponse response = UserResponse.from(updatedUser);

            return ResponseEntity.ok(ApiResponse.success("닉네임이 변경되었습니다.", response));
        } catch (DataIntegrityViolationException e) {
            log.warn("닉네임 변경 실패 - 중복: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("이미 사용 중인 닉네임입니다."));
        } catch (IllegalArgumentException e) {
            log.warn("닉네임 변경 실패 - 유효성 검사: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("닉네임 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("닉네임 변경 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        log.info("비밀번호 변경 요청: {}", authentication.getName());

        try {
            myPageService.updatePassword(authentication.getName(), request.getPassword());
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패 - 유효성 검사: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("비밀번호 변경 중 오류가 발생했습니다."));
        }
    }
}