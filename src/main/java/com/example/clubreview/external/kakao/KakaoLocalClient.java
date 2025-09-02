package com.example.clubreview.external.kakao;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class KakaoLocalClient {

    private final WebClient webClient;
    private final KakaoApiProperties props;

    public KakaoLocalClient(KakaoApiProperties props) {
        this.props = props;
        this.webClient = WebClient.builder()
                .baseUrl(props.host())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + props.restApiKey())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("[Kakao] host={}, keyPresent={} timeoutMs={} radius={}",
                props.host(), props.restApiKey() != null && !props.restApiKey().isBlank(),
                props.timeoutMs(), props.defaultRadius());
    }

    public Mono<String> searchByKeyword(String query, Double x, Double y, Integer radius, Integer page, Integer size) {
        return webClient.get()
                .uri(uri -> uri
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParamIfPresent("x", Optional.ofNullable(x))
                        .queryParamIfPresent("y", Optional.ofNullable(y))
                        .queryParam("radius", radius == null ? props.defaultRadius() : radius)
                        .queryParam("page", page == null ? 1 : page)
                        .queryParam("size", size == null ? 15 : size)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(props.timeoutMs()));
    }
}
