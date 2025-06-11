package gyullectric.gyullectric.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionKpiDto {
    private int totalOrder;           // 🎯 일일 목표 생산량 (고정값: 300)
    private int completed;            // ✅ 현재까지 생산된 양품 수량
    private int achievementRate;      // 📈 달성률: (completed / totalOrder) * 100

    private double currentSpeed;      // ⏱ 분당 생산속도 (예: 1.23대/분)
    private int expectedRate;         // 📊 납기 내 생산 가능 비율 (0~100%)
    private String estimatedTime;     // ⏳ 남은 생산 예상 시간 ("X시간 Y분" 형식)
    private boolean onTime;           // 🕒 납기 가능 여부 (true=정시 가능, false=지연 예상)

    // 🎯 필요한 필드만 받는 커스텀 생성자
    public ProductionKpiDto(int totalOrder, int completed, int achievementRate) {
        this.totalOrder = totalOrder;
        this.completed = completed;
        this.achievementRate = achievementRate;
    }
}
