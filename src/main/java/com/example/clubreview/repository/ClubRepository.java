package com.example.clubreview.repository;

import com.example.clubreview.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    List<Club> findAllByOrderByNameAsc();
    List<Club> findAllByOrderByAverageRatingDesc();
    Optional<Club> findById(Long id);
}
