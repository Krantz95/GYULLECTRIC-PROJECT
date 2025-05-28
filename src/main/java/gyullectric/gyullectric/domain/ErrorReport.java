package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "error_report")
public class ErrorReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "error_title", length = 100, nullable = false)
    private String errorTitle;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "process_step", length = 20, nullable = false)
    private String processStep;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", length = 20, nullable = false)
    private ProductName productName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Members members;

    @Column(name = "written_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime writtenAt;
    @ManyToOne
    @JoinColumn(name = "process_id") // 외래키 컬럼
    private ProcessLog processLog;

    // getters, setters 생략
}
