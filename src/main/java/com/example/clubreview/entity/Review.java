package com.example.clubreview.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String comment;

    private int rating;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    //테스트용
    public Review(String comment, int rating, Club club, User user) {
        this.comment = comment;
        this.rating = rating;
        this.club = club;
        this.user = user;
    }
}
