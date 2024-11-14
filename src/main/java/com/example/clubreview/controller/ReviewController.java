package com.example.clubreview.controller;


import com.example.clubreview.entity.Review;
import com.example.clubreview.entity.User;
import com.example.clubreview.service.ReviewService;
import com.example.clubreview.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    // 리뷰 등록 처리
    @PostMapping("/add")
    public String addReview(@RequestParam Long clubId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            Principal principal) {
        // 현재 로그인한 사용자의 이름으로 User 객체 가져오기
        User user = userService.findByUsername(principal.getName());

        // Review 객체 생성 및 저장
        reviewService.addReview(clubId, user, rating, comment);

        // 클럽 상세 페이지로 리디렉션
        return "redirect:/clubs/" + clubId;
    }

    //리뷰 수정 폼 이동
    @GetMapping("/edit/{id}")
    public String editReviewForm(@PathVariable Long id, Model model) {
        Review review = reviewService.getReviewById(id);
        model.addAttribute("review", review);
        return "reviews/edit";
    }

    // 리뷰  수정처리
    @PostMapping("/admin/edit/{id}")
    public String editReview(@PathVariable Long id,
                             @RequestParam String comment,
                             @RequestParam int rating) {
        Review updatedReview = reviewService.updateReview(id, comment, rating);
        return "redirect:/clubs/" + updatedReview.getClub().getId(); // 클럽으로 다시 이동
    }

    //리뷰 삭제
    @PostMapping("/admin/delete/{id}")
    public String deleteReview(@PathVariable Long id, @RequestParam Long clubId, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("message", "리뷰가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/clubs/" + clubId;
    }

    //리뷰삭제
//    @PostMapping("/admin/reviews/delete/{reviewId}")
//    public String deleteReview(@PathVariable Long reviewId) {
//        clubService.deleteReview(reviewId);
//        return "redirect:/clubs/list";
//    }

}
