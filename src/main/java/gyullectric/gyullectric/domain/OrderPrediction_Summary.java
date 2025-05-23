package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "order_prediction_summary")
public class OrderPrediction_Summary {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", length = 50, nullable = false)
    private ProductName productName;

    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    @Column(name = "predicted_total_order_amount", nullable = false)
    private int predictedTotalOrderAmount = 0;

    @Column(name = "optimal_order_date", nullable = false)
    private LocalDate optimalOrderDate;

    @Column(name = "overstock_risk", nullable = false)
    private boolean overstockRisk = false;

    @ElementCollection
    @CollectionTable(name = "increasing_demand_items", joinColumns = @JoinColumn(name = "product_name"))
    @Column(name = "part_name")
    @Enumerated(EnumType.STRING)
    private List<PartName> increasingDemandItems;

    public OrderPrediction_Summary() {}

    // Getters and setters 생략
}
