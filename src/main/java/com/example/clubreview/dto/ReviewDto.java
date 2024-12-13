package com.example.clubreview.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private String comment;
    private int rating;
    private String userId;
    private String clubId;
    private String clubName;


}
