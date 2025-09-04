package com.example.clubreview.seed;

import com.example.clubreview.entity.Club;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.repository.ClubRepository;
import com.example.clubreview.repository.ReviewRepository;
import com.example.clubreview.repository.UserRepository;
import com.example.clubreview.service.ClubService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("dev")
@Order(2) // CsvClubSeeder 이후에 실행
@RequiredArgsConstructor
public class ReviewSeeder implements ApplicationRunner {

            private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ClubService clubService;
    private final BCryptPasswordEncoder passwordEncoder;

            private final Random random = new Random();

            // 다양한 리뷰 템플릿
            private final List<String> positiveReviews = List.of(
                        "분위기가 정말 좋아요! 음악도 최고였습니다.",
                        "직원들이 친절하고 서비스가 훌륭해요.",
                        "인테리어가 세련되고 음료도 맛있어요.",
                        "친구들과 즐거운 시간 보냈습니다.",
                        "음악이 좋고 춤추기 좋은 공간이에요.",
                        "깔끔하고 시설이 좋네요.",
                        "가격대비 만족스러운 경험이었어요.",
                        "다시 오고 싶은 곳입니다!",
                        "분위기 있고 데이트하기 좋아요.",
                        "음향시설이 정말 좋아요."
                        );

            private final List<String> neutralReviews = List.of(
                        "무난한 클럽이에요. 나쁘지 않네요.",
                        "평범한 수준입니다.",
                        "그럭저럭 괜찮은 것 같아요.",
                        "보통 수준의 클럽이네요.",
                        "한 번 정도는 가볼 만해요.",
                        "평소와 비슷한 분위기예요.",
                        "특별하지는 않지만 나쁘지 않아요.",
                        "적당한 가격에 적당한 서비스네요."
                        );

            private final List<String> negativeReviews = List.of(
                        "너무 시끄럽고 복잡해요.",
                        "가격이 비싼 편이네요.",
                        "서비스가 아쉬워요.",
                        "기대했던 것보다 별로였어요.",
                        "음악이 너무 시끄러워요.",
                        "직원 서비스가 불친절했어요.",
                        "시설이 좀 노후된 것 같아요.",
                        "다시 가고 싶지 않네요."
                        );

            private final List<String> nicknames = List.of(
                        "음악매니아", "댄스킹", "클럽러버", "파티걸", "비트마스터", "나이트오울",
                        "뮤직홀릭", "댄스퀸", "클럽고수", "파티남", "리듬감각", "밤의제왕",
                        "음향전문가", "춤신춤왕", "클럽탐험가", "파티피플", "비트박스", "나이트라이더",
                        "뮤직러버", "댄스머신", "클럽마니아", "파티타임", "사운드헌터", "밤샘족"
                        );

            @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
                if (reviewRepository.count() > 0) {
                        log.info("[ReviewSeeder] 이미 리뷰 데이터가 존재합니다. 시딩을 건너뜁니다.");
                        return;
                    }

                        log.info("[ReviewSeeder] 리뷰 시딩 시작...");

                        // 테스트 사용자들 생성
                                createTestUsers();

                        // 각 클럽에 리뷰 생성
                                createReviewsForClubs();

                        log.info("[ReviewSeeder] 리뷰 시딩 완료");
            }

            private void createTestUsers() {
                List<User> users = new ArrayList<>();

                        for (int i = 0; i < 24; i++) {
                        String nickname = nicknames.get(i);
                        String username = "reviewer" + (i + 1);
                        String phone = String.format("010%08d", 10000000 + i);

                                User user = User.builder()
                                        .userName(username)
                                        .password(passwordEncoder.encode("password123"))
                                        .nickName(nickname)
                                        .phoneNumber(phone)
                                        .role(User.Role.USER)
                                        .build();

                                users.add(user);
                    }

                        userRepository.saveAll(users);
                log.info("[ReviewSeeder] {} 명의 테스트 사용자 생성 완료", users.size());
            }

            private void createReviewsForClubs() {
                List<Club> clubs = clubRepository.findAll();
                List<User> users = userRepository.findAll();

                        if (users.isEmpty()) {
                        log.warn("[ReviewSeeder] 사용자가 없어 리뷰를 생성할 수 없습니다.");
                        return;
                    }

                        List<Review> reviews = new ArrayList<>();

                        for (Club club : clubs) {
                        int reviewCount = 3 + random.nextInt(2); // 3~4개 리뷰

                                for (int i = 0; i < reviewCount; i++) {
                                User reviewer = users.get(random.nextInt(users.size()));
                                int rating = generateWeightedRating();
                                String comment = getRandomReviewComment(rating);
                                LocalDateTime createTime = LocalDateTime.now()
                                                .minusDays(random.nextInt(30))
                                                .minusHours(random.nextInt(24));

                                        Review review = Review.builder()
                                                .club(club)
                                                .user(reviewer)
                                                .rating(rating)
                                                .comment(comment)
                                                .createTime(createTime)
                                                .updateTime(createTime)
                                                .build();

                                        reviews.add(review);
                            }
                    }

                        reviewRepository.saveAll(reviews);
                log.info("[ReviewSeeder] {} 개 클럽에 대해 {} 개의 리뷰 생성 완료", clubs.size(), reviews.size());

                        // 각 클럽의 평균 평점 업데이트
                                updateClubAverageRatings(clubs);
            }

            private int generateWeightedRating() {
                // 가중치: 4-5점이 더 많이 나오도록 (70%), 1-3점은 적게 (30%)
                        double rand = random.nextDouble();
                if (rand < 0.4) return 5;        // 40%
                else if (rand < 0.7) return 4;   // 30%
                else if (rand < 0.85) return 3;  // 15%
                else if (rand < 0.95) return 2;  // 10%
                else return 1;                   // 5%
            }

            private String getRandomReviewComment(int rating) {
                return switch (rating) {
                        case 5, 4 -> positiveReviews.get(random.nextInt(positiveReviews.size()));
                        case 3 -> neutralReviews.get(random.nextInt(neutralReviews.size()));
                        case 2, 1 -> negativeReviews.get(random.nextInt(negativeReviews.size()));
                        default -> "좋은 경험이었습니다.";
                    };
            }

            private void updateClubAverageRatings(List<Club> clubs) {
                log.info("[ReviewSeeder] 클럽 평균 평점 업데이트 시작...");

                        for (Club club : clubs) {
                        clubService.updateClubAverageRating(club.getId());
                    }

                        log.info("[ReviewSeeder] 클럽 평균 평점 업데이트 완료");
            }
}