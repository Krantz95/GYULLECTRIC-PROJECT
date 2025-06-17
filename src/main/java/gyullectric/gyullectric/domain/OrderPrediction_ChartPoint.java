package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "order_prediction_chart_point")
public class OrderPrediction_ChartPoint {

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "actual_order_amount", nullable = false)
    private int actualOrderAmount = 0;

    @Column(name = "predicted_order_amount", nullable = false)
    private int predictedOrderAmount = 0;

    @Column(name = "tren_line_value", nullable = false)
    private float trenLineValue = 0.0f;

    public OrderPrediction_ChartPoint() {}

    // Getters and setters 생략
}
