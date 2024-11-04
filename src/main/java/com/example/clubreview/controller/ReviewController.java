package com.example.clubreview.controller;


import com.example.clubreview.entity.Review;
import com.example.clubreview.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 리뷰 생성폼 페이지
    @GetMapping("/new")
    public String createReviewForm(@RequestParam Long clubId, Model model) {
        model.addAttribute("clubId", clubId);
        model.addAttribute("review", new Review());
        return "reviews/create";
    }

    //리뷰 생성처리
    @PostMapping("/new")
    public String createReview(@RequestParam Long userId,
                               @RequestParam Long clubId,
                               @RequestParam String comment,
                               @RequestParam int rating) {
        reviewService.addReview(userId, clubId, comment, rating);
        return "redirect:/clubs/" + clubId; //생성후 해당클럽 페이지로이동
    }

    //리뷰 수정 폼 이동
    @GetMapping("/edit/{id}")
    public String editReviewForm(@PathVariable Long id, Model model) {
        Review review = reviewService.getReviewByUserId(id);
        model.addAttribute("review", review);
        return "reviews/edit";
    }

    // 리뷰  수정처리
    @PostMapping("/edit/{id}")
    public String editReview(@PathVariable Long id,
                             @RequestParam String comment,
                             @RequestParam int rating) {
        Review updatedReview = reviewService.updateReview(id, comment, rating);
        return "redirect:/clubs/" + updatedReview.getClub().getId(); // 클럽으로 다시 이동
    }

    //리뷰 삭제
    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        Review review = reviewService.getReviewByUserId(id);
        reviewService.deleteReview(id);
        return "redirect:/clubs/" + review.getClub().getId();
    }
}
