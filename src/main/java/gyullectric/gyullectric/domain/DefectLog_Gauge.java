package gyullectric.gyullectric.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "defectlog_gauge")
public class DefectLog_Gauge {

    @Id
    @Column(name = "gaugeTitle", length = 100, nullable = false)
    private String title;

    @Column(name = "stabilityPercent", nullable = false)
    private int stabilityPercent = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomalyLevel", length = 20, nullable = false)
    private AnomalyLevel level = AnomalyLevel.STABLE;

    @Column(name = "isBlinking", nullable = false)
    private boolean isBlinking = false;

    @Column(name = "tooltipMessage", columnDefinition = "TEXT", nullable = false)
    private String tooltipMessage;

    public DefectLog_Gauge() {}

    // Getter & Setter 생략 가능

}
