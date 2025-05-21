package gyullectric.gyullectric.domain;

import ch.qos.logback.core.status.ErrorStatus;

import java.time.LocalDateTime;

public class ErrorPost {
    private Long id;
    private String postTitle;
    private int commentCount;
    private ProductName productName;
    private int processStep;
    private LocalDateTime createAt;
    private Members error_author;
    private ErrorStatus resolved;
}
