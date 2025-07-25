package com.example.clubreview.service;


import com.example.clubreview.dto.club.ClubDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.repository.ClubRepository;
import com.example.clubreview.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ClubService {

    private final ClubRepository clubRepository;
    private final ReviewRepository reviewRepository;

    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    public Optional<Club> getClubById(Long id) {
        return clubRepository.findById(id);
    }

    public Club getClubByIdOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 클럽입니다: " + id));
    }

    public Page<Club> getClubsSortedByName(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clubRepository.findAllByOrderByNameAsc(pageable);
    }

    public Page<Club> getClubsSortedByRating(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clubRepository.findAllByOrderByAverageRatingDesc(pageable);
    }

    @Transactional
    public Club addClub(ClubDto clubDto) {
        log.info("클럽 생성: {}", clubDto.getName());

        if (clubRepository.existsByName(clubDto.getName())) {
            throw new DataIntegrityViolationException("이미 존재하는 클럽 이름입니다.");
        }

        Club club = Club.builder()
                .name(clubDto.getName())
                .location(clubDto.getLocation())
                .description(clubDto.getDescription())
                .callNumber(clubDto.getCallNumber())
                .latitude(clubDto.getLatitude())
                .longitude(clubDto.getLongitude())
                .photoUrl(clubDto.getPhotoUrl())
                .averageRating(0.0)
                .build();

        Club savedClub = clubRepository.save(club);
        log.info("클럽 생성 완료: {}", savedClub.getId());

        return savedClub;
    }

    @Transactional
    public Club updateClub(Long id, ClubDto clubDto) {
        log.info("클럽 수정: {}", id);

        Club existingClub = getClubByIdOrThrow(id);

        Club updatedClub = existingClub.toBuilder()
                .name(clubDto.getName())
                .location(clubDto.getLocation())
                .description(clubDto.getDescription())
                .callNumber(clubDto.getCallNumber())
                .latitude(clubDto.getLatitude())
                .longitude(clubDto.getLongitude())
                .photoUrl(clubDto.getPhotoUrl())
                .build();

        Club savedClub = clubRepository.save(updatedClub);
        log.info("클럽 수정 완료: {}", savedClub.getId());

        return savedClub;
    }

    @Transactional
    public void deleteClub(Long id) {
        log.info("클럽 삭제: {}", id);

        Club club = getClubByIdOrThrow(id);

        // 관련 리뷰도 함께 삭제 (cascade로 자동 처리되지만 명시적으로)
        reviewRepository.deleteByClubId(id);
        clubRepository.delete(club);

        log.info("클럽 삭제 완료: {}", id);
    }

    @Transactional
    public void updateClubAverageRating(Long clubId) {
        log.debug("클럽 평균 평점 업데이트: {}", clubId);

        Club club = getClubByIdOrThrow(clubId);
        Double averageRating = reviewRepository.calculateAverageRating(clubId);

        Club updatedClub = club.toBuilder()
                .averageRating(averageRating != null ? averageRating : 0.0)
                .build();

        clubRepository.save(updatedClub);
        log.debug("평균 평점 업데이트 완료: {} -> {}", clubId, averageRating);
    }
}