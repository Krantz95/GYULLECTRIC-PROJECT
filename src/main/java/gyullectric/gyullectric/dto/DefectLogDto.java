package gyullectric.gyullectric.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefectLogDto {
    private int pressure;         // 압력 (주조데이터차트용)
    private int upperTemp;        // 상단 금형 온도 (주조데이터차트용)
    private int lowerTemp;        // 하단 금형 온도 (주조데이터차트용)
    private int weldingOutput;    // 용접 출력 평균 (배터리팩데이터차트용)
}