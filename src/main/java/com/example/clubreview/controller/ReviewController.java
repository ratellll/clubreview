package com.example.clubreview.controller;


import com.example.clubreview.dto.ReviewDto;
import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.security.CustomUserDetails;
import com.example.clubreview.service.ReviewService;
import com.example.clubreview.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;


    // 리뷰 등록 처리
    @PostMapping("/add")
    public String addReview(@RequestParam Long clubId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            LocalDateTime createTime = LocalDateTime.now();

            reviewService.addReview(clubId, user, rating, comment,createTime);
            redirectAttributes.addFlashAttribute("message", "리뷰가 등록되었습니다");
        }catch (DuplicateReviewException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/clubs/" + clubId;
    }

    //사용자 리뷰수정
    @PostMapping("/user/edit/{id}")
    public String editUserReview(@PathVariable Long id, @Valid ReviewDto reviewDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "redirect:/clubs/" + reviewDto.getClubId();
        }
        // 사용자 검증
        reviewService.userReviewAccess(id, principal.getName());

        reviewService.updateReview(id, reviewDto);
        return "redirect:/clubs/" + reviewDto.getClubId();
    }

    //유저 리뷰 삭제
    @PostMapping("/user/delete/{id}")
    public String deleteUserReview(@PathVariable Long id, @Valid ReviewDto reviewDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "redirect:/clubs/" + reviewDto.getClubId();
        }
        // 사용자 검증
        reviewService.userReviewAccess(id, principal.getName());

        reviewService.deleteReview(id);
        return "redirect:/clubs/" + reviewDto.getClubId();
    }
    // 리뷰 어드민 수정처리
    @PostMapping("/admin/edit/{id}")
    public String editReview(@PathVariable Long id,
                             @RequestParam String comment,
                             @RequestParam int rating,
                             @RequestParam Long clubId) {
        Review updatedReview = reviewService.adminUpdateReview(id, comment, rating);
        return "redirect:/clubs/" + clubId; // 클럽으로 다시 이동
    }

    //리뷰 삭제
    @PostMapping("/admin/delete/{id}")
    public String deleteReview(@PathVariable Long id, @RequestParam Long clubId, RedirectAttributes redirectAttributes) {
        try {
            reviewService.adminDeleteReview(id);
            redirectAttributes.addFlashAttribute("message", "리뷰가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/clubs/"+ clubId;
    }



}
