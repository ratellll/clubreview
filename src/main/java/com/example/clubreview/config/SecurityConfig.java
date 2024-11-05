package com.example.clubreview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration public class SecurityConfig {

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests((authz) -> authz
                            .requestMatchers("/", "/users/register", "/login", "/index").permitAll()  // 접근 허용
                            .requestMatchers("/clubs/list").authenticated()  // clubs/list는 로그인 필요
                            .requestMatchers("/clubs/admin/**", "/reviews/admin/**").hasRole("ADMIN") // admin 경로는 ADMIN 권한 필요
                            .anyRequest().authenticated()
                    )
                    .formLogin((form) -> form
                            .loginPage("/")                        // 로그인 페이지 경로 설정
                            .defaultSuccessUrl("/clubs/list", true)     // 로그인 성공 후 리디렉션 설정
                            .permitAll()
                    )
                    .logout((logout) -> logout.permitAll());

            return http.build();
        }
    }
