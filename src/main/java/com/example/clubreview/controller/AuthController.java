package com.example.clubreview.controller;


import com.example.clubreview.dto.config.ApiResponse;
import com.example.clubreview.dto.config.JwtResponse;
import com.example.clubreview.dto.config.LoginRequest;
import com.example.clubreview.dto.UserDto;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.service.CustomUserDetailsService;
import com.example.clubreview.service.UserService;
import com.example.clubreview.utils.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("로그인 시도: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

            JwtResponse jwtResponse = JwtResponse.of(token, userDetails.getUsername(), expiresAt);

            log.info("로그인 성공: {}", request.getUsername());
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", jwtResponse));

        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 인증정보: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("아이디 또는 비밀번호가 올바르지 않습니다."));
        } catch (LockedException e) {
            log.warn("로그인 실패 - 계정 잠김: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserDto userDto) {
        log.info("회원가입 시도: {}", userDto.getUsername());

        try {
            userService.registerUser(userDto);
            log.info("회원가입 성공: {}", userDto.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("회원가입이 완료되었습니다."));

        } catch (DuplicateReviewException e) {
            log.warn("회원가입 실패 - 중복: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 - 유효성 검사: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(
            @RequestParam @Valid @Pattern(regexp = "username|phoneNumber|nickname") String type,
            @RequestParam @NotBlank String value) {

        try {
            boolean isAvailable = userService.checkDuplicate(type, value);
            String message = isAvailable ? "사용 가능합니다." : "이미 사용 중입니다.";

            return ResponseEntity.ok(ApiResponse.success(message, isAvailable));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("중복 체크 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("중복 체크 중 오류가 발생했습니다."));
        }
    }
}
