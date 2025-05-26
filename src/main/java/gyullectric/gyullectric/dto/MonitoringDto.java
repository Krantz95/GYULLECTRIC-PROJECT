package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.ProcessResultStatus;
import gyullectric.gyullectric.domain.ProductName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonitoringDto {
    private String lotNumber;
    private int processStep;
    private ProcessResultStatus processResultStatus;
    private String creatAt;
    private String errorCode;
    private ProductName productName;

}
