package gyullectric.gyullectric.service;

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

    private final MonitoringDataService monitoringDataService;   // ✅ LOT 기준 완제품 계산용
    private final MonitoringRepository monitoringRepository;     // (첫 공정 시각 조회용)

    public ProductionKpiDto getTodayProductionKpi() {

        /* ───────── 상수 정의 ───────── */
        final int DAILY_TARGET          = 300;  // 금일 목표 수량
        final int MINUTES_PER_PRODUCT_EST = 2;  // 속도 0일 때 가정값
        final int DUE_DAYS              = 7;    // 납기(오늘 + 7일)

        LocalDateTime now = LocalDateTime.now();

        /* 1️⃣ 오늘 첫 공정 시작 시각 */
        LocalDateTime firstLogTime = monitoringRepository.findFirstLogTimeOfToday()
                .orElse(LocalDate.now().atStartOfDay());

        /* 2️⃣ 현실·가상 시간 보정 (5초 ↔ 100초, 20배) */
        long rawElapsedMin      = Math.max(1, Duration.between(firstLogTime, now).toMinutes());
        long adjustedElapsedMin = rawElapsedMin * 20;

        /* 3️⃣ 완제품(1·2·3공정 모두 OK) 생산량 */
        long passCount = monitoringDataService.countTodayCompleteLots();

        log.info("📌 [KPI] 첫 공정 시작: {}", firstLogTime);
        log.info("📌 [KPI] 경과 시간(분): 실제={}, 보정={}", rawElapsedMin, adjustedElapsedMin);
        log.info("📌 [KPI] 완제품 LOT 수량: {}", passCount);

        /* 4️⃣ KPI 계산 */
        double currentSpeed = round(passCount / (double) adjustedElapsedMin, 2);  // 🚲 분당 속도
        int achievementRate = (int) Math.min(100, Math.round(passCount * 100.0 / DAILY_TARGET));

        int remainQty = Math.max(0, DAILY_TARGET - (int) passCount);
        double estMin = currentSpeed > 0 ? remainQty / currentSpeed
                : remainQty * MINUTES_PER_PRODUCT_EST;
        String eta = "%d시간 %d분".formatted((int) estMin / 60, (int) estMin % 60);

        long remainDueMin = Duration.between(
                now,
                LocalDate.now().plusDays(DUE_DAYS).atStartOfDay()
        ).toMinutes();

        double expectedProduced = currentSpeed * remainDueMin;
        int expectedRate = remainQty > 0
                ? (int) Math.min(100, Math.round(expectedProduced * 100.0 / remainQty))
                : 100;

        boolean onTime = expectedRate >= 100;

        /* 5️⃣ DTO 반환 */
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

    /* 🔧 소수점 반올림 헬퍼 */
    private double round(double v, int p) {
        double s = Math.pow(10, p);
        return Math.round(v * s) / s;
    }
}
