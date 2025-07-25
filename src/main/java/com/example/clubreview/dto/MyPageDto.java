package com.example.clubreview.dto;


import com.example.clubreview.dto.review.ReviewDto;
import com.example.clubreview.dto.user.UserDto;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class MyPageDto {

    private final UserDto user;
    private final List<ReviewDto> reviews;
    private final int totalReviews;
    private final double averageRating;

    public static MyPageDto of(User user, List<Review> reviews) {
        List<ReviewDto> reviewDtos = reviews.stream()
                .map(ReviewDto::from)
                .collect(Collectors.toList());

        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return MyPageDto.builder()
                .user(UserDto.from(user))
                .reviews(reviewDtos)
                .totalReviews(reviews.size())
                .averageRating(avgRating)
                .build();
    }
}