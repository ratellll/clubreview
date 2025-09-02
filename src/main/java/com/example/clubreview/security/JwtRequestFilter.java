package com.example.clubreview.security;

import com.example.clubreview.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;


    private static final List<RequestMatcher> WHITELIST = List.of(
            new AntPathRequestMatcher("/admin/export/kakao/**")
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        for (RequestMatcher rm : WHITELIST) {
            if (rm.matches(request)) return true;
        }
        String auth = request.getHeader("Authorization");
        return auth == null || !auth.startsWith("Bearer ");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {


        for (RequestMatcher rm : WHITELIST) {
            if (rm.matches(request)) {
                chain.doFilter(request, response);
                return; // 퍼블릭 경로는 JWT 검사 완전 스킵
            }
        }
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/") ||
                (requestURI.startsWith("/api/clubs") && "GET".equals(request.getMethod()))) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // JWT Token은 "Bearer token" 형식으로 전송됨
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.error("JWT Token을 가져올 수 없습니다", e);
            } catch (ExpiredJwtException e) {
                log.error("JWT Token이 만료되었습니다", e);
            } catch (Exception e) {
                log.error("JWT Token 처리 중 오류 발생", e);
            }
        } else {
            log.debug("JWT Token이 Bearer로 시작하지 않습니다");
        }

        // 토큰을 검증하고 인증 설정
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 벤된 사용자 API 접근 차단
            if (userDetails instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                if (customUserDetails.getUser().isBanned()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }

            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        chain.doFilter(request, response);
    }
}