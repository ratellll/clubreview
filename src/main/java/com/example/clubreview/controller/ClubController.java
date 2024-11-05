package com.example.clubreview.controller;

import com.example.clubreview.dto.ClubDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.service.ClubService;
import org.springframework.data.domain.Page;
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
    public String listClubs(@RequestParam(required = false) String sortBy,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        Page<Club> clubs = (sortBy != null && sortBy.equals("name"))
                ? clubService.getClubsSortedByName(page, size)
                : clubService.getClubsSortedByRating(page, size);
        model.addAttribute("clubs", clubs);
        return "clubs/list";

    }

    // 클럽 상세 정보 조회
    @GetMapping("/{id}")
    public String getClubDetail(@PathVariable Long id, Model model) {
        Club club = clubService.getClubById(id);
        model.addAttribute("club", club);
        model.addAttribute("reviews", club.getReviews());
        return "clubs/details";  // 클럽 상세 페이지 렌더링
    }

    // 클럽 생성폼(어드민전용)
    @GetMapping("/admin/new")
    public String createClubForm(Model model) {
        model.addAttribute("club", new ClubDto());
        return "clubs/create";
    }

    //클럽 생성처리
    @PostMapping("/admin/new")
    public String createClub(@ModelAttribute("club") ClubDto clubDto) {
        clubService.addClub(clubDto);
        return "redirect:/clubs";
    }




}
