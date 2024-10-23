package com.example.clubreview.service;


import com.example.clubreview.dto.ClubDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    // 이름 오름차순으로 정렬된 클럽 목록 (페이징)
    public Page<Club> getClubsSortedByName(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clubRepository.findAllByOrderByNameAsc(pageable);
    }

    // 평점 내림차순으로 정렬된 클럽 목록 (페이징)
    public Page<Club> getClubsSortedByRating(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clubRepository.findAllByOrderByAverageRatingDesc(pageable);
    }

    // 클럽 이름으로 검색
    public Optional<Club> getClubByName(String name) {
        return clubRepository.findByName(name);
    }

    // 클럽 삭제
    public void deleteClub(Long clubId) {
        clubRepository.deleteById(clubId);
    }

    public void addClub(ClubDto clubDto) {
        Club club = club.
    }
}
