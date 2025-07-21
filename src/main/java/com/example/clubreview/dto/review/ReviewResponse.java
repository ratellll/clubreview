package com.example.clubreview.dto.review;

import com.example.clubreview.entity.Review;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewResponse {
    private final Long id;
    private final String comment;
    private final int rating;
    private final String userNickname;
    private final String clubName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(review.getRating())
                .userNickname(review.getUser().getNickname())
                .clubName(review.getClub().getName())
                .createdAt(review.getCreateTime())
                .updatedAt(review.getUpdateTime())
                .build();
    }
}