package gyullectric.gyullectric.service;

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
public class ProductionAnalysisService {

    private final MonitoringRepository monitoringRepository;

    public Map<Integer, Double> getChartAvgProcessingTime() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        List<Object[]> results = monitoringRepository.findAvgProcessTimeByStepTodayNative(startOfDay, endOfDay);
        Map<Integer, Double> timeMap = new HashMap<>();

        for (Object[] row : results) {
            Integer step = (Integer) row[0];
            Double avgSeconds = ((Number) row[1]).doubleValue();
            timeMap.put(step, avgSeconds); // 초 단위 그대로 저장
        }

        return timeMap;
    }

    public Map<Integer, Long> getErrorCountByProcess() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        List<Object[]> results = monitoringRepository.countErrorsByProcessStepToday(startOfDay, endOfDay);
        Map<Integer, Long> errorCountMap = new HashMap<>();
        for (Object[] row : results) {
            Integer step = (Integer) row[0];
            Long count = ((Number) row[1]).longValue();
            errorCountMap.put(step, count);
        }
        return errorCountMap;
    }

    public Map<Integer, Map<String, Double>> getPowerVsDefectData() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);

        // 처리시간 기반 전력량
        Map<Integer, Double> avgTimeSec = new HashMap<>();
        for (Object[] row : monitoringRepository.findAvgProcessTimeByStepTodayNative(start, end)) {
            avgTimeSec.put((Integer) row[0], ((Number) row[1]).doubleValue());
        }

        // 에러율 계산용: 전체 로그
        Map<Integer, Long> totalCountMap = new HashMap<>();
        for (Object[] row : monitoringRepository.countTotalLogsByProcessStepToday(start, end)) {
            totalCountMap.put((Integer) row[0], ((Number) row[1]).longValue());
        }

        // NG 로그
        Map<Integer, Long> ngCountMap = new HashMap<>();
        for (Object[] row : monitoringRepository.countErrorsByProcessStepToday(start, end)) {
            ngCountMap.put((Integer) row[0], ((Number) row[1]).longValue());
        }

        // 결합
        Map<Integer, Map<String, Double>> result = new HashMap<>();
        for (int step = 1; step <= 3; step++) {
            double power = avgTimeSec.getOrDefault(step, 0.0) / 3600 * 2.5;
            long total = totalCountMap.getOrDefault(step, 0L);
            long ng = ngCountMap.getOrDefault(step, 0L);
            double defectRate = (total > 0) ? (ng * 100.0 / total) : 0.0;

            result.put(step, Map.of(
                    "power", Math.round(power * 100.0) / 100.0,
                    "defect", Math.round(defectRate * 10.0) / 10.0
            ));
        }
        return result;
    }
}