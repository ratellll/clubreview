package com.example.clubreview.controller;

import com.example.clubreview.dto.club.ClubCreateRequest;
import com.example.clubreview.dto.club.ClubDetailResponse;
import com.example.clubreview.dto.club.ClubDto;
import com.example.clubreview.dto.club.ClubResponse;
import com.example.clubreview.dto.config.ApiResponse;
import com.example.clubreview.dto.config.PagedResponse;
import com.example.clubreview.dto.review.ReviewResponse;
import com.example.clubreview.entity.Club;
import com.example.clubreview.entity.Review;
import com.example.clubreview.service.ClubService;
import com.example.clubreview.service.ReviewService;
import com.example.clubreview.utils.FileUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ClubController {

    private final ClubService clubService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ClubResponse>>> getClubs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(required = false) @Pattern(regexp = "name|rating") String sortBy) {
// 11
        try {
            Page<Club> clubs = Optional.ofNullable(sortBy)
                    .filter(sort -> sort.equals("name"))
                    .map(sort -> clubService.getClubsSortedByName(page, size))
                    .orElse(clubService.getClubsSortedByRating(page, size));

            Page<ClubResponse> clubResponses = clubs.map(ClubResponse::from);
            PagedResponse<ClubResponse> pagedResponse = PagedResponse.of(clubResponses);

            return ResponseEntity.ok(ApiResponse.success("클럽 목록 조회 성공", pagedResponse));

        } catch (Exception e) {
            log.error("클럽 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("클럽 목록 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClubDetailResponse>> getClub(
            @PathVariable @Positive Long id) {

        try {
            Club club = clubService.getClubByIdOrThrow(id);
            ClubDetailResponse response = ClubDetailResponse.from(club);

            return ResponseEntity.ok(ApiResponse.success("클럽 상세 조회 성공", response));

        } catch (EntityNotFoundException e) {
            log.warn("클럽 조회 실패 - 존재하지 않는 클럽: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("클럽 상세 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("클럽 상세 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getClubReviews(@PathVariable @Positive Long id) {
        try {
            List<Review> reviews = reviewService.getReviewsByClubId(id);
            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(ReviewResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("클럽 리뷰 목록 조회 성공", reviewResponses));
        } catch (Exception e) {
            log.error("클럽 리뷰 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("클럽 리뷰 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClubResponse>> createClub(
            @Valid @RequestPart("club") ClubCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        log.info("클럽 생성 요청: {}", request.getName());

        try {
            String photoUrl = null;
            if (file != null && !file.isEmpty()) {
                photoUrl = FileUtil.saveFile(file);
            }

            ClubDto clubDto = ClubDto.builder()
                    .name(request.getName())
                    .location(request.getLocation())
                    .description(request.getDescription())
                    .callNumber(request.getCallNumber())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .photoUrl(photoUrl)
                    .build();

            Club createdClub = clubService.addClub(clubDto);
            ClubResponse response = ClubResponse.from(createdClub);

            log.info("클럽 생성 완료: {}", createdClub.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("클럽이 성공적으로 생성되었습니다.", response));

        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("파일 업로드 중 오류가 발생했습니다."));
        } catch (Exception e) {
            log.error("클럽 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("클럽 생성 중 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClubResponse>> updateClub(
            @PathVariable @Positive Long id,
            @Valid @RequestPart("club") ClubCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        log.info("클럽 수정 요청: {}", id);

        try {
            Club existingClub = clubService.getClubByIdOrThrow(id);

            String photoUrl = existingClub.getPhotoUrl();
            if (file != null && !file.isEmpty()) {
                photoUrl = FileUtil.saveFile(file);
            }

            ClubDto clubDto = ClubDto.builder()
                    .name(request.getName())
                    .location(request.getLocation())
                    .description(request.getDescription())
                    .callNumber(request.getCallNumber())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .photoUrl(photoUrl)
                    .build();

            Club updatedClub = clubService.updateClub(id, clubDto);
            ClubResponse response = ClubResponse.from(updatedClub);

            log.info("클럽 수정 완료: {}", id);
            return ResponseEntity.ok(ApiResponse.success("클럽이 성공적으로 수정되었습니다.", response));

        } catch (EntityNotFoundException e) {
            log.warn("클럽 수정 실패 - 존재하지 않는 클럽: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("파일 업로드 중 오류가 발생했습니다."));
        } catch (Exception e) {
            log.error("클럽 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("클럽 수정 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClub(@PathVariable @Positive Long id) {
        log.info("클럽 삭제 요청: {}", id);

        try {
            clubService.deleteClub(id);

            log.info("클럽 삭제 완료: {}", id);
            return ResponseEntity.ok(ApiResponse.success("클럽이 성공적으로 삭제되었습니다."));

        } catch (EntityNotFoundException e) {
            log.warn("클럽 삭제 실패 - 존재하지 않는 클럽: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("클럽 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("클럽 삭제 중 오류가 발생했습니다."));
        }
    }
}
