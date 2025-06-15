package gyullectric.gyullectric;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.MonitoringDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.MonitoringService;
import gyullectric.gyullectric.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessScheduler {

    private final MonitoringRepository monitoringRepository;
    private final MonitoringDataService monitoringDataService;
    private final MonitoringService monitoringService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ProductService productService;

    private final Random random = new Random();

    // 가상의 현재 시간
    private LocalDateTime fakeTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    private long lastFakeTimeUpdate = System.currentTimeMillis();

    /** 🔁 공정 처리 시뮬레이션 (5초 주기) */
    @Scheduled(fixedRate = 5000)
    public void processOneByOne() {
        log.info("공정 모니터링 스케쥴러 호출");

        Optional<ProcessLog> processOpt = monitoringRepository
                .findFirstByProcessResultStatusOrderByOrderList_IdAsc(ProcessResultStatus.WAITING);

        if (processOpt.isEmpty()) return;

        ProcessLog step1 = processOpt.get();
        int baseStep = step1.getProcessStep();

        /** ✅ Step 1: 프레임 용접 */
        double frameOutput = 1200 + (600 * random.nextDouble()); // 1200~1800 범위
        boolean isOk1 = frameOutput >= 1700;
        String errorCode1 = "_", errorMessage1 = "OK";

        if (!isOk1 && random.nextDouble() < 0.5) {
            errorCode1 = (frameOutput < 1300) ? "ERROR_102" : "ERROR_101";
            errorMessage1 = getErrorDescription(errorCode1, frameOutput);
        } else {
            isOk1 = true;
        }
        log.info("현재공정단계: {}, 에러메시지: {}, 공정상태: {}",baseStep, errorMessage1, isOk1  );
        LocalDateTime start1 = LocalDateTime.now();
        LocalDateTime end1 = start1.plusSeconds(30 + random.nextInt(91));

        step1.setProcessResultStatus(isOk1 ? ProcessResultStatus.OK : ProcessResultStatus.NG);
        step1.setCreateAt(end1);
        step1.setStartAt(start1);
        step1.setEndAt(end1);
        step1.setErrorCode(errorCode1);
        step1.setErrorValue(!isOk1 ? frameOutput : null);
        monitoringRepository.save(step1);
        sendProcessLog(step1, errorMessage1);

        /** ✅ Step 2: 도장 공정 */
        int step2Num = baseStep + 1;
        String lotStep2 = step1.getLotNumber().replace(
                "_" + String.format("%02d", baseStep) + "_",
                "_" + String.format("%02d", step2Num) + "_"
        );

        double castPressure = 280 + random.nextDouble() * 30;
        double upperMoldTemp = 85 + random.nextDouble() * 20;
        double lowerMoldTemp = 180 + random.nextDouble() * 20;

        boolean isOk2 = castPressure < 299 && upperMoldTemp < 94 && lowerMoldTemp < 193 &&
                castPressure >= 280 && upperMoldTemp >= 85 && lowerMoldTemp >= 180;

        String errorCode2 = "_", errorMessage2 = "OK";
        if (!isOk2 && random.nextDouble() < 0.2) {
            if (castPressure >= 299 || upperMoldTemp >= 94 || lowerMoldTemp >= 193) {
                errorCode2 = "ERROR_201";
                errorMessage2 = getErrorDescription(errorCode2, Math.max(castPressure,
                        Math.max(upperMoldTemp, lowerMoldTemp)));
            } else {
                errorCode2 = "ERROR_202";
                errorMessage2 = getErrorDescription(errorCode2, Math.min(castPressure,
                        Math.min(upperMoldTemp, lowerMoldTemp)));
            }
        } else {
            isOk2 = true;
        }
        log.info("현재공정단계: {},에러메시지 {}, 공정상태: {}",step2Num, errorMessage2,  isOk2  );
        LocalDateTime start2 = LocalDateTime.now();
        LocalDateTime end2 = start2.plusSeconds(30 + random.nextInt(91));

        ProcessLog step2 = ProcessLog.builder()
                .lotNumber(lotStep2)
                .processStep(step2Num)
                .orderList(step1.getOrderList())
                .productName(step1.getProductName())
                .processResultStatus(isOk2 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                .createAt(end2)
                .startAt(start2)
                .endAt(end2)
                .errorCode(errorCode2)
                .errorValue(!isOk2 ? (errorCode2.equals("ERROR_201") ?
                        Math.max(castPressure, Math.max(upperMoldTemp, lowerMoldTemp)) :
                        Math.min(castPressure, Math.min(upperMoldTemp, lowerMoldTemp))) : null)
                .build();
        monitoringRepository.save(step2);
        sendProcessLog(step2, errorMessage2);

        /** ✅ Step 3: 검수 공정 */
        int step3Num = baseStep + 2;
        String lotStep3 = step2.getLotNumber().replace(
                "_" + String.format("%02d", step2Num) + "_",
                "_" + String.format("%02d", step3Num) + "_"
        );

        boolean isOk3 = random.nextDouble() >= 0.2;
        String errorCode3 = "_", errorMessage3 = "OK";
        if (!isOk3) {
            errorCode3 = random.nextDouble() < 0.4 ? "EXCEPTION_ERROR" :
                    new String[]{"ERROR_103", "ERROR_110", "ERROR_111"}[random.nextInt(3)];
            errorMessage3 = getErrorDescription(errorCode3, null);
        }
        log.info("현재공정단계: {},에러메시지: {}, 공정상태: {}",step3Num, errorMessage3,  isOk3  );

        LocalDateTime start3 = LocalDateTime.now();
        LocalDateTime end3 = start3.plusSeconds(30 + random.nextInt(91));

        ProcessLog step3 = ProcessLog.builder()
                .lotNumber(lotStep3)
                .processStep(step3Num)
                .orderList(step1.getOrderList())
                .productName(step1.getProductName())
                .processResultStatus(isOk3 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                .createAt(end3)
                .startAt(start3)
                .endAt(end3)
                .errorCode(errorCode3)
                .errorValue(null)
                .build();
        monitoringRepository.save(step3);
        sendProcessLog(step3, errorMessage3);

        // ✅ 주문 완료 상태 변경
        Long orderId = step1.getOrderList().getId();
        int totalQty = step1.getOrderList().getQuantity();
        long finishedCount = monitoringRepository.countByOrderList_IdAndProcessStep(orderId, 3);

        if (finishedCount >= totalQty) {
            productService.updateOrderState(orderId, ProcessStatus.COMPLETED);
        }
    }

    /** 📤 WebSocket 데이터 전송 */
    private void sendProcessLog(ProcessLog process, String errorMessage) {
        MonitoringDto dto = new MonitoringDto(
                process.getLotNumber(),
                process.getProcessStep(),
                process.getProcessResultStatus().name(),
                process.getCreateAt() != null ? process.getCreateAt().toString() : null,
                process.getErrorCode(),
                process.getProductName().name(),
                errorMessage,
                process.getErrorValue()
        );
        simpMessagingTemplate.convertAndSend("/topic/process", dto);
        log.info("WebSocket 전송: {} - {}", process.getLotNumber(), errorMessage);
    }

    /** 🧠 에러 메시지 매핑 */
    public String getErrorDescription(String errorCode, Double value) {
        return switch (errorCode) {
            case "ERROR_101" -> value != null ? String.format("용접 출력 과다 (%.1fV)", value) : "용접 출력 과다";
            case "ERROR_102" -> value != null ? String.format("용접 출력 부족 (%.1fV)", value) : "용접 출력 부족";
            case "ERROR_201" -> value != null ? String.format("온도 이상 (%.1f℃)", value) : "온도 이상";
            case "ERROR_202" -> value != null ? String.format("온도 이하 (%.1f℃)", value) : "온도 이하";
            case "ERROR_103" -> "스크래치 불량";
            case "ERROR_110" -> "조립 불량";
            case "ERROR_111" -> "기능 불량";
            default -> "EXCEPTION_ERROR";
        };
    }

    /** 📊 제품 달성률 WebSocket 송신 (가상 시간 포함, 10초 주기) */
    @Scheduled(fixedRate = 10000)
    public void sendUpdatedDashboardData() {
        long now = System.currentTimeMillis();
        if (now - lastFakeTimeUpdate >= 10000) {
            fakeTime = fakeTime.plusHours(1);
            lastFakeTimeUpdate = now;
            if (fakeTime.getHour() >= 24) {
                fakeTime = fakeTime.withHour(0).plusDays(1);
            }
        }

        try {
            List<OrderList> productOrderList = productService.allFindOrderList();
            List<ProcessLog> processLogs = monitoringService.allFindProcesses();
            Map<String, Object> chartData = monitoringDataService.calculateProductAchievementAndCounts(productOrderList, processLogs);

            Map<ProductName, Integer> todayTargetMap = productService.getTodayTargetMap();
            Map<String, Integer> targetCountsMap = new HashMap<>();
            for (ProductName name : ProductName.values()) {
                targetCountsMap.put(name.name(), todayTargetMap.getOrDefault(name, 0));
            }
            chartData.put("targetCounts", targetCountsMap);

            chartData.put("fakeHour", fakeTime.getHour());
            simpMessagingTemplate.convertAndSend("/topic/product-achievement", chartData);
            log.info("Dashboard data pushed (fakeHour={}): {}", fakeTime.getHour(), chartData);
        } catch (Exception e) {
            log.error("Dashboard 데이터 전송 실패", e);
        }
    }
}
