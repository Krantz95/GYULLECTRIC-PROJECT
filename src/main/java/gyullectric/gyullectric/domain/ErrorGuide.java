package gyullectric.gyullectric.domain;

import java.time.LocalDateTime;

public class ErrorGuide {
    private ErrorCode errorCode;
    private String guideTitle;
    private Members noti_author;
    private LocalDateTime createdAt;
    private int viewCount;
    private Boolean importance;
    private String solution;
    private String procedure;
    private String caution;
}
