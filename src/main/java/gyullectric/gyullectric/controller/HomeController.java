package gyullectric.gyullectric.controller;


import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.PositionName;
import gyullectric.gyullectric.dto.LoginForm;
import gyullectric.gyullectric.dto.MembersForm;
import gyullectric.gyullectric.service.LoginService;
import gyullectric.gyullectric.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@ControllerAdvice
@RequiredArgsConstructor
public class HomeController {
    private final MemberService memberService;
    private final LoginService loginService;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (loginMember != null) {
                model.addAttribute("loginMember", loginMember);
            }
        }
        return "home";
    }

    @GetMapping("/login")
    public String loginForm(Model model, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember != null){
            return "home";
        } else {
            model.addAttribute("loginForm", new LoginForm());
            return "user/loginForm";
        }

    }
    @PostMapping("/login")
    public String loginPost(@Valid @ModelAttribute LoginForm loginForm, BindingResult bindingResult, HttpServletRequest request, Model model){
        if(bindingResult.hasErrors()){
            model.addAttribute("loginForm", loginForm);
            return "user/loginForm";
        }
        Members loginMember = loginService.login(loginForm.getLoginId(), loginForm.getPassword());

        if(loginMember == null){
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다");
            return "user/loginForm";
        }
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
        model.addAttribute("loginForm", loginForm);

        return "redirect:/dashboard/main";
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession(false);

        if(session != null){
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("membersForm", new MembersForm());
        return "member/createMemberForm";
    }

    @PostMapping("/signup")
    public String signupPost(@Valid MembersForm membersForm, BindingResult bindingResult, Model model
    ){
        // 비밀번호와 비밀번호 확인이 다르면 오류 추가
        if (!membersForm.getPassword().equals(membersForm.getPasswordCheck())) {
            bindingResult.rejectValue("passwordCheck", "error.passwordCheck", "비밀번호가 일치하지 않습니다.");
        }
        if (memberService.validateDuplicateMember(membersForm.getLoginId()) != null) {
            model.addAttribute("duplicateMessage", "이미 사용 중인 아이디입니다.");
            model.addAttribute("isDuplicate", true);
            return "member/createMemberForm";
        }
        if(bindingResult.hasErrors()){
            return "member/createMemberForm";
        }

        Members members = Members.builder()
                .loginId(membersForm.getLoginId())
                .name(membersForm.getName())
                .password(membersForm.getPassword())
                .phone(membersForm.getPhone())
                .positionName(PositionName.ENGINEER)
                .createDate(LocalDateTime.now())
                .build();
        memberService.signup(members);
        model.addAttribute("member", members);

        return "redirect:/login";
    }
    @GetMapping("/signup/check")
    public String checkDuplicate(Model model,@ModelAttribute("membersForm") MembersForm membersForm, BindingResult bindingResult) {
        // loginId 필드만 수동 검증
        if (membersForm.getLoginId() == null || membersForm.getLoginId().trim().isEmpty()) {
            bindingResult.rejectValue("loginId", "required", "아이디는 필수 입력값입니다");
            return "member/createMemberForm";
        }
        String loginId = membersForm.getLoginId();
        boolean isDuplicate = memberService.validateDuplicateMember(loginId) != null;

        if(isDuplicate){
            model.addAttribute("duplicateMessage", "이미 사용중인 아이디입니다");
        } else{
            model.addAttribute("duplicateMessage", "사용 가능한 아이디입니다");
        }
        membersForm.setLoginId(loginId);
        model.addAttribute("membersForm", membersForm);
        model.addAttribute("isDuplicate", isDuplicate);

        return "member/createMemberForm";
    }

}




