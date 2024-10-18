package com.example.clubreview.service;


import com.example.clubreview.domain.Club;
import com.example.clubreview.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubService {

    private final ClubRepository clubRepository;

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }
    public List<Club> getClubsSortedById(Long id) {
        return clubRepository.findAllByOrderById(id);
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
}
