package com.example.clubreview.dto;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewDto {

    private String comment;
    private int rating;
    private String userId;

}
