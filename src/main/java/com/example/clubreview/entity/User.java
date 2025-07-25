package com.example.clubreview.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true, length = 11)
    private String phoneNumber;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column
    private LocalDateTime banEndTime;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public enum Role {
        USER, ADMIN
    }

    // 비즈니스 메서드
    public User banUser(int days) {
        return this.toBuilder()
                .banEndTime(LocalDateTime.now().plusDays(days))
                .build();
    }

    public User unbanUser() {
        return this.toBuilder()
                .banEndTime(null)
                .build();
    }

    public User updateNickname(String newNickname) {
        return this.toBuilder()
                .nickname(newNickname)
                .build();
    }

    public User updatePassword(String newPassword) {
        return this.toBuilder()
                .password(newPassword)
                .build();
    }

    public boolean isBanned() {
        return banEndTime != null && banEndTime.isAfter(LocalDateTime.now());
    }
}