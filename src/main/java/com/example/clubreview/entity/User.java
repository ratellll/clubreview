package com.example.clubreview.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String username;
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public enum Role{
        USER,ADMIN
    }

    //UserDetails 인터페이스 메서드 구현
    /*
     Collection 문법 확인하기
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role.name()); // ROLE_USER 또는 ROLE_ADMIN반환하는것
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 계정 만료 상태를 관리하지 않는 경우 true 반환
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정 잠금 상태를 관리하지 않는 경우 true 반환
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 자격 증명 만료 상태를 관리하지 않는 경우 true 반환
    }

    @Override
    public boolean isEnabled() {
        return true;  // 계정 활성화 상태를 관리하지 않는 경우 true 반환
    }
}
