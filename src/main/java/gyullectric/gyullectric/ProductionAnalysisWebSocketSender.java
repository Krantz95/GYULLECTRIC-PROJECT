package gyullectric.gyullectric;

import gyullectric.gyullectric.dto.ProductionKpiDto;
import gyullectric.gyullectric.service.KpiService;
import gyullectric.gyullectric.service.ProductionAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionAnalysisWebSocketSender {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProductionAnalysisService analysisService;

    /* ⬇️ 새로 주입 */
    private final KpiService kpiService;

    @Scheduled(fixedRate = 5000)
    public void sendProductionAnalysis() {

        /* 1) 그래프·병목 관련 데이터 */
        Map<Integer, Double> avgProcessingTime = analysisService.getChartAvgProcessingTime();
        Map<Integer, Long>   errorCounts       = analysisService.getErrorCountByProcess();
        Map<Integer, Map<String, Double>> powerDefect = analysisService.getPowerVsDefectData();

        /* 2) KPI -- 단일 진입점으로 통일 */
        ProductionKpiDto kpi = kpiService.getTodayProductionKpi();

        /* 3) 페이로드 조립 */
        Map<String, Object> result = new HashMap<>();
        result.put("avgProcessingTime", avgProcessingTime);
        result.put("errorCounts",       errorCounts);
        result.put("powerDefect",       powerDefect);
        result.put("kpi",               kpi);   // ★ 그대로 직렬화

        /* 4) WebSocket 전송 */
        messagingTemplate.convertAndSend("/topic/analysis", result);
        log.info("[WS] analysis payload pushed: {}", kpi);
    }
}
