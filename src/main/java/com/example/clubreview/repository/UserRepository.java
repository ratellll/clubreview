package com.example.clubreview.repository;

import com.example.clubreview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인용
    Optional<User> findByUsername(String username);

    //폰번호 중복쳌
    Optional<User> findByPhoneNumber(String phoneNumber);
}
