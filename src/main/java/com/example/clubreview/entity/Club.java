package com.example.clubreview.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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

    private String description; // 클럽정보

    private String callNumber; // 해당클럽 전화번호

    private double averageRating;

    // 클럽과 리뷰의 1:N 관계 설정
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();



}
