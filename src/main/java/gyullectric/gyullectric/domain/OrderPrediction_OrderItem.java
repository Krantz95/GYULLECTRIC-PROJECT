package gyullectric.gyullectric.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_prediction_order_item")
public class OrderPrediction_OrderItem {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "part_name", length = 50, nullable = false)
    private PartName partName;

    @Column(name = "current_stock", nullable = false)
    private int currentInventory = 0;

    @Column(name = "predicted_demand", nullable = false)
    private int predictedDemand = 0;

    @Column(name = "recommended_order", nullable = false)
    private int recommendedOrderAmount = 0;

    public OrderPrediction_OrderItem() {}

    // Getters and setters 생략
}
