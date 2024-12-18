package com.example.clubreview.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, Model model) {
        model.addAttribute("errorMessage", "파일 크기가 너무 큽니다. 10MB이하로 등록해주십쇼");
        return "clubs/create";
    }

    @ExceptionHandler(ClubNotFoundException.class)
    public String handlerClubNotFoundException(ClubNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "clubs/list";
    }
}
