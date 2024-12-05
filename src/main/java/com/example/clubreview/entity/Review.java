package com.example.clubreview.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateTime;

    //테스트용
    public Review(String comment, int rating, Club club, User user) {
        this.comment = comment;
        this.rating = rating;
        this.club = club;
        this.user = user;
    }
}
