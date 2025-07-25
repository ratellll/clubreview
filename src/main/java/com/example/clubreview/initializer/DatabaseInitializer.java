package com.example.clubreview.initializer;

import com.example.clubreview.entity.Club;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.repository.ClubRepository;
import com.example.clubreview.repository.ReviewRepository;
import com.example.clubreview.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ReviewRepository reviewRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initDatabase() {
        // 기존 데이터 삭제
        reviewRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();

        // 사용자 생성
        User user1 = User.builder()
                .username("user1")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("01012341235")
                .nickname("홍길동")
                .role(User.Role.USER)
                .build();

        User user2 = User.builder()
                .username("user2")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("01095743212")
                .nickname("바둑이")
                .role(User.Role.USER)
                .build();

        User user3 = User.builder()
                .username("user3")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("01098871623")
                .nickname("이방인")
                .role(User.Role.USER)
                .build();

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .phoneNumber("01073751673")
                .nickname("어드민")
                .role(User.Role.ADMIN)
                .build();

        userRepository.saveAll(List.of(user1, user2, user3, admin));

        // 클럽 생성
        List<Club> clubs = List.of(
                Club.builder()
                        .name("Octagon")
                        .location("서울 강남구")
                        .description("강남에 위치한 인기 클럽 Octagon")
                        .callNumber("010-1000-1001")
                        .latitude(37.504833)
                        .longitude(127.044176)
                        .photoUrl("/images/clubs/capybara.png")
                        .averageRating(4.6)
                        .build(),
                Club.builder()
                        .name("M2")
                        .location("서울 마포구")
                        .description("홍대의 클럽 M2")
                        .callNumber("010-1000-1002")
                        .latitude(37.555702)
                        .longitude(126.923612)
                        .photoUrl("/images/clubs/capybara.png")
                        .averageRating(4.2)
                        .build(),
                Club.builder()
                        .name("Arena")
                        .location("서울 강남구")
                        .description("강남에서 유명한 클럽 Arena")
                        .callNumber("010-1000-1003")
                        .latitude(37.510722)
                        .longitude(127.022393)
                        .photoUrl("/images/clubs/capybara.png")
                        .averageRating(4.5)
                        .build()
        );
        clubRepository.saveAll(clubs);

        // 리뷰 생성
        for (Club club : clubs) {
            Review review1 = Review.builder()
                    .comment("좋은 경험이었습니다!")
                    .rating(5)
                    .club(club)
                    .user(user1)
                    .build();

            Review review2 = Review.builder()
                    .comment("음악이 좋아요!")
                    .rating(4)
                    .club(club)
                    .user(user2)
                    .build();

            Review review3 = Review.builder()
                    .comment("조금 혼잡했어요.")
                    .rating(3)
                    .club(club)
                    .user(user3)
                    .build();

            reviewRepository.saveAll(List.of(review1, review2, review3));
        }
    }
}
