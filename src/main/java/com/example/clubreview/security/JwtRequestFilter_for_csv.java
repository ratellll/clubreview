//package com.example.clubreview.security;
//
//import io.jsonwebtoken.ExpiredJwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class JwtRequestFilter_for_csv extends OncePerRequestFilter {
//
//
//    private final com.example.clubreview.service.CustomUserDetailsService userDetailsService;
//    private final com.example.clubreview.security.JwtUtil jwtUtil;
//
//
//    private static final List<RequestMatcher> WHITELIST = List.of(
//            new AntPathRequestMatcher("/admin/export/kakao/**")
//    );
//
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
//        for (RequestMatcher rm : WHITELIST) if (rm.matches(request)) return true;
//        String auth = request.getHeader("Authorization");
//        return auth == null || !auth.startsWith("Bearer ");
//    }
//
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//// hard skip whitelist (defensive)
//        for (RequestMatcher rm : WHITELIST) {
//            if (rm.matches(request)) { chain.doFilter(request, response); return; }
//        }
//
//
//        final String requestTokenHeader = request.getHeader("Authorization");
//        String username = null; String jwtToken = null;
//
//
//        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
//            jwtToken = requestTokenHeader.substring(7);
//            try {
//                username = jwtUtil.getUsernameFromToken(jwtToken);
//            } catch (IllegalArgumentException e) {
//                log.error("JWT Token을 가져올 수 없습니다", e);
//            } catch (ExpiredJwtException e) {
//                log.error("JWT Token이 만료되었습니다", e);
//            } catch (Exception e) {
//                log.error("JWT Token 처리 중 오류 발생", e);
//            }
//        } else {
//            log.debug("JWT Token이 Bearer로 시작하지 않습니다");
//        }
//
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//            if (userDetails instanceof com.example.clubreview.security.CustomUserDetails cud && cud.getUser().isBanned()) {
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN); return;
//            }
//            if (jwtUtil.validateToken(jwtToken, userDetails)) {
//                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            }
//        }
//        chain.doFilter(request, response);
//    }
//}