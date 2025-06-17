package gyullectric.gyullectric.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerForm {
    private Long id;
    @NotEmpty(message = "답변은 필수입니다")
    private String content;
}
