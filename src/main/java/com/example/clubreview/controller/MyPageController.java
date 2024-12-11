package com.example.clubreview.controller;


import com.example.clubreview.dto.MyPageDto;
import com.example.clubreview.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public String getMyPage(Principal principal, Model model) {
        String username = principal.getName();

        MyPageDto myPageData = myPageService.getMyPageData(username);
        model.addAttribute("user", myPageData.getUser());
        model.addAttribute("reviews", myPageData.getReviews());
        return "myPage";
    }

}
