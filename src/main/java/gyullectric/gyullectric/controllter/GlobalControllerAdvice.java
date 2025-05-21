package gyullectric.gyullectric.controllter;

import gyullectric.gyullectric.domain.Members;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private  final HttpSession session;

    @ModelAttribute
    public void addLoginMemberToModel(Model model) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("loginMember", loginMember);
    }
}
