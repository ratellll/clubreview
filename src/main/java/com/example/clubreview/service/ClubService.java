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


    // 모든클럽 조회
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    // 특정 클럽 조회
    public Club getClubById(Long id) {
        return clubRepository.findById(id).orElseThrow(() -> new RuntimeException("Club not found"));
    }

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
        Club club = Club.builder()
                .name(clubDto.getName())
                .location(clubDto.getLocation())
                .description(clubDto.getDescription())
                .callNumber(clubDto.getCallNumber())
                .build();
        clubRepository.save(club);
    }
    //수정
    public void updateClub(Long id, ClubDto clubDto) {
        Club club = getClubById(id);
        club.setName(clubDto.getName());
        club.setLocation(clubDto.getLocation());
        club.setDescription(clubDto.getDescription());
        club.setCallNumber(clubDto.getCallNumber());
        clubRepository.save(club);
    }


}
