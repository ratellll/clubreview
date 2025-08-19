package com.example.clubreview.dto.review;

import com.example.clubreview.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class AdminReviewResponse {
    private Long id;
    private String clubName;
    private String userNickName;
    private String comment;
    private Integer rating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

            public static AdminReviewResponse from(Review review) {
                return AdminReviewResponse.builder()
                                .id(review.getId())
                                .clubName(review.getClub().getName())
                                .userNickName(review.getUser().getNickName())
                                .comment(review.getComment())
                                .rating(review.getRating())
                                .createTime(review.getCreateTime())
                                .updateTime(review.getUpdateTime())
                                .build();
            }
}