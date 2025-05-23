package gyullectric.gyullectric.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorReportForm {
    private Long id;  // 수정 시 필요

    @NotBlank(message = "에러 코드 입력은 필수입니다")
    private String errorCode;

    @NotBlank(message = "에러 제목은 필수입니다")
    private String title;

    @NotBlank(message = "에러 상세 내용은 필수입니다")
    private String description;

    @NotNull(message = "공정 번호는 필수입니다")
    private Integer processStep;

    @NotBlank(message = "작성자명은 필수입니다")
    private String authorName;

    private String createdAt;  // 화면에서 보여주기용 (optional)
}
