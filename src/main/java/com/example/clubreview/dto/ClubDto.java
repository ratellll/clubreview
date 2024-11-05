package com.example.clubreview.dto;

import com.example.clubreview.entity.Club;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {

    private Long id;
    private String name;
    private String location;
    private String description;
    private String callNumber;
    private double averageRating;

    //Entity 변환 메서드
    public Club toEntity() {
        return Club.builder()
                .id(this.id)
                .name(this.name)
                .location(this.location)
                .description(this.description)
                .callNumber(this.callNumber)
                .averageRating(this.averageRating)
                .build();
    }
}
