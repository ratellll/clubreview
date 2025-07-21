package com.example.clubreview.dto.config;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtResponse {

    private final String token;
    private final String username;
    private final String tokenType;
    private final LocalDateTime expiresAt;

    public static JwtResponse of(String token, String username, LocalDateTime expiresAt) {
        return JwtResponse.builder()
                .token(token)
                .username(username)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .build();
    }
}

