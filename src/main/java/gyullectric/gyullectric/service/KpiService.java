package gyullectric.gyullectric.service;

import gyullectric.gyullectric.dto.ProcessDataDto;
import gyullectric.gyullectric.dto.ProductionKpiDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KpiService {

    private final MonitoringRepository monitoringRepository;
    private final MonitoringDataService monitoringDataService;

    public ProductionKpiDto getTodayProductionKpi() {
        final int DAILY_TARGET = 300;
        final int MINUTES_PER_PRODUCT_EST = 2;
        final int DUE_DAYS = 7;

        LocalDateTime now = LocalDateTime.now();

        // ⏱ 오늘 첫 공정 로그 시간
        LocalDateTime firstLogTime = monitoringRepository.findFirstLogTimeOfToday()
                .orElse(LocalDate.now().atStartOfDay());

        // 💡 실제 웹소켓 5초 ≈ 현실 100초 환산 → 20배 시간 보정
        long rawElapsedMinutes = Math.max(1, Duration.between(firstLogTime, now).toMinutes());
        long adjustedElapsedMinutes = rawElapsedMinutes * 20;

        // ✅ 완성품(PASS + 3공정) 기준 생산량
        long passCount = monitoringRepository.countTodayPassProduct(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        );

        log.info("📌 [KPI] 첫 공정 시작 시각: {}", firstLogTime);
        log.info("📌 [KPI] 실제 경과 시간(분): {}", rawElapsedMinutes);
        log.info("📌 [KPI] 보정 경과 시간(분): {}", adjustedElapsedMinutes);
        log.info("📌 [KPI] PASS된 자전거 수량: {}", passCount);

        // 🚲 현재 속도 = 분당 생산량 (PASS 기준)
        double currentSpeed = round(passCount / (double) adjustedElapsedMinutes, 2);

        int achievementRate = (int) Math.min(100, Math.round(passCount * 100.0 / DAILY_TARGET));
        int remainQty = Math.max(0, DAILY_TARGET - (int) passCount);

        double estMin = currentSpeed > 0
                ? remainQty / currentSpeed
                : remainQty * MINUTES_PER_PRODUCT_EST;
        String eta = String.format("%d시간 %d분", (int) estMin / 60, (int) estMin % 60);

        long remainDueMin = Duration.between(now, LocalDate.now().plusDays(DUE_DAYS).atStartOfDay()).toMinutes();
        double expectedProduced = currentSpeed * remainDueMin;
        int expectedRate = remainQty > 0
                ? (int) Math.min(100, Math.round(expectedProduced * 100.0 / remainQty))
                : 100;

        boolean onTime = expectedRate >= 100;

        return ProductionKpiDto.builder()
                .totalOrder(DAILY_TARGET)
                .completed((int) passCount)
                .achievementRate(achievementRate)
                .currentSpeed(currentSpeed)
                .expectedRate(expectedRate)
                .estimatedTime(eta)
                .onTime(onTime)
                .build();
    }

    private double round(double v, int p) {
        double s = Math.pow(10, p);
        return Math.round(v * s) / s;
    }
}
