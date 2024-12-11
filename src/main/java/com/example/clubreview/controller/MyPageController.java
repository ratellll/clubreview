package com.example.clubreview.controller;


import com.example.clubreview.dto.MyPageDto;
import com.example.clubreview.dto.UserDto;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.service.MyPageService;
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

    @GetMapping
    public String getMyPage(Principal principal, Model model) {
        String username = principal.getName();

        MyPageDto myPageData = myPageService.getMyPageData(username);
        model.addAttribute("user", myPageData.getUser());
        model.addAttribute("reviews", myPageData.getReviews());
        return "myPage";
    }

    @GetMapping("/checkNickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean nickNameIsFine = userService.nickNameIsFine(nickname);
        return ResponseEntity.ok(nickNameIsFine);
    }

    @PostMapping("/edit")
    public String editMyStub(@Valid @ModelAttribute("user") UserDto userDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                System.err.println("Error: " + error.getDefaultMessage());
            });

            redirectAttributes.addFlashAttribute("errorMessage", "입력값을 확인해주세요.");
            return "redirect:/mypage";
        } // 회원가입 형식체크 추가

        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/mypage";
        } catch (DuplicateReviewException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage"; // 회원가입 페이지로 리다이렉트
        }

    }
}
