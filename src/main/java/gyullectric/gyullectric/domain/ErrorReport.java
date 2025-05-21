package gyullectric.gyullectric.domain;

import java.time.LocalDateTime;

public class ErrorReport {
    private Long id;
    private String errorTitle;
    private LocalDateTime occurredAt;
    private int processStep;
    private Priority priority;
    private ProductName productName;
    private String content;
    private Members error_author;
    private LocalDateTime writtenAt;
}
