package com.example.clubreview.service;


import com.example.clubreview.dto.ClubDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.exception.ClubNotFoundException;
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

    // 모든 클럽 조회
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    // 특정 클럽 조회
    public  Optional<Club> getClubById(Long id) {
        return clubRepository.findById(id);

    }
    //클럽 id조회 메서드로 추출
    public Club getClubByIdOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException("존재하지 않는 클럽입니다. 클럽 ID: " + id));
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
    public void deleteClub(Long id) {
        Club club = getClubByIdOrThrow(id);
        clubRepository.delete(club);
    }

    // 클럽 추가
    public void addClub(ClubDto clubDto) {
        Club club = Club.builder()
                .name(clubDto.getName())
                .location(clubDto.getLocation())
                .description(clubDto.getDescription())
                .callNumber(clubDto.getCallNumber())
                .longitude(clubDto.getLongitude())
                .latitude(clubDto.getLatitude())
                .photoUrl(clubDto.getPhotoUrl())
                .build();
        clubRepository.save(club);
    }

    // 클럽 수정
    public void updateClub(Long id, ClubDto clubDto) {
        Club club = getClubByIdOrThrow(id);
        club.setName(clubDto.getName());
        club.setLocation(clubDto.getLocation());
        club.setDescription(clubDto.getDescription());
        club.setCallNumber(clubDto.getCallNumber());
        club.setLongitude(clubDto.getLongitude());
        club.setLatitude(clubDto.getLatitude());
        club.setPhotoUrl(clubDto.getPhotoUrl());
        clubRepository.save(club);
    }

    public void deleteReview(Long reviewId) {
        clubRepository.deleteById(reviewId);
    }
}
