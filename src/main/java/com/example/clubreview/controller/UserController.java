package com.example.clubreview.controller;


import com.example.clubreview.dto.UserDto;
import com.example.clubreview.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String registerUser(@ModelAttribute("user") UserDto userDto) {
        userService.registerUser(userDto);
        return "redirect:/";
    }

}
