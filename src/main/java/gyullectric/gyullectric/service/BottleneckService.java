package gyullectric.gyullectric.service;

import gyullectric.gyullectric.dto.BottleneckDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BottleneckService {

    private final MonitoringRepository monitoringRepository;

    public BottleneckDto analyzeTodayBottleneck() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        // 1. 공정별 평균 처리시간 조회
        List<Object[]> timeResults = monitoringRepository.findAvgProcessTimeByStepTodayNative(startOfDay, endOfDay);
        Map<Integer, Double> processTimeMap = new HashMap<>();
        for (Object[] row : timeResults) {
            Integer step = (Integer) row[0];
            Double avgSeconds = ((Number) row[1]).doubleValue();
            processTimeMap.put(step, avgSeconds);
        }

        // 2. 공정별 에러 수 조회
        List<Object[]> errorResults = monitoringRepository.countErrorsByProcessStepToday(startOfDay, endOfDay);
        Map<Integer, Long> errorMap = new HashMap<>();
        for (Object[] row : errorResults) {
            Integer step = (Integer) row[0];
            Long errorCount = ((Number) row[1]).longValue();
            errorMap.put(step, errorCount);
        }

        // 3. 병목 공정 결정
        int bottleneck = processTimeMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);

        // 4. 공정명 및 원인 설명 매핑
        String processName = switch (bottleneck) {
            case 1 -> "프레임공정";
            case 2 -> "도장공정";
            case 3 -> "조립공정";
            default -> "분석 불가";
        };

        String reason = switch (bottleneck) {
            case 1 -> "용접 지연 / 온도 편차 발생";
            case 2 -> "도장온도 평균 3.5℃ 초과";
            case 3 -> "검수 공정 기준 강화로 인한 지연";
            default -> "분석 불가";
        };

        // 5. 결과 DTO 생성
        return new BottleneckDto(processTimeMap, errorMap, bottleneck, processName, reason);
    }
}
