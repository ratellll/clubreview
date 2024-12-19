package com.example.clubreview.controller;


import com.example.clubreview.dto.UserDto;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // 회원가입 폼이동
    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "/users/register";
    }

    // 통합 중복체크
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkDuplicate(@RequestParam String type, @RequestParam String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(false);
            }
            Boolean isAvailable = switch (type) {
                case "username" -> userService.idIsFine(value);
                case "phoneNumber" -> userService.phoneIsFine(value);
                case "nickname" -> userService.nickNameIsFine(value);
                default -> throw new IllegalArgumentException("유효하지 않은 타입입니다.");
            };
            return ResponseEntity.ok(isAvailable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(false);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }


    //회원가입 처리
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDto userDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                System.err.println("Error: " + error.getDefaultMessage());
            });

            redirectAttributes.addFlashAttribute("errorMessage", "입력값을 확인해주세요.");
            return "redirect:/users/register";
        } // 회원가입 형식체크 추가
        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/";
        } catch (DuplicateReviewException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/register"; // 회원가입 페이지로 리다이렉트
        }
    }
    }

