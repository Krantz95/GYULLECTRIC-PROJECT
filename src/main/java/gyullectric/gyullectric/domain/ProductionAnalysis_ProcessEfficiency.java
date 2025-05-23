package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "production_analysis_process_efficiency")
public class ProductionAnalysis_ProcessEfficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_step", nullable = false, length = 20)
    private int processStep;

    @Column(name = "avg_processing_time", nullable = false)
    private double avgProcessingTime = 0.0;

    @Column(name = "planned_operation_time", nullable = false)
    private double plannedOperationTime = 0.0;

    @Column(name = "actual_operation_time", nullable = false)
    private double actualOperationTime = 0.0;

    @Column(name = "power_usage", nullable = false)
    private double powerUsage = 0.0;

    @Column(name = "defect_rate", nullable = false)
    private double defectRate = 0.0;

    @Column(name = "is_over_defect_threshold", nullable = false)
    private boolean isOverDefectThreshold = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_unit", nullable = false, length = 20)
    private TimeUnit timeUnit;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    public ProductionAnalysis_ProcessEfficiency() {}

    // Getters and setters 생략
}
