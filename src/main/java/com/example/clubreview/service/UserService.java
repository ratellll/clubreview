package com.example.clubreview.service;

import com.example.clubreview.dto.UserDto;
import com.example.clubreview.entity.User;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(UserDto userDto) {

        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new DuplicateReviewException("이미 존재하는 아이디 입니다.");
        }

        if (userRepository.findByPhoneNumber(userDto.getPhoneNumber()).isPresent()) {
            throw new DuplicateReviewException("이미 존재하는 핸드폰번호 입니다.");
        }
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .username(userDto.getUsername())
                .password(encodedPassword)
                .phoneNumber(userDto.getPhoneNumber().replaceAll("-","")) //하이픈제거하고넣기
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
    }

    // 이름으로 user 조회
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다 " + username));
    }

    //회원가입 아이디중복체크
    public boolean idIsFine(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

}
