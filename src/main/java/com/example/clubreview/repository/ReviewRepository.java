package com.example.clubreview.repository;

import com.example.clubreview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.club.id = :clubId ORDER BY r.createTime DESC")
    List<Review> findByClubIdWithUser(@Param("clubId") Long clubId);

    Optional<Review> findByUserIdAndClubId(Long userId, Long clubId);

    @Query("SELECT r FROM Review r JOIN FETCH r.club WHERE r.user.id = :userId ORDER BY r.createTime DESC")
    List<Review> findByUserIdWithClub(@Param("userId") Long userId);

    List<Review> findByUserId(Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.club.id = :clubId")
    Double calculateAverageRating(@Param("clubId") Long clubId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r JOIN FETCH r.club WHERE r.user.id = :userId AND r.createTime BETWEEN :startDate AND :endDate ORDER BY r.createTime DESC")
    List<Review> findByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Review r JOIN FETCH r.club WHERE r.user.id = :userId AND r.rating >= :minRating ORDER BY r.rating DESC")
    List<Review> findByUserIdAndMinRating(@Param("userId") Long userId, @Param("minRating") int minRating);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.club.id = :clubId")
    void deleteByClubId(@Param("clubId") Long clubId);


    List<Review> findAllByOrderByCreateTimeDesc();

    List<Review> findByUserNickNameContainingIgnoreCaseOrderByCreateTimeDesc(String nickName);
}