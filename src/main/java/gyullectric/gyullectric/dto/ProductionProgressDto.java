package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.ProductName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionProgressDto {

    private ProductName productName;

    private int producedCount;

    private int totalTarget;

    private double progressRate;    // ex. 0.75 → 75%

    private double efficiencyRate;  // 생산 효율성

    private String statusMessage;   // 상태 메시지 (ex. 정상 진행 중 / 지연 우려)
}
