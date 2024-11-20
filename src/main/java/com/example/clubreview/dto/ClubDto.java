package com.example.clubreview.dto;

import com.example.clubreview.entity.Club;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {

    @NotBlank(message = "클럽 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String location;

    private String description;

    private String callNumber;

    @DecimalMin(value = "-90.0", message = "위도는 -90에서 90 사이여야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 -90에서 90 사이여야 합니다.")
    private double latitude;

    @DecimalMin(value = "-180.0", message = "경도는 -180에서 180 사이여야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 -180에서 180 사이여야 합니다.")
    private double longitude;

    private String photoUrl;

    public Club toEntity() {
        return Club.builder()
                .name(this.name)
                .location(this.location)
                .description(this.description)
                .callNumber(this.callNumber)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .photoUrl(this.photoUrl)
                .build();
    }
}

