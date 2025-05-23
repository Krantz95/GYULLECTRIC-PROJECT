package gyullectric.gyullectric.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class DefectLogDetectionDataId implements Serializable {

    private int processStep;
    private LocalDateTime timestamp;

    public DefectLogDetectionDataId() {}

    public DefectLogDetectionDataId(int processStep, LocalDateTime timestamp) {
        this.processStep = processStep;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefectLogDetectionDataId)) return false;
        DefectLogDetectionDataId that = (DefectLogDetectionDataId) o;
        return processStep == that.processStep && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processStep, timestamp);
    }
}
