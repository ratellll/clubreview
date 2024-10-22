package com.example.clubreview.service;


import com.example.clubreview.entity.Club;
import com.example.clubreview.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClubService {

    private final ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }
    public Optional<Club> getClubById(Long id) {
        return clubRepository.findById(id);
    }

    public List<Club> getClubsSortedByName() {
        return clubRepository.findAllByOrderByNameAsc();
    }

    public List<Club> getClubsSortedByRating() {
        return clubRepository.findAllByOrderByAverageRatingDesc();
    }

    public Club addClub(Club club) {
        return clubRepository.save(club);
    }

    public void deleteClub(Long id) {
        clubRepository.deleteById(id);
    }
}
