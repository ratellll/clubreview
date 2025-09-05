// file: src/main/java/com/example/clubreview/seed/CsvClubSeeder.java
package com.example.clubreview.seed;

/*
Minimal, drop‑in seeder with:
- dev only (@Profile("dev"))
- FK-safe purge: TRUNCATE child tables (e.g., review) → TRUNCATE club, in SAME connection
- CSV import (name,description,location,latitude,longitude,call_number,photo_url)
- Description: use CSV or generate
- UPSERT on (name, latitude, longitude)
- Basic logs
*/

import com.example.clubreview.entity.User;
import com.example.clubreview.repository.ReviewRepository;
import com.example.clubreview.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("dev")
@Order(1)
public class CsvClubSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final ResourceLoader resourceLoader;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SeedProps props;

    public CsvClubSeeder(JdbcTemplate jdbc, ResourceLoader resourceLoader, SeedProps props, UserRepository userRepository, ReviewRepository reviewRepository, BCryptPasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.resourceLoader = resourceLoader;
        this.props = props;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void logProps() {
        log.info("[Seed] enabled={}, truncateBefore={}, csvPath={}, useCsvDesc={}, defaultRating={}",
                props.isRunOnStartup(), props.isTruncateBefore(), props.getCsvPath(), props.isUseCsvDescription(), props.getDefaultAverageRating());
        log.info("[Seed] enabled={}, truncateBefore={}, csvPath={}, useCsvDesc={}, defaultRating={}, createReviews={}",
                props.isRunOnStartup(), props.isTruncateBefore(), props.getCsvPath(), props.isUseCsvDescription(), props.getDefaultAverageRating(), props.isCreateReviews());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!props.isRunOnStartup()) {
            log.info("[Seed] run-on-startup=false → skip");
            return;
        }

        purgeBeforeSeed();

        createUniqueIndexIfAbsent();

        // 3) CSV resolve & read
        Resource r = resolveCsv(props.getCsvPath());
        if (!r.exists()) {
            log.warn("[Seed] CSV not found: {}", props.getCsvPath());
            return;
        }

        int inserted = 0, updated = 0, skipped = 0, read = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) {
                log.warn("[Seed] empty CSV");
                return;
            }
            Map<String, Integer> cols = headerIndex(header);

            for (String line; (line = br.readLine()) != null; ) {
                read++;
                List<String> f = parseCsvLine(line);
                if (f.size() < 7) {
                    skipped++;
                    continue;
                }

                ClubRow row = toRow(f, cols);
                if (row.latitude == null || row.longitude == null || isBlank(row.name)) {
                    skipped++;
                    continue;
                }

                String description = props.isUseCsvDescription() ? nz(row.description) : genDescription(row);
                int result = upsert(row, description);
                if (result == 1) inserted++;
                else if (result == 2) updated++;
                else skipped++;
            }
        } catch (IOException e) {
            log.warn("[Seed] IO failed: {}", e.getMessage());
        }
        log.info("[Seed] done. read={}, inserted={}, updated={}, skipped={}", read, inserted, updated, skipped);

        if (props.isCreateReviews()) {
            log.info("[Seed] 리뷰 시딩 시작--");
            seedReview();
        }
    }

    private void seedReview() {
        try {
            createTestUsers();

            createReviewsForClubs();
            log.info("[Seed] 리뷰 시딩 완료---");
        } catch (Exception e) {
            log.error("[Seed] 리뷰 시딩 . 오류 발생 ", e);
        }
    }

    private void createTestUsers() {
        List<String> nicknames = List.of(
                "음악매니아", "댄스킹", "클럽러버", "파티걸", "비트마스터", "나이트오울",
                "뮤직홀릭", "댄스퀸", "클럽고수", "파티남", "리듬감각", "밤의제왕",
                "음향전문가", "춤신춤왕", "클럽탐험가", "파티피플", "비트박스", "나이트라이더",
                "뮤직러버", "댄스머신", "클럽마니아", "파티타임", "사운드헌터", "밤샘족"
        );

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String nickname = nicknames.get(i);
            String username = "reviewer" + (i + 1);
            String phone = String.format("010%08d", 30000000 + i); // 높은 번호로 시작

            // 중복 체크
            if (userRepository.existsByUserName(username) ||
                    userRepository.existsByPhoneNumber(phone) ||
                    userRepository.existsByNickName(nickname)) {
                continue;
            }

            User user = User.builder()
                    .userName(username)
                    .password(passwordEncoder.encode("password123"))
                    .nickName(nickname)
                    .phoneNumber(phone)
                    .role(User.Role.USER)
                    .build();

            users.add(user);
        }

        if (!users.isEmpty()) {
            userRepository.saveAll(users);
            log.info("[Seed] {} 명의 테스트 사용자 생성 완료", users.size());
        }
    }

    private void createReviewsForClubs() {
        // 클럽 목록을 직접 DB에서 조회
        List<Map<String, Object>> clubs = jdbc.queryForList("SELECT id, name FROM club ORDER BY id");
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getUserName().startsWith("reviewer"))
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            log.warn("[Seed] 테스트 사용자가 없어 리뷰를 생성할 수 없습니다.");
            return;
        }

        List<String> positiveReviews = List.of(
                "분위기가 정말 좋아요! 음악도 최고였습니다.",
                "직원들이 친절하고 서비스가 훌륭해요.",
                "인테리어가 세련되고 음료도 맛있어요.",
                "친구들과 즐거운 시간 보냈습니다.",
                "음악이 좋고 춤추기 좋은 공간이에요."
        );

        List<String> neutralReviews = List.of(
                "무난한 클럽이에요. 나쁘지 않네요.",
                "평범한 수준입니다.",
                "그럭저럭 괜찮은 것 같아요."
        );

        List<String> negativeReviews = List.of(
                "너무 시끄럽고 복잡해요.",
                "가격이 비싼 편이네요.",
                "서비스가 아쉬워요."
        );

        Random random = new Random();
        int totalReviews = 0;

        for (Map<String, Object> club : clubs) {
            Long clubId = ((Number) club.get("id")).longValue();
            int reviewCount = 3 + random.nextInt(2); // 3-4개 리뷰

            for (int i = 0; i < reviewCount; i++) {
                User reviewer = users.get(random.nextInt(users.size()));
                int rating = generateWeightedRating(random);
                String comment = getRandomReviewComment(rating, positiveReviews, neutralReviews, negativeReviews, random);

                // 직접 SQL로 리뷰 삽입 (JPA 순환 참조 회피)
                jdbc.update(
                        "INSERT INTO review (club_id, user_id, rating, comment, create_time, update_time) VALUES (?, ?, ?, ?, NOW(6), NOW(6))",
                        clubId, reviewer.getId(), rating, comment
                );

                totalReviews++;
            }
        }

        log.info("[Seed] {} 개 클럽에 대해 {} 개의 리뷰 생성 완료", clubs.size(), totalReviews);

        // 클럽 평균 평점 업데이트
        updateClubAverageRatings();
    }

    private int generateWeightedRating(Random random) {
        double rand = random.nextDouble();
        if (rand < 0.4) return 5;        // 40%
        else if (rand < 0.7) return 4;   // 30%
        else if (rand < 0.85) return 3;  // 15%
        else if (rand < 0.95) return 2;  // 10%
        else return 1;                   // 5%
    }

    private String getRandomReviewComment(int rating, List<String> positive, List<String> neutral, List<String> negative, Random random) {
        return switch (rating) {
            case 5, 4 -> positive.get(random.nextInt(positive.size()));
            case 3 -> neutral.get(random.nextInt(neutral.size()));
            case 2, 1 -> negative.get(random.nextInt(negative.size()));
            default -> "좋은 경험이었습니다.";
        };
    }

    private void updateClubAverageRatings() {
        log.info("[Seed] 클럽 평균 평점 업데이트 시작...");

        jdbc.update("""
                UPDATE club c 
                SET average_rating = (
                    SELECT COALESCE(AVG(r.rating), 0.0) 
                    FROM review r 
                    WHERE r.club_id = c.id
                )
                """);

        log.info("[Seed] 클럽 평균 평점 업데이트 완료");
    }

    private void purgeBeforeSeed() {
        if (!props.isTruncateBefore()) return;
        jdbc.execute((ConnectionCallback<Void>) con -> {
            try (Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String t : safeTableList(props.getDependentTables())) {
                    try {
                        st.execute("TRUNCATE TABLE `" + t + "`");
                        st.execute("TRUNCATE TABLE `review`");
                        log.info("[Seed] truncated {}", t);
                    } catch (Exception e) {
                        log.warn("[Seed] skip truncate {} : {}", t, e.getMessage());
                    }
                }
                st.execute("TRUNCATE TABLE `club`");
                log.info("[Seed] truncated club");
            } finally {
                try (Statement st2 = con.createStatement()) {
                    st2.execute("SET FOREIGN_KEY_CHECKS=1");
                } catch (Exception ignore) {
                }
            }
            return null;
        });
    }

    private static List<String> safeTableList(List<String> in) {
        if (in == null) return List.of();
        List<String> out = new ArrayList<>();
        for (String s : in) {
            if (s == null) continue;
            String v = s.trim();
            if (v.matches("^[a-zA-Z0-9_]+$")) out.add(v);
        }
        return out;
    }

    private static class ClubRow {
        String name, description, location, callNumber, photoUrl;
        Double latitude, longitude;
    }

    private ClubRow toRow(List<String> f, Map<String, Integer> idx) {
        ClubRow r = new ClubRow();
        r.name = at(f, idx.get("name"));
        r.description = at(f, idx.get("description"));
        r.location = at(f, idx.get("location"));
        r.latitude = toD(at(f, idx.get("latitude")));
        r.longitude = toD(at(f, idx.get("longitude")));
        r.callNumber = at(f, idx.get("call_number"));
        r.photoUrl = at(f, idx.get("photo_url"));
        return r;
    }

    private static String at(List<String> list, Integer i) {
        if (i == null || i < 0 || i >= list.size()) return null;
        String v = list.get(i);
        return v != null ? v.trim() : null;
    }

    private static Double toD(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private Map<String, Integer> headerIndex(String headerLine) {
        List<String> h = parseCsvLine(headerLine);
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < h.size(); i++) m.put(h.get(i).trim().toLowerCase(Locale.ROOT), i);
        for (String key : List.of("name", "location", "latitude", "longitude"))
            if (!m.containsKey(key)) log.warn("[Seed] missing header: {}", key);
        return m;
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                    continue;
                }
                inQ = !inQ;
            } else if (c == ',' && !inQ) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out;
    }

    private String genDescription(ClubRow r) {
        String loc = isBlank(r.location) ? "서울" : r.location;
        String phone = isBlank(r.callNumber) ? "" : (" 문의:" + r.callNumber);
        return String.format("%s는 %s에 위치한 클럽/펍입니다. ", r.name, loc, r.latitude, r.longitude, phone);
    }

    private void createUniqueIndexIfAbsent() {
        try {
            jdbc.execute("ALTER TABLE club ADD UNIQUE KEY uk_club_name_lat_lng (name, latitude, longitude)");
            log.info("[Seed] added unique key uk_club_name_lat_lng");
        } catch (DataAccessException ignore) {
        }
    }

    private int upsert(ClubRow r, String description) {
// club 테이블 스키마
// id(PK, AI), average_rating(DOUBLE NOT NULL), call_number(VARCHAR255), description(VARCHAR255),
// latitude(DOUBLE NOT NULL), location(VARCHAR255 NOT NULL), longitude(DOUBLE NOT NULL), name(VARCHAR255 NOT NULL),
// photo_url(VARCHAR255), created_at DATETIME(6), updated_at DATETIME(6), create_time DATETIME(6), update_time DATETIME(6)


        final String sql = """
                INSERT INTO `club`
                (`name`,`description`,`location`,`latitude`,`longitude`,`call_number`,`photo_url`,`average_rating`,`created_at`,`updated_at`)
                VALUES
                (?,?,?,?,?,?,?, ?, NOW(6), NOW(6))
                ON DUPLICATE KEY UPDATE
                `description` = VALUES(`description`),
                `location` = VALUES(`location`),
                `call_number` = VALUES(`call_number`),
                `photo_url` = VALUES(`photo_url`),
                `average_rating` = VALUES(`average_rating`),
                `updated_at` = NOW(6)
                """;


        try {
            int affected = jdbc.update(con -> {
                var ps = con.prepareStatement(sql);
                int i = 1;
                ps.setString(i++, t(r.name, 255)); // name
                ps.setString(i++, t(description, 255)); // description (255 제한)
                ps.setString(i++, t(r.location, 255)); // location
                ps.setObject(i++, r.latitude); // latitude
                ps.setObject(i++, r.longitude); // longitude
                ps.setString(i++, t(r.callNumber, 255)); // call_number
                ps.setString(i++, t(r.photoUrl, 255)); // photo_url
                ps.setDouble(i++, props.getDefaultAverageRating()); // average_rating (NOT NULL)
                return ps;
            });
            return affected; // 1: insert, 2: update (드라이버마다 상이)
        } catch (org.springframework.dao.DataAccessException e) {
            String msg = (e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage());
            log.warn("[Seed] upsert failed for '{}' @({},{}): {}", r.name, r.latitude, r.longitude, msg);
            return 0;
        }
    }

    // 길이 제한 유틸(데이터 길이 초과 방지)
    private static String t(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max));
    }

    private Resource resolveCsv(String loc) {
        if (loc == null || loc.isBlank()) return resourceLoader.getResource("classpath:seed/seoul_clubs_pubs.csv");
        if (loc.startsWith("classpath:") || loc.startsWith("file:")) return resourceLoader.getResource(loc);
        return resourceLoader.getResource("file:" + loc);
    }

    /* ========================= Props ========================= */
    @Configuration
    @ConfigurationProperties(prefix = "seed")
    @Getter
    @Setter
    public static class SeedProps {
        private String csvPath = "classpath:seed/seoul_clubs_pubs.csv";
        private boolean runOnStartup = false;
        private boolean truncateBefore = false;
        private boolean useCsvDescription = false;
        private boolean createReviews = true;
        private double defaultAverageRating = 0.0;
        /**
         * child tables referencing club.id (FK club_id)
         */
        private List<String> dependentTables = List.of("review");
    }
}

