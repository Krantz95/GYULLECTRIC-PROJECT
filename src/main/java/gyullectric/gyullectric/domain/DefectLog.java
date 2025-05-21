package gyullectric.gyullectric.domain;

import java.time.LocalDateTime;

public class DefectLog {
    private Long id;
    private LocalDateTime occuredAt;
    private int processStep;
    private String detectedSymptom;
}
