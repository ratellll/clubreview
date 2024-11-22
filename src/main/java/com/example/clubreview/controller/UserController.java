package com.example.clubreview.controller;


import com.example.clubreview.dto.UserDto;
import com.example.clubreview.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입 폼이동
    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "/users/register";
    }

    //회원가입 처리
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/register"; // 회원가입 페이지로 리다이렉트
        }
    }

}
