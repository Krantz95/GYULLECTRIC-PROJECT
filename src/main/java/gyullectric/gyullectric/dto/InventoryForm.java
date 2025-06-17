package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.domain.Supplier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryForm {

    private Long id; // 수정 시 식별자용

    @NotNull(message = "부품은 필수입니다")
    private PartName partName;


    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;

    @NotNull(message = "공급업체는 필수입니다")
    private Supplier supplier;

    private LocalDateTime orderedAt; // 발주 일시 (선택, 기본값은 현재시간)
}
