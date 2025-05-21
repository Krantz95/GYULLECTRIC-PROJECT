package gyullectric.gyullectric.domain;

import java.time.LocalDateTime;

public class ProcessLog {
    private Long id;
    private ProductName productName;
    private String lotNumber;
    private int processStep;
    private ProcessResultStatus processResultStatus;
    private LocalDateTime createAt;
    private String errorCode;

}
