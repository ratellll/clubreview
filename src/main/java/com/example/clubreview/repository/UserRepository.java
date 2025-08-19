package com.example.clubreview.repository;

import com.example.clubreview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByNickName(String nickName);
    boolean existsByUserName(String userName);
    boolean existsByNickName(String nickName);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findByNickNameContaining(String nickname);

}