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
            User user2 = new User("유저2", passwordEncoder.encode("123"),"010-1234-1235", User.Role.USER);
            User user3 = new User("유저3", passwordEncoder.encode("123"),"010-1234-1236", User.Role.USER);
            User user1 = new User("유저1", passwordEncoder.encode("123"),"010-1234-1234", User.Role.USER);
            User user4 = new User("123", passwordEncoder.encode("123"),"010-1234-1237", User.Role.ADMIN);
            userRepository.saveAll(List.of(user1, user2, user3,user4));

            // 클럽 데이터 생성
            List<Club> clubs = List.of(
                    new Club("Octagon", "서울 강남구", "강남에 위치한 인기 클럽 Octagon", "010-1000-1001", 4.6, 37.504833, 127.044176, "/images/clubs/capybara.png"),
                    new Club("M2", "서울 마포구", "홍대의 클럽 M2", "010-1000-1002", 4.2, 37.555702, 126.923612, "/images/clubs/capybara.png"),
                    new Club("Arena", "서울 강남구", "강남에서 유명한 클럽 Arena", "010-1000-1003", 4.5, 37.510722, 127.022393, "/images/clubs/capybara.png"),
                    new Club("Cakeshop", "서울 용산구", "이태원 클럽 Cakeshop", "010-1000-1004", 4.1, 37.534349, 126.994547, "/images/clubs/capybara.png"),
                    new Club("Club Made", "서울 마포구", "홍대의 힙한 클럽 Made", "010-1000-1005", 4.3, 37.555869, 126.922759, "/images/clubs/capybara.png"),
                    new Club("NB2", "서울 마포구", "홍대의 유명한 힙합 클럽 NB2", "010-1000-1006", 4.4, 37.556495, 126.923839, "/images/clubs/capybara.png"),
                    new Club("FAUST", "서울 중구", "을지로에 위치한 클럽 FAUST", "010-1000-1007", 3.8, 37.565328, 126.977822, "/images/clubs/capybara.png"),
                    new Club("SOAP", "서울 용산구", "이태원의 클럽 SOAP", "010-1000-1008", 4.0, 37.536925, 126.995072, "/images/clubs/capybara.png"),
                    new Club("Modeci", "서울 강남구", "럭셔리 클럽 Modeci", "010-1000-1009", 4.2, 37.506427, 127.045735, "/images/clubs/capybara.png"),
                    new Club("Mass", "서울 강남구", "강남의 대형 클럽 Mass", "010-1000-1010", 4.0, 37.504978, 127.042758, "/images/clubs/capybara.png"),
                    new Club("Chroma", "서울 송파구", "잠실에 위치한 대형 클럽 Chroma", "010-1000-1011", 4.7, 37.513144, 127.105474, "/images/clubs/capybara.png"),
                    new Club("Move", "서울 강남구", "음악이 좋은 클럽 Move", "010-1000-1012", 3.9, 37.508292, 127.038199, "/images/clubs/capybara.png"),
                    new Club("The Henz Club", "서울 마포구", "힙합 분위기의 클럽 The Henz Club", "010-1000-1013", 4.1, 37.556292, 126.923840, "/images/clubs/capybara.png"),
                    new Club("Hidden Cellar", "서울 용산구", "이태원의 Hidden Cellar", "010-1000-1014", 3.7, 37.535755, 126.994585, "/images/clubs/capybara.png"),
                    new Club("Vurt", "서울 마포구", "홍대의 작은 클럽 Vurt", "010-1000-1015", 4.0, 37.554232, 126.921872, "/images/clubs/capybara.png")
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