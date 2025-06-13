package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.ProductName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderForm {
    private Long id;
    @NotNull(message = "제품명은 필수입니다")
    private ProductName productName;
    @NumberFormat
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;
}
