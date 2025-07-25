package com.example.clubreview.dto.review;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewStatistics {

    private final int totalReviews;
    private final double averageRating;
    private final Map<Integer, Long> ratingDistribution; // 평점별 리뷰 개수

    public static ReviewStatistics empty() {
        return ReviewStatistics.builder()
                .totalReviews(0)
                .averageRating(0.0)
                .ratingDistribution(new HashMap<>())
                .build();
    }

    // 가장 많이 준 평점
    public int getMostFrequentRating() {
        return ratingDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
    }

    // 5점 리뷰 비율
    public double getFiveStarPercentage() {
        if (totalReviews == 0) return 0.0;
        return (ratingDistribution.getOrDefault(5, 0L) * 100.0) / totalReviews;
    }
}