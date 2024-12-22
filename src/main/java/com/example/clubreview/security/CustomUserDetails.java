package com.example.clubreview.security;

import com.example.clubreview.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User 엔티티의 Role을 기반으로 ROLE_ 접두사를 붙여 GrantedAuthority를 반환합니다.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public String getNickname() {
        return user.getNickname();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 상태를 관리하지 않는 경우 true 반환
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (user.getBanEndTime() != null && user.getBanEndTime().isAfter(LocalDateTime.now())) {
            return false; // 계정이 잠겨있음
        }
        return true; // 계정이 정지되지 않음
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public User getUser() {
        return user;
    }
}