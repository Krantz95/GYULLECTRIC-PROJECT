package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.domain.Supplier;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryForm {
    private Long id;
    @NotNull(message = "부품은 필수입니다")
    @Enumerated(EnumType.STRING)
    private PartName partName;
    @NotNull(message = "수량은 필수입니다")
    private Integer quantity;
    @NotNull(message = "거래처는 필수입니다")
    @Enumerated(EnumType.STRING)
    private Supplier supplier;
}
