package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.Priority;
import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProductName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorReportForm {
    private Long id;

    private List<ProcessLog> processLogList;
    @NotNull(message = "긴급도는 필수입니다")
    private Priority priority;
    @NotBlank(message = "에러 제목은 필수입니다")
    private String title;
    @NotBlank(message = "제품명은 필수입니다")
    private String productName;

    @NotBlank(message = "에러 상세 내용은 필수입니다")
    private String description;

    @NotNull(message = "공정 번호는 필수입니다")
    private Integer processStep;

    private String createdAt;
}

