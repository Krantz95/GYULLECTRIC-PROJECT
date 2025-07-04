package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.PositionName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "password")
public class MyPageForm {
    private Long id;
    @NotBlank(message = "회원아이디는 필수입니다")
    private String loginId;
    @NotEmpty(message = "이름은 필수 입니다")
    private String name;

    private String password;

    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9]).{4,8}",
            message = "비밀번호는 영문 대/소문자 포함 4자리이상 8자리 이하로 입력하세요")

    private String newPassword;

    private String passwordCheck;

    private String phone;

    private PositionName positionName;

}
