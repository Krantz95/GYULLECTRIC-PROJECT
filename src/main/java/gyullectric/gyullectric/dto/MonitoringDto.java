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
    private String processResultStatus;
    private String createAt;
    private String errorCode;
    private String productName;



}
