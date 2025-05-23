package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_analysis_insight")
public class ProductionAnalysis_Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_text", columnDefinition = "TEXT", nullable = false)
    private String summaryText;

    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    @Column(name = "total_production", nullable = false)
    private double totalProduction = 0;

    @Column(name = "production_change_rate", nullable = false)
    private double productionChangeRate = 0.0;

    @Column(name = "is_production_increased", nullable = false)
    private boolean isProductionIncreased = false;

    @Column(name = "avg_processing_time", nullable = false)
    private double avgProcessingTime = 0.0;

    @Column(name = "time_change_rate", nullable = false)
    private double timeChangeRate = 0.0;

    @Column(name = "is_time_shortened", nullable = false)
    private boolean isTimeShortened = false;

    @Column(name = "defect_rate", nullable = false)
    private double defectRate = 0.0;

    @Column(name = "defect_status_msg", columnDefinition = "TEXT", nullable = false)
    private String defectStatusMsg;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProductionAnalysis_Insight() {}

    // Getters and setters 생략
}
