package com.example.clubreview.security;

import com.example.clubreview.entity.User;
import com.example.clubreview.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage = "아이디 또는 비밀번호가 잘못되었습니다."; // 기본 메시지

        if (exception instanceof InternalAuthenticationServiceException && exception.getCause() instanceof LockedException) {
            // LockedException - 계정 정지된 경우
            errorMessage = exception.getCause().getMessage(); // 예외 메시지에 만료일이 포함되어 있습니다.
        } else if (exception instanceof LockedException) {
            // LockedException - 계정 정지된 경우
            errorMessage = exception.getMessage();
        } else if (exception instanceof BadCredentialsException) {
            // BadCredentialsException - 아이디 또는 비밀번호가 잘못된 경우
            errorMessage = "아이디 또는 비밀번호가 잘못되었습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            // UsernameNotFoundException - 존재하지 않는 아이디인 경우
            errorMessage = "존재하지 않는 아이디입니다.";
        } else if (exception instanceof DisabledException) {
            // DisabledException - 계정이 비활성화된 경우
            errorMessage = "해당 계정은 비활성화 상태입니다.";
        }


        request.getSession().setAttribute("errorMessage", errorMessage);


        // 로그인 실패 후 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}


