package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "error_comment")
public class ErrorComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "error_report_id", nullable = false)
    private ErrorReport errorReport;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "noti_author_id", nullable = false)
    private Members noti_author;

    @Column(name = "written_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime writtenAt;

    @Column(name = "modify_date", nullable = true)
    private LocalDateTime modifyDate;

    public ErrorComment() {}

    // Getter, Setter 생략 가능
}
