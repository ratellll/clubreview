package com.example.clubreview.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상, 20자 이하로 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상 입력해주세요.")
    private String password;

    @NotBlank(message = "핸드폰 번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "\\d{3}-\\d{3,4}-\\d{4}", message = "핸드폰 번호 형식이 잘못되었습니다.")
    private String phoneNumber;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[가-힣]+$", message = "한글만 입력 가능합니다.")
    @Size(min = 2, max = 6, message = "닉네임은 2자 이상, 5자 이하로 입력해주세요")
    private String nickname;

    private LocalDateTime createTime;
}
