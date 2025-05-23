package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "process_log")
public class ProcessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", length = 50, nullable = false)
    private ProductName productName;

    @Column(name = "lot_number", length = 50, nullable = false)
    private String lotNumber;

    @Column(name = "process_step", length = 20, nullable = false)
    private int processStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_result_status", length = 20, nullable = false)
    private ProcessResultStatus processResultStatus = ProcessResultStatus.OK;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    @Column(name = "error_code", length = 20)
    private String errorCode;

    public ProcessLog() {}

    // Getters and setters 생략
}
