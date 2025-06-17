package gyullectric.gyullectric.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_prediction")
public class OrderPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_name", length = 50, nullable = false)
    private PartName partName;

    @Column(name = "current_stock", nullable = false)
    private int currentStock = 0;

    @Column(name = "predicted_demand", nullable = false)
    private int predictedDemand = 0;

    @Column(name = "recommended_order", nullable = false)
    private int recommendedOrder = 0;

    public OrderPrediction() {}

    // Getters and setters 생략
}
