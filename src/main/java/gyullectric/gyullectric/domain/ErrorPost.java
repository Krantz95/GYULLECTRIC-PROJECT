package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "error_post")
public class ErrorPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_title", length = 100, nullable = false)
    private String postTitle;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", length = 20, nullable = false)
    private ProductName productName;

    @Column(name = "process_step", length = 20, nullable = false)
    private String processStep;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "error_author_id", nullable = false)
    private Members error_author;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, columnDefinition = "varchar(20) default 'UNRESOLVED'")
    private ErrorStatus resolved;

    public ErrorPost() {}

    // getter, setter 생략 가능
}
