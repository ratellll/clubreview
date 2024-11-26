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
        // CSRF 비활성화 및 X-Frame-Options 비활성화
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // X-Frame-Options 비활성화

        // 인증 및 권한 설정
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/users/register", "/login", "/h2-console/**","/uploads/**").permitAll() // h2-console 접근 허용
                        .requestMatchers("clubs/list").authenticated()
                        .requestMatchers("/clubs/admin/**", "/reviews/admin/**").hasRole("ADMIN") // admin 경로는 ADMIN 권한 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/") // 로그인 페이지 경로 설정
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/clubs/list", true) // 로그인 성공 후 리디렉션 설정
                        .failureHandler(customAuthenticationFailureHandler) // 실패 핸들러 등록
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }
}
