package gyullectric.gyullectric;

import gyullectric.gyullectric.dto.MyPageForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class  MyPageFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return MyPageForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MyPageForm form = (MyPageForm) target;

        if (form.getPassword() == null || form.getPassword().isBlank()) {
            errors.rejectValue("password", "required", "기존 비밀번호는 필수입니다.");
        }

        // 새 비밀번호를 입력한 경우에만 검사
        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
            if (!form.getNewPassword().matches("(?=.*[a-zA-Z])(?=.*[0-9]).{4,8}")) {
                errors.rejectValue("newPassword", "invalid.password", "비밀번호는 영문 대/소문자 포함 4자리 이상 8자리 이하로 입력하세요");
            }

            if (form.getPasswordCheck() == null || !form.getNewPassword().equals(form.getPasswordCheck())) {
                errors.rejectValue("passwordCheck", "mismatch", "비밀번호 확인이 일치하지 않습니다.");
            }
        }

        // 전화번호는 비어있으면 기본값이 있는 경우만 허용 (예: 수정 시 기존 번호가 그대로 유지되는 경우)
        if (form.getPhone() == null || form.getPhone().isBlank()) {
            errors.rejectValue("phone", "required", "전화번호는 필수입니다.");
        }
    }
}
