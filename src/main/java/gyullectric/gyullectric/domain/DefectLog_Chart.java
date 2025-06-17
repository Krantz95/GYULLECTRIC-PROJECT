package gyullectric.gyullectric.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectLog_Chart {

    @Id
    @GeneratedValue
    private Long id;                      // 고유 식별자 (PK), 자동 생성됨

    private int processStep;             // 공정 단계 (예: 1: 주조, 2: 용접, 3: 도장 등)
    private int defectScore;             // 불량 점수 (0~100 범위, 높을수록 심각)
    private String summary;              // 불량 요약 설명 (예: "압력 기준 초과")
    private LocalDateTime createdAt;     // 데이터 생성 시간 (타임스탬프 기록용)
    private boolean inspectionRequested; // 점검 요청 여부 (true이면 점검 필요 상태)
}
