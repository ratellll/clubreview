package com.example.clubreview.controller;

import com.example.clubreview.domain.Club;
import com.example.clubreview.service.ClubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public List<Club> getAllClubs() {
        return clubService.getAllClubs();
    }

    @PostMapping
    public Club createClub(@RequestBody Club club){
        return clubService.createClub(club);
    }
}
