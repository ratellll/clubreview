package com.example.clubreview.dto.club;


import com.example.clubreview.dto.review.ReviewResponse;
import com.example.clubreview.entity.Club;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubDetailResponse {

    private final Long id;
    private final String name;
    private final String location;
    private final String description;
    private final String callNumber;
    private final double latitude;
    private final double longitude;
    private final double averageRating;
    private final String photoUrl;
    private final List<ReviewResponse> reviews;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    public static ClubDetailResponse from(Club club) {
        List<ReviewResponse> reviewResponses = club.getReviews().stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());

        return ClubDetailResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .location(club.getLocation())
                .description(club.getDescription())
                .callNumber(club.getCallNumber())
                .latitude(club.getLatitude())
                .longitude(club.getLongitude())
                .averageRating(club.getAverageRating())
                .photoUrl(club.getPhotoUrl())
                .reviews(reviewResponses)
                .createTime(club.getCreateTime())
                .updateTime(club.getUpdateTime())
                .build();
    }
}
