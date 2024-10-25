package com.example.clubreview.repository;

import com.example.clubreview.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {


    // 이름 오름차순으로 모든 클럽 조회 (페이징 포함)
    Page<Club> findAllByOrderByNameAsc(Pageable pageable);

    // 평점 내림차순으로 모든 클럽 조회 (페이징 포함)
    Page<Club> findAllByOrderByAverageRatingDesc(Pageable pageable);

    // 클럽 이름으로 검색
    Optional<Club> findByName(String name);


}
