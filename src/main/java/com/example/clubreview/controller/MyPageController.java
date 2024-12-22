package com.example.clubreview.controller;


import com.example.clubreview.dto.MyPageDto;
import com.example.clubreview.dto.ReviewDto;
import com.example.clubreview.dto.UserDto;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.service.MyPageService;
import com.example.clubreview.service.ReviewService;
import com.example.clubreview.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping("/list")
    public String getMyPage(Principal principal, Model model) {
        MyPageDto myPageData = myPageService.getMyPageData(principal.getName());
        model.addAttribute("user", myPageData.getUser());
        model.addAttribute("reviews", myPageData.getReviews());
        return "mypage/list";
    }

    @PostMapping("/editNickname")
    public String editNickname(@RequestParam String nickname,  RedirectAttributes redirectAttributes, Principal principal) {
        try {
            userService.validateNickname(nickname);
            myPageService.updateNickName(principal.getName(), nickname);
            redirectAttributes.addFlashAttribute("message", "수정이 완료되었습니다.");
            return "redirect:/mypage/list";
        } catch (DuplicateReviewException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/mypage/list";
    }

    @PostMapping("/editPassword")
    public String editPassword(@Valid @ModelAttribute("user") UserDto userDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) {
        try {
            myPageService.updatePassword(principal.getName(), userDto.getPassword());
            redirectAttributes.addFlashAttribute("message", "수정이 완료되었습니다.");
            return "redirect:/mypage/list";
        } catch (DuplicateReviewException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/mypage/list"; // 회원가입 페이지로 리다이렉트
    }

    @PostMapping("/reviews/edit")
    public String editReview(@Valid ReviewDto reviewDto, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 수정에 실패했습니다. 입력값을 확인해주세요.");
            return "redirect:/mypage/list";
        }
        try {
            reviewService.updateReview(reviewDto.getId(), reviewDto,principal.getName());
            redirectAttributes.addFlashAttribute("message", "리뷰가 수정되었습니다.");
        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/mypage/list";
    }
    @PostMapping("/reviews/delete/{id}")
    public String deleteUserReview(@PathVariable Long id, @Valid ReviewDto reviewDto, BindingResult bindingResult,RedirectAttributes redirectAttributes, Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제에 실패했습니다. 입력값을 확인해주세요.");
            return "redirect:/mypage/list";
        }
        // 사용자 검증

        reviewService.deleteReview(id);
        return "redirect:/mypage/list";
    }
}
