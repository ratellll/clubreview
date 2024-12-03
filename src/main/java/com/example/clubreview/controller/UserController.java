package com.example.clubreview.controller;


import com.example.clubreview.dto.UserDto;
import com.example.clubreview.exception.DuplicateReviewException;
import com.example.clubreview.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    //아이디 중복체크
    @GetMapping("/register/idCheck")
    public ResponseEntity<Boolean> registerId(@RequestParam String username) {
        System.out.println("Checking username: " + username);
        boolean idIsFine = userService.idIsFine(username);
        System.out.println("ID is fine: " + idIsFine);
        return ResponseEntity.ok(idIsFine);

    }
    //회원가입 처리
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserDto userDto, RedirectAttributes redirectAttributes, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "입력값을 확인해주세요.");
            return "redirect:/users/register";
        } // 회원가입 형식체크 추가

        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/login";
        } catch (DuplicateReviewException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/register"; // 회원가입 페이지로 리다이렉트
        }
    }

}
