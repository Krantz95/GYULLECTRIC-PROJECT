package gyullectric.gyullectric.service;

import gyullectric.gyullectric.dto.ProductionKpiDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import gyullectric.gyullectric.repository.OrderListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final OrderListRepository orderListRepository;
    private final MonitoringRepository monitoringRepository;

    public ProductionKpiDto getTodayProductionKpi() {
        final int DAILY_TARGET = 300;                     // 🎯 고정 목표 생산량
        final double MINUTES_PER_PRODUCT = 2.0;           // ⏱ 현실 시간 기준 생산 소요 (5초 = 2분)

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        // ✅ 기일 PASS 제품 수량
        Long passedProduct = monitoringRepository.countTodayPassProduct(startOfDay, endOfDay);
        int passCount = passedProduct != null ? passedProduct.intValue() : 0;

        // ✅ 기일 목표 달성률
        int achievementRate = (int) ((passCount / (double) DAILY_TARGET) * 100);

        // ✅ 현재 속도 계산 (분당 생산량)
        double runtimeMinutes = passCount * MINUTES_PER_PRODUCT;
        double currentSpeed = (runtimeMinutes > 0) ? (passCount / runtimeMinutes) : 0;

        // ✅ 내기 예측: 오늘 자정부터 7일 후 자정까지
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = startOfDay.plusDays(7);
        long remainingMinutesUntilDue = Duration.between(now, dueDate).toMinutes();

        // ✅ 남은 목표량
        int remainingQty = Math.max(0, DAILY_TARGET - passCount);

        // ✅ 남은 예상 시간
        double requiredMinutes = remainingQty * MINUTES_PER_PRODUCT;

        // ✅ 내기 가능 여부 및 예측률
        boolean onTime = requiredMinutes <= remainingMinutesUntilDue;
        int expectedRate = onTime
                ? (int) ((1 - requiredMinutes / remainingMinutesUntilDue) * 100)
                : 0;

        // ✅ 남은 예상 시간 문자열 변환
        String estimatedTime = String.format("%d시간 %d분",
                (int) requiredMinutes / 60, (int) requiredMinutes % 60);

        // ✅ DTO 구성
        ProductionKpiDto dto = new ProductionKpiDto();
        dto.setTotalOrder(DAILY_TARGET);
        dto.setCompleted(passCount);
        dto.setAchievementRate(achievementRate);
        dto.setCurrentSpeed(round(currentSpeed, 2));
        dto.setExpectedRate(expectedRate);
        dto.setEstimatedTime(estimatedTime);
        dto.setOnTime(onTime);

        return dto;
    }

    // 소수점 반월임 유틸
    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
