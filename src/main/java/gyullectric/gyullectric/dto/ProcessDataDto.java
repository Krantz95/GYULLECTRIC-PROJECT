package gyullectric.gyullectric.dto;

import gyullectric.gyullectric.domain.ProcessLog;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ProcessDataDto {
    private List<ProcessLog> processes;

    Map<Integer, Long> countBystep;

    Map<Integer, Long> ngCountByStep;

    Map<Integer, Long> okCountByStep;

    private long productCount;

    private int totalCount;


}
