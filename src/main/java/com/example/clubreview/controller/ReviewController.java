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

    // 클럽별 리뷰를 보여주는 페이지
    @GetMapping("/club/{clubId}")
    public String getReviewsForClub(@PathVariable Long clubId, Model model) {
        List<Review> reviews = reviewService.getReviewsForClub(clubId);
        model.addAttribute("reviews", reviews);
        return "reviews";  // reviews.html 파일을 렌더링
    }

    // 새로운 리뷰 추가
    @PostMapping
    public String createReview(Review review) {
        reviewService.createReview(review);
        return "redirect:/reviews/club/" + review.getClub().getId();  // 리뷰 페이지로 리다이렉션
    }
}
