package com.example.clubreview.dto.review;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Jacksonized
public class ReviewCreateRequest {


    private final Long clubId;

    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private final int rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 10, max = 500, message = "리뷰 내용은 10자 이상 500자 이하여야 합니다.")
    private final String comment;
}