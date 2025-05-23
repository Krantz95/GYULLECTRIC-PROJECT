package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.dto.MyPageForm;
import gyullectric.gyullectric.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final MemberService memberService;

    @GetMapping("/{id}/edit")
    public String myPage(@PathVariable("id")Long id, Model model) {
        Members myPageMember = memberService.oneFindMembers(id).orElseThrow(()->new IllegalArgumentException("해당 아이디를 찾을 수 없습니다"));

        MyPageForm myPageForm = MyPageForm.builder()
                .id(myPageMember.getId())
                .loginId(myPageMember.getLoginId())
                .name(myPageMember.getName())
                .password(myPageMember.getPassword())
                .phone(myPageMember.getPhone())
                .positionName(myPageMember.getPositionName())
                .build();
        model.addAttribute("myPageForm", myPageForm);
        return "user/myPageForm";
    }
    @PostMapping("/{id}/edit")
    public String updateUser(Model model, @Valid @ModelAttribute("myPageForm")MyPageForm myPageForm, BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            Members members = memberService.oneFindMembers(myPageForm.getId()).orElseThrow(()->new IllegalArgumentException("해당 아이디를 찾을 수 없습니다"));

            MyPageForm myPageForms = MyPageForm.builder()
                    .id(members.getId())
                    .loginId(members.getLoginId())
                    .name(members.getName())
                    .password(members.getPassword())
                    .phone(members.getPhone())
                    .positionName(members.getPositionName())
                    .build();
            model.addAttribute("myPageForm", myPageForms);
        }
        Members members = memberService.oneFindMembers(myPageForm.getId()).orElseThrow(()->new IllegalArgumentException("해당 아이디를 찾을 수 없습니다"));

        // 비밀번호 변경 요청이 있을 때만 확인
        if (myPageForm.getNewPassword() != null && !myPageForm.getNewPassword().isBlank()) {
            if (!myPageForm.getNewPassword().equals(myPageForm.getPasswordCheck())) {
                bindingResult.rejectValue("passwordCheck", "password.mismatch", "비밀번호가 일치하지 않습니다.");
                return "user/myPageForm";
            }
            // 새 비밀번호로 갱신
            members = members.toBuilder()
                    .password(myPageForm.getNewPassword()) // 반드시 암호화 필요!
                    .phone(myPageForm.getPhone())
                    .build();
        } else {
            // 비밀번호 변경 없음
            members = members.toBuilder()
                    .phone(myPageForm.getPhone())
                    .build();
        }

        memberService.signup(members);
        return "redirect:/login";
    }

}
