package com.example.clubreview.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDto {


    private UserDto user;

    private List<ReviewDto> reviews;
}
