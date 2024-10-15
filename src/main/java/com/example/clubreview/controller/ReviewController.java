package com.example.clubreview.controller;


import com.example.clubreview.domain.Review;
import com.example.clubreview.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/club/{clubId}")
    public List<Review> getReviewsForClub(@PathVariable Long clubId) {
        return reviewService.getReviewsForClub(clubId);
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }
}
