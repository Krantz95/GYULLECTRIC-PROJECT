package gyullectric.gyullectric.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryDto {
    private int totalLots;    // 총 로트 수 (제품 수)
    private int finishedLots; // 완제품 수 (3공정 모두 OK)
    private int ngLots;       // 불량품 수 (3공정 중 NG 하나라도 존재)

    // 편의 메서드
    public int getFinishedLots() {
        return finishedLots;
    }

    public int getNgLots() {
        return ngLots;
    }

    public int getTotalLots() {
        return totalLots;
    }
}
