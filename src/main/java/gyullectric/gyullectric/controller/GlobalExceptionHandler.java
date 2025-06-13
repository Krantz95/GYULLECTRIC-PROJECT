package gyullectric.gyullectric.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException e, Model model) {
        log.warn("예외 발생: {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "product/orderNew";
    }
}
