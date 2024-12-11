package com.example.clubreview.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String nickname;
    @Column(nullable = false, unique = true, length = 11)
    private String phoneNumber;
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

    public enum Role{
        USER,ADMIN
    }

    @PrePersist // 엔티티가 처음 저장되기 전에 실행되는 메서드
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }

    //테스트용 id없는 생성자
    public User(String username, String password,String phoneNumber, Role role,String nickname) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.nickname = nickname;
    }


}
