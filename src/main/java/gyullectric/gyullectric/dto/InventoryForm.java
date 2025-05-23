package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.domain.Supplier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryForm {
    private Long id; // 수정 시 필요

    @NotNull(message = "자재명은 필수입니다")
    private PartName partName;

    @NotNull(message = "공급업체는 필수입니다")
    private Supplier supplier;

    @Min(value = 0, message = "수량은 1 이상이어야 합니다")
    private int quantity;

    private LocalDateTime orderedAt; // 발주 일시, 선택 가능 (기본 현재시간)
}
