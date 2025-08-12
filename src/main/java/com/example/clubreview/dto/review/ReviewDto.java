package com.example.clubreview.dto.review;


import com.example.clubreview.entity.Review;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Jacksonized
public class ReviewDto {

    private final Long id;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 3, max = 100, message = "리뷰 내용은 3자 이상 100자 이하여야 합니다.")
    private final String comment;

    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private final int rating;

    private final Long userId;
    private final Long clubId;
    private final String clubName;
    private final String userNickName;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    public static ReviewDto from(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(review.getRating())
                .userId(review.getUser().getId())
                .clubId(review.getClub().getId())
                .clubName(review.getClub().getName())
                .userNickName(review.getUser().getNickName())
                .createTime(review.getCreateTime())
                .updateTime(review.getUpdateTime())
                .build();
    }
}
