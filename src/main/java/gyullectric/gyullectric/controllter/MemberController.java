package gyullectric.gyullectric.controllter;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.PositionName;
import gyullectric.gyullectric.dto.MembersForm;
import gyullectric.gyullectric.dto.MembersUpdateForm;
import gyullectric.gyullectric.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Controller
@RequiredArgsConstructor

@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    //목록보기
    @GetMapping("/list")
    public String listMembers(Model model, HttpSession session,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "kw", defaultValue = "") String kw,
                              @RequestParam(value = "type", required = false, defaultValue = "") String type
    ) {

        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return "redirect:/";
        }
        if (loginMember.getPositionName() != PositionName.ADMIN) {
            return "redirect:/";
        }

        Page<Members> paging = memberService.getList(page, kw, type);
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("type", type);

        return "member/memberList";
    }

    //수정
    @GetMapping("/{id}/edit")
    public String editMemberForm(@PathVariable("id") Long id, Model model) {
        Members members = memberService.oneFindMembers(id).orElseThrow(() -> new IllegalArgumentException("해당 아이디를 찾을 수 없습니다"));

        MembersForm membersForm = MembersForm.builder()
                .id(members.getId())
                .loginId(members.getLoginId())
                .password(members.getPassword())
                .name(members.getName())
                .phone(members.getPhone())
                .positionName(members.getPositionName())
                .build();
        model.addAttribute("membersForm", membersForm);
        return "member/updateMemberForm";
    }

    // 수정저장
    @PostMapping("/{id}/edit")
    public String postEditForm(Model model, @Valid @ModelAttribute("membersForm") MembersUpdateForm membersForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Members members = memberService.oneFindMembers(membersForm.getId()).orElseThrow(() -> new IllegalArgumentException("찾는 해당 아이디가 없습니다"));
            membersForm.setId(members.getId());
            membersForm.setLoginId(members.getLoginId());
            membersForm.setPassword(members.getPassword());
            membersForm.setName(members.getName());
            membersForm.setPhone(members.getPhone());
            membersForm.setPositionName(members.getPositionName());
            model.addAttribute("membersForm", membersForm);
            return "member/updateMemberForm";
        }
        Members members = memberService.oneFindMembers(membersForm.getId()).orElseThrow(() -> new IllegalArgumentException("찾는 해당 아이디가 없습니다"));

        String newPassword = membersForm.getPassword();
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = members.getPassword();
        }

        Members updateMembers = members.toBuilder()
                .id(membersForm.getId())
                .loginId(membersForm.getLoginId())
                .password(newPassword)
                .name(membersForm.getName())
                .phone(membersForm.getPhone())
                .positionName(membersForm.getPositionName())
                .build();
        memberService.signup(updateMembers);

        return "redirect:/members/list";
    }

    //    delete
    @GetMapping("/{id}/delete")
    public String deleteMember(@PathVariable("id") Long id, Model model) {
        Members members = memberService.oneFindMembers(id).orElseThrow(() -> new IllegalArgumentException("해당 아이디를 찾을 수 없습니다"));

        memberService.deleteMembers(members.getId());
        return "redirect:/members/list";
    }
}