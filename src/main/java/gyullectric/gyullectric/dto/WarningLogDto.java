package gyullectric.gyullectric.dto;

import lombok.*;

// 불량예측용 2 : 실시간 '경고로그' 테이블 저장용
@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningLogDto {

    private String timestamp;         // 발생 시각 (예: 2025-06-12 10:49)
    private String processStep;       // 공정 단계 (예: 2공정 - 도장)
    private String symptomItem;       // 증상 항목 (예: 압착 압력)
    private String symptom;           // 감지 현상 텍스트 (예: 기준치 미만)
    private int score;                // 점수 (예: 78)
    private String warningLevel;      // 경고 등급 (🟡 주의 등)
    private boolean inspectionRequested; // 점검 요청 여부 (예: false)

    // 간편 생성자 : Builder 없이 빠르게 WarningLogDto 만들기용
    public WarningLogDto(String symptomItem, String symptom, int score, String processStep) {
        this.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.symptomItem = symptomItem;
        this.symptom = symptom;
        this.score = score;
        this.processStep = processStep;
        this.warningLevel = ""; // 필요시 기본값 넣기
    }

}

