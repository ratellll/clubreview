//package com.example.clubreview.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//
//@Configuration
//@EnableWebSecurity
//@Profile("dev")
//public class DevExportSecurityConfig {
//
//
//    @Bean(name = "exportPublicChainDev")
//    @Order(0)
//    public SecurityFilterChain exportPublicChain(HttpSecurity http, JwtAuthenticationEntryPoint entryPoint) throws Exception {
//        http
//                .securityMatcher(new AntPathRequestMatcher("/admin/export/kakao/**"))
//                .csrf(csrf -> csrf.disable())
//                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.GET, "/admin/export/kakao/**").permitAll()
//                );
//        return http.build();
//    }
//}