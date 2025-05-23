package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "defectlog_detectiondata")
@IdClass(DefectLogDetectionDataId.class)
public class DefectLog_DetectionData {

    @Id
    @Column(name = "processStep", length = 20, nullable = false)
    private int processStep;

    @Column(name = "currentvalue", nullable = false)
    private double currentvalue;

    @Column(name = "thresholdValue", nullable = false)
    private double thresholdValue;

    @Id
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomalyLevel", length = 20, nullable = false)
    private AnomalyLevel anomalyLevel = AnomalyLevel.STABLE;

    @Column(name = "isAnomaly", nullable = false)
    private boolean isAnomaly = false;

    // 기본 생성자
    public DefectLog_DetectionData() {}

    // getter, setter 생략

}
