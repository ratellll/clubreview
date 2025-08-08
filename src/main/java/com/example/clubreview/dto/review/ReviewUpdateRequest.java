package com.example.clubreview.dto.review;


import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Jacksonized
public class ReviewUpdateRequest {

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 100, message = "리뷰 내용은 100자를 초과할 수 없습니다.")
    private String comment;
}