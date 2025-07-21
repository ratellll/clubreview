package com.example.clubreview.dto.review;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private String comment;
    private int rating;
    private Long userId;
    private Long clubId;
    private String clubName;


}
