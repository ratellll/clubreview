//package com.example.clubreview.config;
//
//import com.example.clubreview.security.JwtAuthenticationEntryPoint;
//import com.example.clubreview.security.JwtRequestFilter;
//import com.example.clubreview.service.CustomUserDetailsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(jsr250Enabled = true, prePostEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig_for_csv {
//
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final JwtRequestFilter jwtRequestFilter;
//    private final CustomUserDetailsService customUserDetailsService;
//
//
//
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(customUserDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }
//    @Bean
//    public SecurityFilterChain security(HttpSecurity http,
//                                        JwtAuthenticationEntryPoint entryPoint,
//                                        JwtRequestFilter jwtRequestFilter) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .cors(cors -> {})
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .requestCache(rc -> rc.disable())
//                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.GET, "/admin/export/kakao/**").permitAll()
//                        .requestMatchers("/", "/favicon.ico", "/error",
//                                "/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**").permitAll()
//                        .requestMatchers("/auth/**", "/api/auth/**", "/actuator/health").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }
//}