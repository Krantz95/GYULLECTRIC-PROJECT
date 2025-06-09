package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ErrorAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;
    @Column(columnDefinition = "TEXT")
    private  String content;

    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "error_id")
    private ErrorReport errorReport;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Members members;

    private  LocalDateTime modifyDate;
}
