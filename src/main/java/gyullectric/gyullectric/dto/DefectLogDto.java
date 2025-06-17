package gyullectric.gyullectric.dto;

import lombok.Getter;
import lombok.Setter;

// 불량예측용 1 : 실시간 차트 구현쪽
@Getter
@Setter
public class DefectLogDto {
    private int pressure;         // 압력 (불량예측 차트1.공정용)
    private int upperTemp;        // 상단 금형 온도 (불량예측 차트1.공정용)
    private int lowerTemp;        // 하단 금형 온도 (불량예측 차트1.공정용)
    private int weldingOutput;    // 용접 출력 평균 (불량예측 차트2.용접용)
}