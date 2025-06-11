package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.ProductName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BikeProductionDto {

    private ProductName productName;
    private int targetCount;
    private int actualCount;
}
