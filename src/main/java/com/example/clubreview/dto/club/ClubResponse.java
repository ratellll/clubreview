package com.example.clubreview.dto.club;


import com.example.clubreview.entity.Club;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubResponse {

    private final Long id;
    private final String name;
    private final String location;
    private final String description;
    private final String callNumber;
    private final double latitude;
    private final double longitude;
    private final double averageRating;
    private final String photoUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ClubResponse from(Club club) {
        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .location(club.getLocation())
                .description(club.getDescription())
                .callNumber(club.getCallNumber())
                .latitude(club.getLatitude())
                .longitude(club.getLongitude())
                .averageRating(club.getAverageRating())
                .photoUrl(club.getPhotoUrl())
                .createdAt(club.getCreatedAt())
                .updatedAt(club.getUpdatedAt())
                .build();
    }
}
