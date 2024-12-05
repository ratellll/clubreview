package com.example.clubreview.config;

import com.example.clubreview.security.CustomAuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public SecurityConfig(CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }


    @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // X-Frame-Options 비활성화

        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/users/register", "/h2-console/**", "/uploads/**","/users/register/**").permitAll() // 공개 경로
                        .requestMatchers("/clubs/list", "/clubs/{id}", "/clubs/details/**","reviews/user/**").authenticated() // 클럽 리스트와 상세 페이지는 로그인 필요
                        .requestMatchers("/clubs/admin/**", "/reviews/admin/**").hasRole("ADMIN") // ADMIN 권한 필요
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/") // "/" 경로를 로그인 페이지로 설정
                        .loginProcessingUrl("/login") // 로그인 처리 URL
                        .defaultSuccessUrl("/clubs/list", true) // 로그인 성공 후 이동
                        .failureHandler(customAuthenticationFailureHandler) // 로그인 실패 핸들러
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 경로
                        .logoutSuccessUrl("/") // 로그아웃 후 경로
                        .permitAll());

        return http.build();
    }
}
