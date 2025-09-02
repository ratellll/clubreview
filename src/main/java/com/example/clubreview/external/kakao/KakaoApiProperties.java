package com.example.clubreview.external.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoApiProperties(
        String host,
        String restApiKey,
        int timeoutMs,
        int defaultRadius
) {}