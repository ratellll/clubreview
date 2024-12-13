package com.example.clubreview.controller;

import com.example.clubreview.dto.ClubDto;
import com.example.clubreview.entity.Club;
import com.example.clubreview.service.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;


@Controller
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;



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
            map.put("id", club.getId());
            map.put("name", club.getName());
            map.put("location", club.getLocation());
            map.put("callNumber", club.getCallNumber());
            map.put("latitude", club.getLatitude());
            map.put("longitude", club.getLongitude());
            map.put("description", club.getDescription());
            map.put("averageRating", club.getAverageRating());
            map.put("photoUrl", club.getPhotoUrl());
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
    public String createClub(@Valid @ModelAttribute("club") ClubDto clubDto,
                             BindingResult bindingResult,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "clubs/create"; // 유효성 검사 실패 시 현재 페이지 유지
        }

        try {
            // 파일 저장 처리
            if (!file.isEmpty()) {
                String uploadDir = System.getProperty("user.home") + "/uploads/"; // 사용자 디렉토리 내 uploads 폴더
                String originalFileName = file.getOriginalFilename();
                String fileExtension = ""; // 확장자

                // 확장자 추출
                if (originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                // UUID로 고유한 파일 이름 생성
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                Path filePath = Paths.get(uploadDir + uniqueFileName);

                // 디렉토리 생성
                Files.createDirectories(filePath.getParent());

                // 파일 저장
                file.transferTo(filePath.toFile());

                // DTO에 파일 경로 설정
                clubDto.setPhotoUrl("/uploads/" + uniqueFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            bindingResult.reject("fileUploadError", "파일 업로드 중 오류가 발생했습니다.");
            return "clubs/create"; // 파일 업로드 오류 시 다시 등록 페이지로 이동
        }

        clubService.addClub(clubDto); // 서비스로 전달하여 클럽 저장
        redirectAttributes.addFlashAttribute("message", "클럽이 성공적으로 등록되었습니다!");
        return "redirect:/clubs/list"; // 등록 성공 시 리스트 페이지로 이동
    }





    //클럽 수정폼
    @GetMapping("/admin/edit/{id}")
    public String editClubForm(@PathVariable Long id, Model model) {
        Club club = clubService.getClubById(id);

        ClubDto clubDto = new ClubDto(
                club.getId(),
                club.getName(),
                club.getLocation(),
                club.getDescription(),
                club.getCallNumber(),
                club.getLatitude(),
                club.getLongitude(),
                club.getPhotoUrl());

        model.addAttribute("club", clubDto);
        model.addAttribute("clubId", id);
        return "clubs/edit";
    }

    //클럽 수정처리
    @PostMapping("/admin/edit/{id}")
    public String updateClub(@PathVariable Long id, @ModelAttribute("club") ClubDto clubDto,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            //파일처리 - 새로운파일업로드시
            if (!file.isEmpty()) {
                String uploadDir = System.getProperty("user.home") + "/uploads/";
                String originalFileName = file.getOriginalFilename();
                String fileExtension = "";

                //확장자추출
                if (originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                //UUID로 파일이름 다시짓기
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(uploadDir + uniqueFileName);

                Files.createDirectories(filePath.getParent());
                file.transferTo(filePath.toFile());

                //새로저장되는거 dto로 다시넣기
                clubDto.setPhotoUrl("/uploads/" + uniqueFileName);
            } else {
                //파일 수정안할대
                Club existingClub = clubService.getClubById(id);
                clubDto.setPhotoUrl(existingClub.getPhotoUrl());
            }

            // Dto 엔티티 변환하고 서비스로 전달
            clubService.updateClub(id, clubDto);
            redirectAttributes.addFlashAttribute("message", "클럽 정보가 성공적으로 수정되었습니다!");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다.");
            return "redirect:/clubs/admin/edit/" + id;
        }

        return "redirect:/clubs/list";
    }

    //클럽삭제
    @PostMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteClub(@PathVariable Long id) {
        Club club = clubService.getClubById(id);
        clubService.deleteClub(id);
        return "redirect:/clubs/list";
    }


}
