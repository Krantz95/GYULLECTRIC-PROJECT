package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "error_guide")
public class ErrorGuide {

    @Id
    @Column(name = "error_code", length = 20)
    @Enumerated(EnumType.STRING)
    private ErrorCode errorCode; // PK

    @Column(name = "guide_title", length = 100, nullable = false)
    private String guideTitle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "noti_author_id", nullable = false)
    private Members noti_author;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "importance", nullable = false)
    private Boolean importance = false;

    @Column(name = "solution", columnDefinition = "TEXT", nullable = false)
    private String solution;

    @Column(name = "procedure", columnDefinition = "TEXT", nullable = false)
    private String procedure;

    @Column(name = "caution", columnDefinition = "TEXT", nullable = true)
    private String caution;

    public ErrorGuide() {}

    // getter, setter 등 생략 가능
}
