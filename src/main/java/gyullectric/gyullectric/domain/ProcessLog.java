package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "process_log")
public class ProcessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="process_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", length = 50)
    private ProductName productName;

    @Column(name = "lot_number", length = 50, nullable = false)
    private String lotNumber;

    @Column(name = "process_step", length = 20, nullable = false)
    private int processStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_result_status", length = 20, nullable = false)
    private ProcessResultStatus processResultStatus;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    // ✅ 병목 분석용: 공정 시작/종료 시각 추가
    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "error_code", length = 20)
    private String errorCode;

    private Double errorValue;

    private String errorDescription;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private OrderList orderList;

    @Builder.Default
    @OneToMany(mappedBy = "processLog", cascade = CascadeType.ALL)
    private List<ErrorReport> errorReportList = new ArrayList<>();

    public String getErrorDescription() {
        return errorDescription;
    }
}
