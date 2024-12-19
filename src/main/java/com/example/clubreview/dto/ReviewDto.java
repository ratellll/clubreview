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
    private Long userId;
    private Long clubId;
    private String clubName;


}
