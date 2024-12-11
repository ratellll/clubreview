package com.example.clubreview.service;

import com.example.clubreview.dto.UserDto;
import com.example.clubreview.entity.User;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.exception.UserNotFoundException;
import com.example.clubreview.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;


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
                .nickname(userDto.getNickname())
                .password(encodedPassword)
                .phoneNumber(userDto.getPhoneNumber().replaceAll("-","")) //하이픈제거하고넣기
                .role(User.Role.USER)
                .build();

        userRepository.save(user);
    }

    //회원 수정
    @Transactional
    public void updateUser(UserDto userDto) {
        if (userRepository.findByNickname(userDto.getNickname()).isPresent()) {
            throw new DuplicateReviewException("이미 존재하는 닉네임 입니다.");
        }
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        User user = User.builder()
                .nickname(userDto.getNickname())
                .password(encodedPassword)
                .build();
        userRepository.save(user);
    }

    //유저 삭제메서드
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다. ID: " + id));
        userRepository.delete(user);
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

    public boolean phoneIsFine(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isEmpty();
    }
    public boolean nickNameIsFine(String nickName) {
        return userRepository.findByNickname(nickName).isEmpty();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다"+ id) );
    }
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    //회원정지
    public void banUser(Long id, int days) {
        User user = userRepository.findById(id).orElseThrow(() ->new UserNotFoundException("유저를 찾을 수 없습니다." + id));
        user.setBanEndTime(LocalDateTime.now().plusDays(days));
        userRepository.save(user);
    }

    //정지해제
    public void unbanUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->new UserNotFoundException("유저를 찾을 수 없습니다." + id));
        user.setBanEndTime(null);
        userRepository.save(user);
    }

}
