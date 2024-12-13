package com.example.clubreview.service;


import com.example.clubreview.dto.MyPageDto;
import com.example.clubreview.dto.ReviewDto;
import com.example.clubreview.dto.UserDto;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.repository.ReviewRepository;
import com.example.clubreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;


    public MyPageDto getMyPageData(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<ReviewDto> reviewDtos = reviewRepository.findByUserId(user.getId()).stream()
                .map(review -> ReviewDto.builder()
                        .id(review.getId())
                        .comment(review.getComment())
                        .rating(review.getRating())
                        .clubId(review.getClub().getId().toString())
                        .userId(user.getId().toString())
                        .clubName(review.getClub().getName())
                        .build())
                .collect(Collectors.toList());

        UserDto userDto = UserDto.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .nickname(user.getNickname())
                .build();

        return MyPageDto.builder()
                .user(userDto)
                .reviews(reviewDtos)
                .build();
    }
    //닉네임수정
    public void updateNickName(String username, String newNickname) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.getNickname().equals(newNickname)) {
            if (userRepository.findByNickname(user.getUsername()).isPresent()) {
                throw new IllegalArgumentException("이미 존재하는 닉네임 입니다.");
            }
            user.setNickname(newNickname);
        }
        userRepository.save(user);
    }
    //비밀번호 수정
    public void updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

}
