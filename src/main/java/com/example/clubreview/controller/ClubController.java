package com.example.clubreview.controller;

import com.example.clubreview.dto.ClubDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.service.ClubService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;


@Controller
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    // 클럽 목록 조회 (이름순 또는 별점순 정렬)
    @GetMapping("/list")
    public String listClubs(@RequestParam(required = false) String sortBy,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        Page<Club> clubs = (sortBy != null && sortBy.equals("name"))
                ? clubService.getClubsSortedByName(page, size)
                : clubService.getClubsSortedByRating(page, size);

        List<Map<String, Object>> clubLocations = clubs.getContent().stream().map(club -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", club.getName());
            map.put("location", club.getLocation());
            map.put("callNumber", club.getCallNumber());
            map.put("averageRating", club.getAverageRating());
            map.put("latitude", club.getLatitude());
            map.put("longitude", club.getLongitude());
            return map;
        }).toList();
        model.addAttribute("clubs", clubs);
        model.addAttribute("clubLocations", clubLocations); // Page에서 사용하는 메서드 해당 데이터가 포함된 list를가져옴
        System.out.println(clubLocations);
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

    //클럽 수정폼
    @GetMapping("/admin/edit/{id}")
    public String editClubForm(@PathVariable Long id, Model model) {
        Club club = clubService.getClubById(id);
        ClubDto clubDto = new ClubDto(club.getName(), club.getLocation(), club.getDescription(), club.getCallNumber());

        model.addAttribute("club", clubDto);
        model.addAttribute("clubId", id);
        return "clubs/edit";
    }

    //클럽 수정처리
    @PostMapping("/admin/edit/{id}")
    public String updateClub(@PathVariable Long id, @ModelAttribute("club") ClubDto clubDto) {
        clubService.updateClub(id, clubDto);
        return "redirect:/clubs/list";
    }

    //클럽삭제
    @PostMapping("/admin/delete/{id}")
    public String deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return "redirect:/clubs/list";
    }

    //클럽위치매핑
    @GetMapping("/map")
    public String getClubsForMap(Model model) {
        List<Club> clubs = clubService.getAllClubs();
        model.addAttribute("clubs", clubs);
        return "clubs/map";
    }
}
