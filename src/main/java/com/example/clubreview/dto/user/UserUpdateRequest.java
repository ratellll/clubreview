package com.example.clubreview.dto.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Jacksonized
public class UserUpdateRequest {

    @Pattern(regexp = "^[가-힣]{2,5}$", message = "닉네임은 2자 이상 5자 이하의 한글이어야 합니다.")
    private final String nickName;

    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private final String password;
}
