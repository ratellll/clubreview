package com.example.clubreview.dto.review;


import com.example.clubreview.entity.Review;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonDeserialize(builder = ReviewDto.ReviewDtoBuilder.class)
public class ReviewDto {

    private final Long id;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 10, max = 500, message = "리뷰 내용은 10자 이상 500자 이하여야 합니다.")
    private final String comment;

    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private final int rating;

    private final Long userId;
    private final Long clubId;
    private final String clubName;
    private final String userNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ReviewDto from(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(review.getRating())
                .userId(review.getUser().getId())
                .clubId(review.getClub().getId())
                .clubName(review.getClub().getName())
                .userNickname(review.getUser().getNickname())
                .createdAt(review.getCreateTime())
                .updatedAt(review.getUpdateTime())
                .build();
    }
}
