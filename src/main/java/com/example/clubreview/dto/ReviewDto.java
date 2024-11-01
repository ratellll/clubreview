package com.example.clubreview.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private String comment;
    private int rating;
    private String userId;

}
