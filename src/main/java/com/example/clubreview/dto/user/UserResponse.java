package com.example.clubreview.dto.user;

import com.example.clubreview.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponse {
    private final Long id;
    private final String userName;
    private final String nickName;
    private final String phoneNumber;
    private final LocalDateTime createTime;
    private final LocalDateTime banEndTime;
    private final User.Role role;
    private final boolean banned;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .createTime(user.getCreateTime())
                .banEndTime(user.getBanEndTime())
                .role(user.getRole())
                .banned(user.isBanned())
                .build();
    }
}