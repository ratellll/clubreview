package com.example.clubreview.dto.user;


import com.example.clubreview.entity.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Jacksonized
public class UserDto {

    private final Long id;

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상, 20자 이하로 입력해주세요.")
    private final String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상 입력해주세요.")
    private final String password;

    @NotBlank(message = "핸드폰 번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "핸드폰 번호 형식이 잘못되었습니다.")
    private final String phoneNumber;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[가-힣]+$", message = "한글만 입력 가능합니다.")
    @Size(min = 2, max = 6, message = "닉네임은 2자 이상, 6자 이하로 입력해주세요")
    private final String nickname;

    private final LocalDateTime createTime;
    private final LocalDateTime banEndTime;
    private final User.Role role;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .createTime(user.getCreateTime())
                .banEndTime(user.getBanEndTime())
                .role(user.getRole())
                .build();
    }

    public User toEntity(String encodedPassword) {
        return User.builder()
                .username(this.username)
                .password(encodedPassword)
                .nickname(this.nickname)
                .phoneNumber(this.phoneNumber)
                .role(this.role != null ? this.role : User.Role.USER)
                .build();
    }
}
