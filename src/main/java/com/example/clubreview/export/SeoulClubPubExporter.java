package com.example.clubreview.export;


import com.example.clubreview.external.kakao.KakaoPlacesService;
import com.example.clubreview.external.kakao.dto.KakaoDtos;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeoulClubPubExporter {

    private final KakaoPlacesService kakao;

    public SeoulClubPubExporter(KakaoPlacesService kakao) { this.kakao = kakao; }

    public List<ClubSeedRow> fetch(int limit, int pagesPerCenter, int sizePerPage, int radiusMeters) {
        var centers = seoulCenters();
        var keywords = List.of("클럽", "펍", "힙합 클럽", "홍대 클럽", "이태원 클럽", "건대 클럽", "EDM", "힙합");
        Map<String, ClubSeedRow> dedup = new LinkedHashMap<>();
        for (var center : centers) {
            for (var kw : keywords) {
                var list = kakao.searchKeywordPaged(kw, center.lng(), center.lat(), radiusMeters, pagesPerCenter, sizePerPage);
                for (KakaoDtos.ClubNormalized c : list) {
                    if (c.latitude() == null || c.longitude() == null) continue; // guarantee coords
                    var row = toRow(c);
                    dedup.putIfAbsent(key(c), row);
                    if (dedup.size() >= limit) return new ArrayList<>(dedup.values());
                }
            }
        }
        return new ArrayList<>(dedup.values());
    }


    private static String key(KakaoDtos.ClubNormalized c) { return c.provider() + "::" + c.externalId(); }


    private static ClubSeedRow toRow(KakaoDtos.ClubNormalized c) {
        String location = (c.roadAddress() != null && !c.roadAddress().isBlank()) ? c.roadAddress() : c.address();
        return new ClubSeedRow(
                c.name(),
                "source:kakao keyword export",
                location,
                c.latitude(),
                c.longitude(),
                c.phone(),
                null
        );
    }


    public byte[] toCsvBytes(List<ClubSeedRow> rows) {
        String header = "name,description,location,latitude,longitude,call_number,photo_url\n";
        String body = rows.stream().map(SeoulClubPubExporter::csvLine).collect(Collectors.joining("\n"));
        return (header + body + "\n").getBytes(StandardCharsets.UTF_8);
    }


    public List<Map<String, Object>> toJson(List<ClubSeedRow> rows) {
        return rows.stream().map(r -> Map.<String, Object>of(
                "name", r.name(),
                "description", r.description(),
                "location", r.location(),
                "latitude", r.latitude(),
                "longitude", r.longitude(),
                "call_number", r.callNumber(),
                "photo_url", r.photoUrl()
        )).collect(Collectors.toList());
    }


    private static String esc(String s) {
        if (s == null) return "";
        String q = s.replace("\"", "\"\"");
        return '"' + q + '"';
    }


    private static String csvLine(ClubSeedRow r) {
        return String.join(",",
                esc(r.name()), esc(r.description()), esc(r.location()),
                r.latitude() == null ? "" : r.latitude().toString(),
                r.longitude() == null ? "" : r.longitude().toString(),
                esc(r.callNumber()), esc(r.photoUrl())
        );
    }


    // Records for immutable data
    public record ClubSeedRow(
            String name,
            String description,
            String location,
            Double latitude,
            Double longitude,
            String callNumber,
            String photoUrl
    ) {}


    public record Geo(double lat, double lng, String name) {}


    private static List<Geo> seoulCenters() {
        return List.of(
                new Geo(37.5571, 126.9239, "Hongdae"),
                new Geo(37.5349, 126.9946, "Itaewon"),
                new Geo(37.4981, 127.0276, "Gangnam"),
                new Geo(37.5253, 127.0417, "Cheongdam/Apgujeong"),
                new Geo(37.5133, 127.1028, "Jamsil"),
                new Geo(37.5405, 127.0709, "Konkuk"),
                new Geo(37.5446, 127.0551, "Seongsu"),
                new Geo(37.5658, 126.9230, "Yeonnam"),
                new Geo(37.5599, 126.9360, "Sinchon")
        );
    }
}