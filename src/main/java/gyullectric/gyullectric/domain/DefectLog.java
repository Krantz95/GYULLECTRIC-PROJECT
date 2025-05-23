package gyullectric.gyullectric.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;  // PK + 생성 시각

    @Column(name = "process_step", length = 20, nullable = false)
    private String processStep;  // 공정 번호 (VARCHAR(20))

    @Column(name = "symptom", length = 100, nullable = false)
    private String symptom;  // 이상현상 감지

    @Column(name = "inspection_requested", nullable = false)
    private boolean inspectionRequested = false;  // 점검요청여부
}