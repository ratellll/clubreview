package com.example.clubreview.controller;


import com.example.clubreview.entity.User;
import com.example.clubreview.exception.UserNotFoundException;
import com.example.clubreview.repository.UserRepository;
import com.example.clubreview.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;


    //유저 목록
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public String getUserList(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    //유저탈퇴
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "유저가 성공적으로 삭제되었습니다.");
        } catch (UserNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "유저 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/users/list";
    }

    //유저 정지
    @PostMapping("/ban/{id}")
    public String banUser(@PathVariable Long id, @RequestParam int days, RedirectAttributes redirectAttributes) {
        try {
            userService.banUser(id, days);
            redirectAttributes.addFlashAttribute("message", "유저가" + days + " 일 동안 정지됩니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원정지 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/users/list";
    }

    //유저 정지해제
    @PostMapping("/unban/{id}")
    public String unbanUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.unbanUser(id);
            redirectAttributes.addFlashAttribute("message", "유저 정지가  해제 됩니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "유저 정지 도중 오류가 발생했습니다.");
        }
        return "redirect:/admin/users/list";
    }
}
