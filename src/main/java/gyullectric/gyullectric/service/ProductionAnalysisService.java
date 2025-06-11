package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProcessResultStatus;
import gyullectric.gyullectric.repository.MonitoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

        Map<Integer, Double> avgTimeSec = new HashMap<>();
        for (Object[] row : monitoringRepository.findAvgProcessTimeByStepTodayNative(start, end)) {
            avgTimeSec.put((Integer) row[0], ((Number) row[1]).doubleValue());
        }

        Map<Integer, Long> totalCountMap = new HashMap<>();
        for (Object[] row : monitoringRepository.countTotalLogsByProcessStepToday(start, end)) {
            totalCountMap.put((Integer) row[0], ((Number) row[1]).longValue());
        }

        Map<Integer, Long> ngCountMap = new HashMap<>();
        for (Object[] row : monitoringRepository.countErrorsByProcessStepToday(start, end)) {
            ngCountMap.put((Integer) row[0], ((Number) row[1]).longValue());
        }

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

    public int getTodayCompletedCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        return monitoringRepository.countTodayPassProduct(start, end).intValue();
    }

    public double getCurrentSpeed() {
        List<ProcessLog> recent = monitoringRepository.findLastHour();
        long passCount = recent.stream()
                .filter(p -> p.getProcessStep() == 3 && p.getProcessResultStatus() == ProcessResultStatus.OK)
                .count();
        return Math.round((passCount / 60.0) * 10.0) / 10.0;
    }

    public double getExpectedCompletionRate() {
        int completed = getTodayCompletedCount();
        int target = 300;
        return Math.round((completed * 100.0 / target) * 10.0) / 10.0;
    }

    public String getEstimatedTimeString() {
        double speed = getCurrentSpeed();
        int remaining = 300 - getTodayCompletedCount();
        if (speed == 0) return "속도 정보 부족";

        double totalMinutes = remaining / speed;
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) (totalMinutes % 60);

        return hours + "시간 " + minutes + "분";
    }
}
