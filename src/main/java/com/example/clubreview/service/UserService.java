package com.example.clubreview.service;

import com.example.clubreview.dto.user.UserDto;
import com.example.clubreview.entity.User;
import com.example.clubreview.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ReviewService reviewService;

    @Transactional
    public User registerUser(UserDto userDto) {
        log.info("회원가입 시도: {}", userDto.getUserName());

        // 중복 검사
        validateUserUniqueness(userDto);

        // 유효성 검사
        validateUserData(userDto);

        // 사용자 생성
        User user = User.builder()
                .userName(userDto.getUserName())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickName(userDto.getNickName())
                .phoneNumber(userDto.getPhoneNumber().replaceAll("-", ""))
                .role(User.Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: {}", savedUser.getId());

        return savedUser;
    }

    public boolean checkDuplicate(String type, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("값이 비어있습니다.");
        }

        return switch (type.toLowerCase()) {
            case "username" -> !userRepository.existsByUserName(value);
            case "phonenumber" -> !userRepository.existsByPhoneNumber(value.replaceAll("-", ""));
            case "nickname", "nickName" -> !userRepository.existsByNickName(value);
            default -> throw new IllegalArgumentException("유효하지 않은 타입입니다: " + type);
        };
    }


    @Transactional
    public void deleteUser(Long id) {
        log.info("사용자 삭제: {}", id);

        User user = findById(id);

        reviewService.deleteUserReviews(id);

        userRepository.delete(user);

        log.info("사용자 삭제 완료: {}", id);
    }

    public User findByUsername(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " +userName));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " +id));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }


    @Transactional(readOnly = true)
    public List<User> searchUsersByNickname(String nickname) {
        log.info("닉네임으로 사용자 검색: {}", nickname);
        return userRepository.findByNickNameContaining(nickname);
    }


    @Transactional
    public User banUser(Long id, int days) {
        log.info("사용자 정지: {} ({}일)", id, days);

        User user = findById(id);
        User bannedUser = user.banUser(days);

        User savedUser = userRepository.save(bannedUser);
        log.info("사용자 정지 완료: {}", savedUser.getId());

        return savedUser;
    }

    @Transactional
    public User unbanUser(Long id) {
        log.info("사용자 정지 해제: {}", id);

        User user = findById(id);
        User unbannedUser = user.unbanUser();

        User savedUser = userRepository.save(unbannedUser);
        log.info("사용자 정지 해제 완료: {}", savedUser.getId());

        return savedUser;
    }


    @Transactional
    public User updateNickname(String username, String newNickName) {
        log.info("닉네임 변경: {} -> {}", username, newNickName);

        validateNickname(newNickName);

        User user = findByUsername(username);
        if (!user.getNickName().equals(newNickName) && userRepository.existsByNickName(newNickName)) {
            throw new DataIntegrityViolationException("이미 사용 중인 닉네임입니다.");
        }

        User updatedUser = user.updateNickname(newNickName);
        User savedUser = userRepository.save(updatedUser);

        log.info("닉네임 변경 완료: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    public User updatePassword(String username, String newPassword) {
        log.info("비밀번호 변경: {}", username);

        validatePassword(newPassword);

        User user = findByUsername(username);
        User updatedUser = user.updatePassword(passwordEncoder.encode(newPassword));
        User savedUser = userRepository.save(updatedUser);

        log.info("비밀번호 변경 완료: {}", savedUser.getId());
        return savedUser;
    }

    private void validateUserUniqueness(UserDto userDto) {
        if (userRepository.existsByUserName(userDto.getUserName())) {
            throw new DataIntegrityViolationException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByNickName(userDto.getNickName())) {
            throw new DataIntegrityViolationException("이미 존재하는 닉네임입니다.");
        }
        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber().replaceAll("-", ""))) {
            throw new DataIntegrityViolationException("이미 존재하는 전화번호입니다.");
        }
    }

    private void validateUserData(UserDto userDto) {
        validateNickname(userDto.getNickName());
        validatePassword(userDto.getPassword());
        validatePhoneNumber(userDto.getPhoneNumber());
    }

    public void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (!nickname.matches("^[가-힣]{2,5}$")) {
            throw new IllegalArgumentException("닉네임은 2자 이상 5자 이하의 한글이어야 합니다.");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        String cleanNumber = phoneNumber.replaceAll("-", "");
        if (!cleanNumber.matches("^\\d{10,11}$")) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다.");
        }
    }
}