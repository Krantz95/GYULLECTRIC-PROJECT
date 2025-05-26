package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.ProductName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderForm {
    private Long id;
    @NotNull(message = "제품명은 필수입니다")
    private ProductName productName;
    @NotNull(message = "수량은 필수입니다")
    private Integer quantity;
}
