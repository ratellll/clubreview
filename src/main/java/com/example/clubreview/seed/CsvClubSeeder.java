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
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Profile("dev")
@Order(1)
public class CsvClubSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final ResourceLoader resourceLoader;
    private final SeedProps props;

    public CsvClubSeeder(JdbcTemplate jdbc, ResourceLoader resourceLoader, SeedProps props) {
        this.jdbc = jdbc;
        this.resourceLoader = resourceLoader;
        this.props = props;
    }

    @PostConstruct
    void logProps() {
        log.info("[Seed] enabled={}, truncateBefore={}, csvPath={}, useCsvDesc={}, defaultRating={}",
                props.isRunOnStartup(), props.isTruncateBefore(), props.getCsvPath(), props.isUseCsvDescription(), props.getDefaultAverageRating());
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
    }

    private void purgeBeforeSeed() {
        if (!props.isTruncateBefore()) return;
        jdbc.execute((ConnectionCallback<Void>) con -> {
            try (Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String t : safeTableList(props.getDependentTables())) {
                    try {
                        st.execute("TRUNCATE TABLE `" + t + "`");
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
        return String.format("%s는 %s에 위치한 클럽/펍입니다. 위도 %.5f, 경도 %.5f.%s", r.name, loc, r.latitude, r.longitude, phone);
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
        private double defaultAverageRating = 0.0;
        /**
         * child tables referencing club.id (FK club_id)
         */
        private List<String> dependentTables = List.of("review");
    }
}

