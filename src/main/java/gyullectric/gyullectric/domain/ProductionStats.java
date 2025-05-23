package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_stats")
public class ProductionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", nullable = false, length = 50)
    private ProductName productName;

    @Column(name = "production_count", nullable = false)
    private int productionCount = 0;

    @Column(name = "avg_processing_time", nullable = false)
    private double avgProcessingTime = 0.0;

    @Column(name = "defect_rate", nullable = false)
    private double defectRate = 0.0;

    @Column(name = "rework_rate", nullable = false)
    private double reworkRate = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_period", nullable = false, length = 20)
    private WeekPeriod weekPeriod;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProductionStats() {}

    // Getters and Setters 생략 가능 (롬복 사용 시 @Getter @Setter 사용 가능)
}
