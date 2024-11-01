package com.example.clubreview.controller;

import com.example.clubreview.entity.Club;
import com.example.clubreview.service.ClubService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    // 클럽 목록 조회 (이름순 또는 별점순 정렬)
    @GetMapping
    public String listClubs(@RequestParam(required = false)String sortBy, Model model) {
        List<Club> clubs = sortBy != null && sortBy.equals("name")
                ? clubService.getClubsSortedByName()
                : clubService.getClubsSortedByRating();
        model.addAttribute("clubs", clubs);
        return "club/list";

    }

    // 클럽 상세 정보 조회
    @GetMapping("/{id}")
    public String getClubDetail(@PathVariable Long id, Model model) {
        Club club = clubService.getClubById(id)
                .orElseThrow(() -> new RuntimeException("클럽을 찾을 수 없습니다."));
        model.addAttribute("club", club);
        model.addAttribute("reviews", club.getReviews());
        return "club-detail";  // 클럽 상세 페이지 렌더링
    }


}
