package com.example.clubreview.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;

import java.util.List;


public class KakaoDtos {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(
            List<Document> documents,
            Meta meta
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(@JsonProperty("is_end") boolean isEnd) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String id,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("road_address_name") String roadAddressName,
            @JsonProperty("address_name") String addressName,
            String phone,
            String x, // 경도(lng) - 문자열로 옴
            String y  // 위도(lat)  - 문자열로 옴
    ) {}

    public record ClubNormalized(
            String provider,
            String externalId,
            String name,
            String roadAddress,
            String address,
            String phone,
            Double latitude,
            Double longitude
    ) {
        public static ClubNormalized from(Document d) {
            Double lat = toDoubleSafe(d.y()); // y=위도
            Double lng = toDoubleSafe(d.x()); // x=경도
            return new ClubNormalized(
                    "kakao",
                    d.id(),
                    d.placeName(),
                    d.roadAddressName(),
                    d.addressName(),
                    d.phone(),
                    lat,
                    lng
            );
        }
    }

    private static Double toDoubleSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}