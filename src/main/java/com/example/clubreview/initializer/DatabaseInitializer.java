package com.example.clubreview.initializer;

import com.example.clubreview.entity.Club;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.repository.ClubRepository;
import com.example.clubreview.repository.ReviewRepository;
import com.example.clubreview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Configuration
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ReviewRepository reviewRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, ClubRepository clubRepository,
                               ReviewRepository reviewRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // 유저 생성
            User user1 = new User("유저1", passwordEncoder.encode("123"), User.Role.USER);
            User user2 = new User("유저2", passwordEncoder.encode("123"), User.Role.USER);
            User user3 = new User("유저3", passwordEncoder.encode("123"), User.Role.USER);
            userRepository.saveAll(List.of(user1, user2, user3));

            // 클럽 데이터 생성
            List<Club> clubs = List.of(
                    new Club("Octagon", "서울 강남구", "강남에 위치한 인기 클럽 Octagon", "010-1000-1001", 4.6),
                    new Club("M2", "서울 마포구", "홍대의 클럽 M2", "010-1000-1002", 4.2),
                    new Club("Arena", "서울 강남구", "강남에서 유명한 클럽 Arena", "010-1000-1003", 4.5),
                    new Club("Cakeshop", "서울 용산구", "이태원 클럽 Cakeshop", "010-1000-1004", 4.1),
                    new Club("Club Made", "서울 마포구", "홍대의 힙한 클럽 Made", "010-1000-1005", 4.3),
                    new Club("NB2", "서울 마포구", "홍대의 유명한 힙합 클럽 NB2", "010-1000-1006", 4.4),
                    new Club("FAUST", "서울 중구", "을지로에 위치한 클럽 FAUST", "010-1000-1007", 3.8),
                    new Club("SOAP", "서울 용산구", "이태원의 클럽 SOAP", "010-1000-1008", 4.0),
                    new Club("Modeci", "서울 강남구", "럭셔리 클럽 Modeci", "010-1000-1009", 4.2),
                    new Club("Mass", "서울 강남구", "강남의 대형 클럽 Mass", "010-1000-1010", 4.0),
                    new Club("Chroma", "서울 송파구", "잠실에 위치한 대형 클럽 Chroma", "010-1000-1011", 4.7),
                    new Club("Move", "서울 강남구", "음악이 좋은 클럽 Move", "010-1000-1012", 3.9),
                    new Club("The Henz Club", "서울 마포구", "힙합 분위기의 클럽 The Henz Club", "010-1000-1013", 4.1),
                    new Club("Hidden Cellar", "서울 용산구", "이태원의 Hidden Cellar", "010-1000-1014", 3.7),
                    new Club("Vurt", "서울 마포구", "홍대의 작은 클럽 Vurt", "010-1000-1015", 4.0)
            );
            clubRepository.saveAll(clubs);

            // 각 클럽에 대한 리뷰 3개씩 생성
            for (Club club : clubs) {
                Review review1 = new Review("좋은 경험이었습니다!", 5, club, user1);
                Review review2 = new Review("음악이 좋아요!", 4, club, user2);
                Review review3 = new Review("조금 혼잡했어요.", 3, club, user3);
                reviewRepository.saveAll(List.of(review1, review2, review3));
            }
        };
    }
}