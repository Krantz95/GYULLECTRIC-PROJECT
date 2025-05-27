package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private  final HttpSession session;
    private final ProductService productService;

    @ModelAttribute
    public void addLoginMemberToModel(Model model) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("loginMember", loginMember);


    }


}
