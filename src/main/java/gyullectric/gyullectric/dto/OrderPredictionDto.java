package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.PartName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 발주 예측용 DTO (Flask 예측 결과 + 재고 비교 후 권장 발주량 계산용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPredictionDto {
    private PartName part;         // 부품명
    private int predicted;         // 예측 수요량
    private int currentStock;      // 현재 재고
    private int recommended;       // 권장 발주량 = max(예측 - 재고, 0)
}
