package com.example.clubreview.dto.club;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonDeserialize(builder = ClubCreateRequest.ClubCreateRequestBuilder.class)
public class ClubCreateRequest {


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


}
