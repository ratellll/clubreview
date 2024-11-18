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
@Builder
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

    @Column(nullable = false)
    private double latitude; // 위도

    @Column(nullable = false)
    private double longitude; //경도

    // 클럽과 리뷰의 1:N 관계 설정
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();


    // 생성자 (id 제외) 테스트용
    public Club(String name, String location, String description, String callNumber, double averageRating,double latitude,double longitude) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.callNumber = callNumber;
        this.averageRating = averageRating;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    // 리뷰추가 메서드
    public void addReview(Review review) {
        review.setClub(this);
        reviews.add(review);
    }
    //리뷰삭제
    public void removeReview(Review review) {
        reviews.remove(review);
        review.setClub(null);
    }

}
