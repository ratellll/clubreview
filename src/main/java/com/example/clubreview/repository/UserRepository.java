package com.example.clubreview.repository;

import com.example.clubreview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    //로그인용
    User findByUsername(String username);
}
