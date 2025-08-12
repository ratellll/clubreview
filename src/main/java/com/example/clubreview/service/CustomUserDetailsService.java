package com.example.clubreview.service;

import com.example.clubreview.entity.User;
import com.example.clubreview.repository.UserRepository;
import com.example.clubreview.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));

        if (user.getBanEndTime() != null && user.getBanEndTime().isAfter(LocalDateTime.now())) {
            throw new LockedException("해당 계정은 정지되었습니다. 만료일: " +
                    user.getBanEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        return new CustomUserDetails(user);
    }

}