package gyullectric.gyullectric.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorCommentForm {
    private Long id;  // 수정 시 필요할 수도 있어서 넣었어요
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String commentText;
    private Long errorReportId; // 어떤 에러 리포트에 달린 댓글인지 연결
    private String authorName;  // 작성자명
    private String createdAt;   // 화면에서 보여주기용 (optional)
}
