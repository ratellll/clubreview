package com.example.clubreview.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {

    private Long id;
    private String name;
    private String location;
    private String description;
    private String callNumber;
    private double averageRating;
}
