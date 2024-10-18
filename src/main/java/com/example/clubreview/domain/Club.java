package com.example.clubreview.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    private String description;

    private double latitude;
    private double longitude;

    private double averageRating;

    // 클럽과 리뷰의 1:N 관계 설정
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();



}
