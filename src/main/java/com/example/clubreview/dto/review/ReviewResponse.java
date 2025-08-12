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
    private final String userNickName;
    private final String userName;
    private final String clubName;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(review.getRating())
                .userNickName(review.getUser().getNickName())
                .userName(review.getUser().getUserName())
                .clubName(review.getClub().getName())
                .createTime(review.getCreateTime())
                .updateTime(review.getUpdateTime())
                .build();
    }
}