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

        // ✅ 1. 공정별 평균 처리시간 조회 (초 단위)
        List<Object[]> timeResults = monitoringRepository.findAvgProcessTimeByStepTodayNative(startOfDay, endOfDay);
        Map<Integer, Double> processTimeMap = new HashMap<>();
        for (Object[] row : timeResults) {
            Integer step = (Integer) row[0];
            Double avgSeconds = ((Number) row[1]).doubleValue();
            processTimeMap.put(step, avgSeconds);
        }

        // ✅ 2. 공정별 에러 수 조회
        List<Object[]> errorResults = monitoringRepository.countErrorsByProcessStepToday(startOfDay, endOfDay);
        Map<Integer, Long> errorMap = new HashMap<>();
        for (Object[] row : errorResults) {
            Integer step = (Integer) row[0];
            Long errorCount = ((Number) row[1]).longValue();
            errorMap.put(step, errorCount);
        }

        // ✅ 3. 가장 오래 걸린 공정을 병목으로 간주
        int bottleneck = processTimeMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);

        // ✅ 4. 병목 공정에 따른 메시지
        String reason = switch (bottleneck) {
            case 1 -> "프레임 공정의 용접 지연 발생 (온도 조절 필요)";
            case 2 -> "도장 공정의 온도가 평균보다 높음";
            case 3 -> "검수 공정에서 불량 기준 강화됨";
            default -> "분석 불가";
        };

        // ✅ 5. 결과 DTO 생성
        return new BottleneckDto(processTimeMap, errorMap, bottleneck, reason);
    }
}
