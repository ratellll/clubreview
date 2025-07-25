package com.example.clubreview.service;


import com.example.clubreview.dto.MyPageDto;
import com.example.clubreview.dto.review.ReviewDto;
import com.example.clubreview.dto.user.UserDto;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.repository.ReviewRepository;
import com.example.clubreview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MyPageService {

    private final ReviewService reviewService;
    private final UserService userService;

    public MyPageDto getMyPageData(String username) {
        log.info("마이페이지 데이터 조회: {}", username);

        User user = userService.findByUsername(username);
        List<Review> reviews = reviewService.getUserReviews(user.getId());

        List<ReviewDto> reviewDtos = reviews.stream()
                .map(ReviewDto::from)
                .collect(Collectors.toList());

        UserDto userDto = UserDto.from(user);

        return MyPageDto.builder()
                .user(userDto)
                .reviews(reviewDtos)
                .totalReviews(reviews.size())
                .averageRating(reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0))
                .build();
    }

    @Transactional
    public User updateNickName(String username, String newNickname) {
        return userService.updateNickname(username, newNickname);
    }

    @Transactional
    public User updatePassword(String username, String newPassword) {
        return userService.updatePassword(username, newPassword);
    }
}