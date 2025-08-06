package com.example.clubreview.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Jacksonized
public class LoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private final String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private final String password;
}
