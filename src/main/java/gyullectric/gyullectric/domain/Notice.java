package gyullectric.gyullectric.domain;

import java.time.LocalDateTime;

public class Notice {
    private Long id;
    private String title;
    private String content;
    private Boolean isNotice;
    private LocalDateTime createdAt;
    private Members noti_author;
    private Integer viewCount;

}
