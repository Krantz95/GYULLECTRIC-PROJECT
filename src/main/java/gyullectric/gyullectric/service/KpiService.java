package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.ProcessResultStatus;
import gyullectric.gyullectric.dto.ProductionKpiDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KpiService {

    private final MonitoringRepository monitoringRepository;

    /** KPI – 당일(00:00~현재) 기준으로 속도·예측률 집계 */
    public ProductionKpiDto getTodayProductionKpi() {

        /* ── 상수 정의 ───────────────────────────── */
        final int DAILY_TARGET            = 300;      // 오늘 목표
        final int MINUTES_PER_PRODUCT_EST = 2;        // 속도 0일 때 가정
        final int DUE_DAYS                = 7;        // 주문일 + 7일 (고정)

        /* ── 오늘 범위 & 경과 시간 ───────────────── */
        LocalDateTime startDay = LocalDate.now().atStartOfDay();
        LocalDateTime now      = LocalDateTime.now();
        long elapsedMinutes    = Math.max(1, Duration.between(startDay, now).toMinutes()); // 0 분 방지

        /* 1) 오늘 완제품(PASS) 누계 */
        int passCount = Optional.ofNullable(
                        monitoringRepository.countTodayPassProduct(startDay, now))   // end 인자를 now 로 축소
                .orElse(0L).intValue();

        /* 2) 현재 평균 속도(대/분) = 누계 / 경과 분 */
        double currentSpeed = round(passCount / (double) elapsedMinutes, 2);

        /* 3) 달성률 */
        int achievementRate = (int) Math.min(100, Math.round(passCount * 100.0 / DAILY_TARGET));

        /* 4) 잔여 수량 & 예상 소요 시간 */
        int remainQty   = Math.max(0, DAILY_TARGET - passCount);
        double estMin   = currentSpeed > 0 ? remainQty / currentSpeed
                : remainQty * MINUTES_PER_PRODUCT_EST;
        String eta      = String.format("%d시간 %d분", (int) estMin / 60, (int) estMin % 60);

        /* 5) 남은 납기 시간(분) – 오늘 주문이라고 가정 */
        long remainDueMin = Duration.between(now, startDay.plusDays(DUE_DAYS)).toMinutes();

        /* 6) 납기 예측 확률 */
        double expectedProduced = currentSpeed * remainDueMin;
        int expectedRate = remainQty > 0
                ? (int) Math.min(100, Math.round(expectedProduced * 100.0 / remainQty))
                : 100;

        boolean onTime = expectedRate >= 100;

        /* 7) DTO 조립 */
        return ProductionKpiDto.builder()
                .totalOrder(DAILY_TARGET)
                .completed(passCount)
                .achievementRate(achievementRate)
                .currentSpeed(currentSpeed)
                .expectedRate(expectedRate)
                .estimatedTime(eta)
                .onTime(onTime)
                .build();
    }

    /** 소수점 반올림 */
    private double round(double v, int p) {
        double s = Math.pow(10, p);
        return Math.round(v * s) / s;
    }
}
