package com.example.clubreview.dto.club;

import com.example.clubreview.entity.Club;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Jacksonized
public class ClubDto {

    private final Long id;

    @NotBlank(message = "클럽 이름은 필수입니다.")
    private final String name;

    @NotBlank(message = "주소는 필수입니다.")
    private final String location;

    private final String description;
    private final String callNumber;

    @DecimalMin(value = "-90.0", message = "위도는 -90에서 90 사이여야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 -90에서 90 사이여야 합니다.")
    private final double latitude;

    @DecimalMin(value = "-180.0", message = "경도는 -180에서 180 사이여야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 -180에서 180 사이여야 합니다.")
    private final double longitude;

    private final String photoUrl;
    private final double averageRating;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    public static ClubDto from(Club club) {
        return ClubDto.builder()
                .id(club.getId())
                .name(club.getName())
                .location(club.getLocation())
                .description(club.getDescription())
                .callNumber(club.getCallNumber())
                .latitude(club.getLatitude())
                .longitude(club.getLongitude())
                .photoUrl(club.getPhotoUrl())
                .averageRating(club.getAverageRating())
                .createTime(club.getCreateTime())
                .updateTime(club.getUpdateTime())
                .build();
    }

    public Club toEntity() {
        return Club.builder()
                .name(this.name)
                .location(this.location)
                .description(this.description)
                .callNumber(this.callNumber)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .photoUrl(this.photoUrl)
                .averageRating(this.averageRating)
                .build();
    }
}

