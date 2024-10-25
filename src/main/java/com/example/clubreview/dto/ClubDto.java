package com.example.clubreview.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClubDto {

    private Long id;
    private String name;
    private String location;
    private String description;
    private String callNumber;
    private double averageRating;
}
