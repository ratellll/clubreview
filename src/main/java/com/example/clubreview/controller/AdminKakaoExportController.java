package com.example.clubreview.controller;

import com.example.clubreview.export.SeoulClubPubExporter;
import com.example.clubreview.external.kakao.KakaoPlacesService;
import com.example.clubreview.external.kakao.dto.KakaoDtos;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@PermitAll
@RestController
@RequestMapping("/admin/export/kakao")
@Validated
public class AdminKakaoExportController {
    private final SeoulClubPubExporter exporter;
    private final KakaoPlacesService kakao;

    public AdminKakaoExportController(SeoulClubPubExporter exporter, KakaoPlacesService kakao) {
        this.exporter = exporter; this.kakao = kakao;
    }


    @GetMapping(value = "/clubs.csv", produces = "text/csv")
    public ResponseEntity<byte[]> csv(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "2") int pages,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "3000") int radius
    ) {
        var rows = exporter.fetch(limit, pages, size, radius);
        byte[] csv = exporter.toCsvBytes(rows);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=seoul_clubs_pubs.csv");
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/clubs-direct.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> direct(
            @RequestParam String q,
            @RequestParam double x, // longitude
            @RequestParam double y, // latitude
            @RequestParam(defaultValue = "2") int pages,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "3000") int radius
    ) {
        var list = kakao.searchKeywordPaged(q, x, y, radius, pages, size);
        return list.stream()
                .filter(c -> c.latitude() != null && c.longitude() != null)
                .map(AdminKakaoExportController::toJson)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/clubs-adhoc.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> adhoc(
            @RequestParam(defaultValue = "클럽,펍,바,나이트클럽,칵테일 바") String keywords,
            @RequestParam(defaultValue = "37.5571,126.9239;37.5349,126.9946;37.4981,127.0276;37.5446,127.0551") String centers,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "3") int pages,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "3000") int radius
    ) {
        var kwList = Arrays.stream(keywords.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
        var centerList = Arrays.stream(centers.split(";"))
                .map(s -> s.trim())
                .filter(s -> !s.isBlank())
                .map(AdminKakaoExportController::parseLatLng)
                .toList();


        Map<String, Map<String,Object>> dedup = new LinkedHashMap<>();
        outer:
        for (var center : centerList) {
            double lat = (double) center.get("lat");
            double lng = (double) center.get("lng");
            for (var kw : kwList) {
                var list = kakao.searchKeywordPaged(kw, lng, lat, radius, pages, size);
                for (var c : list) {
                    if (c.latitude() == null || c.longitude() == null) continue;
                    dedup.putIfAbsent(c.provider()+"::"+c.externalId(), toJson(c));
                    if (dedup.size() >= limit) break outer;
                }
            }
        }
        return new ArrayList<>(dedup.values());
    }

    @GetMapping(value = "/clubs.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> json(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "2") int pages,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "3000") int radius
    ) {
        var rows = exporter.fetch(limit, pages, size, radius);
        return exporter.toJson(rows);
    }

    private static Map<String,Object> toJson(KakaoDtos.ClubNormalized c) {
        String location = (c.roadAddress()!=null && !c.roadAddress().isBlank()) ? c.roadAddress() : c.address();
        return Map.of(
                "name", c.name(),
                "description", "source:kakao",
                "location", location,
                "latitude", c.latitude(),
                "longitude", c.longitude(),
                "call_number", c.phone(),
                "photo_url", null
        );
    }


    private static Map<String,Object> parseLatLng(String s) {
        var parts = s.split(",");
        double lat = Double.parseDouble(parts[0].trim());
        double lng = Double.parseDouble(parts[1].trim());
        return Map.of("lat", lat, "lng", lng);
    }
}