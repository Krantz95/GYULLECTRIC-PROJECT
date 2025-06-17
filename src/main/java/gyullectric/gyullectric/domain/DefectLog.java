package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "defect_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 기본 PK, 자동 증가

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;  // 발생 시각 (WarningLogDto에서는 timestamp으로)

    @Column(name = "process_step", length = 20, nullable = false)
    private String processStep;  // 공정 단계 (예: 2공정 - 도장)

    @Column(name = "symptom_item", length = 100, nullable = false)
    private String symptomItem;  // 증상 항목 (예: 압착 압력)

    @Column(name = "symptom", length = 100, nullable = false)
    private String symptom;  // 감지된 현상 (예: 기준치 미만)

    @Column(name = "score", nullable = false)
    private int score;  // 예측 점수

    @Column(name = "warning_level", length = 10, nullable = false)
    private String warningLevel;  // 경고 등급 (주의, 위험 등)

    @Column(name = "inspection_requested", nullable = false)
    private boolean inspectionRequested = false;  // 점검 요청 여부
}