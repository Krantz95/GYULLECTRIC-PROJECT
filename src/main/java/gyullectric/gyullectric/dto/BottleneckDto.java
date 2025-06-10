package gyullectric.gyullectric.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BottleneckDto {
    private Map<Integer, Double> processTimeMap; // 공정번호 → 평균 처리 시간 (초)
    private Map<Integer, Long> errorMap;         // 공정번호 → 에러 수
    private int bottleneck;                      // 병목 공정번호
    private String mainCause;                    // 병목 원인 설명
}
