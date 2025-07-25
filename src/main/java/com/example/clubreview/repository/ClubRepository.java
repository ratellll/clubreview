package com.example.clubreview.repository;

import com.example.clubreview.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    // 페이징 정렬용 (실제 사용)
    Page<Club> findAllByOrderByNameAsc(Pageable pageable);
    Page<Club> findAllByOrderByAverageRatingDesc(Pageable pageable);

    // 중복 체크용 (실제 사용)
    boolean existsByName(String name);

    /*
    Optional<Club> findByName(String name);

    @Query("SELECT c FROM Club c WHERE c.location LIKE %:location%")
    List<Club> findByLocationContaining(@Param("location") String location);

    @Query("SELECT c FROM Club c WHERE c.averageRating >= :minRating")
    List<Club> findByAverageRatingGreaterThanEqual(@Param("minRating") double minRating);
    */
}