package com.example.clubreview.external.kakao;

import com.example.clubreview.external.kakao.dto.KakaoDtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class KakaoPlacesService {
    private final KakaoLocalClient client;
    private final ObjectMapper om = new ObjectMapper();


    public KakaoPlacesService(KakaoLocalClient client) { this.client = client; }


    public List<KakaoDtos.ClubNormalized> searchKeywordPaged(String query, Double x, Double y, Integer radius, int pages, int size) {
        List<KakaoDtos.ClubNormalized> out = new ArrayList<>();
        for (int page = 1; page <= Math.max(1, pages); page++) {
            try {
                String json = client.searchByKeyword(query, x, y, radius, page, size).block();
                var resp = om.readValue(json, KakaoDtos.SearchResponse.class);
                int docs = (resp == null || resp.documents() == null) ? 0 : resp.documents().size();
                log.info("[Kakao] q='{}' center=({}, {}) radius={} page={}/{} docs={}", query, y, x, radius, page, pages, docs);
                if (docs == 0) break;
                resp.documents().forEach(d -> out.add(KakaoDtos.ClubNormalized.from(d)));
                if (resp.meta() != null && resp.meta().isEnd()) break;
            } catch (Exception e) {
                log.warn("[Kakao] parse/fetch failed q='{}' center=({}, {}) page={} : {}", query, y, x, page, e.toString());
                break; // 외부 오류시 반복 중단(과도 호출 방지)
            }
        }
        log.info("[Kakao] aggregated {} items for q='{}'", out.size(), query);
        return out;
    }
}