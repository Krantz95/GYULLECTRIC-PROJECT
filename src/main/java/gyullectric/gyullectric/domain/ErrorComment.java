package gyullectric.gyullectric.domain;

import java.time.LocalDateTime;

public class ErrorComment {
    private Long id;
    private ErrorReport errorReport;
    private String content;
    private Members noti_author;
    private LocalDateTime writtenAt;
    private LocalDateTime modifyDate;

}
