package com.example.clubreview.external.kakao;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KakaoApiProperties.class)
public class KakaoConfig {
}
