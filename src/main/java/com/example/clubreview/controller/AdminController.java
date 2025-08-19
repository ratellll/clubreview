package com.example.clubreview.controller;


import com.example.clubreview.dto.ApiResponse;
import com.example.clubreview.dto.review.AdminReviewResponse;
import com.example.clubreview.dto.user.UserResponse;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.service.ReviewService;
import com.example.clubreview.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUserList() {
        try {
            List<User> users = userService.findAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 성공", userResponses));
        } catch (Exception e) {
            log.error("사용자 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam @NotBlank @Size(min = 1, max = 50) String nickname) {
        try {
            List<User> users = userService.searchUsersByNickname(nickname);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("사용자 검색 성공", userResponses));
        } catch (Exception e) {
            log.error("사용자 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 검색 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<AdminReviewResponse>>> getAllReviews(@RequestParam(required = false) String authorNickname) {
        try {
            List<Review> reviews = reviewService.getAllReviews(authorNickname);
            List<AdminReviewResponse> reviewResponses = reviews.stream()
                    .map(AdminReviewResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("모든 리뷰 조회 성공", reviewResponses));
        } catch (Exception e) {
            log.error("모든 리뷰 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("모든 리뷰 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(
            @PathVariable @Positive Long id,
            @RequestParam @Min(1) @Max(365) int days) {

        log.info("사용자 정지 요청: {} ({}일)", id, days);

        try {
            User bannedUser = userService.banUser(id, days);
            UserResponse response = UserResponse.from(bannedUser);

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("사용자가 %d일 동안 정지되었습니다.", days), response));
        } catch (EntityNotFoundException e) {
            log.warn("사용자 정지 실패 - 사용자 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정지 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 정지 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<UserResponse>> unbanUser(@PathVariable @Positive Long id) {
        log.info("사용자 정지 해제 요청: {}", id);

        try {
            User unbannedUser = userService.unbanUser(id);
            UserResponse response = UserResponse.from(unbannedUser);

            return ResponseEntity.ok(ApiResponse.success("사용자 정지가 해제되었습니다.", response));
        } catch (EntityNotFoundException e) {
            log.warn("사용자 정지 해제 실패 - 사용자 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정지 해제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 정지 해제 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable @Positive Long id) {
        log.info("사용자 삭제 요청: {}", id);

        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("사용자가 삭제되었습니다."));
        } catch (EntityNotFoundException e) {
            log.warn("사용자 삭제 실패 - 사용자 없음: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 삭제 중 오류가 발생했습니다."));
        }
    }
}