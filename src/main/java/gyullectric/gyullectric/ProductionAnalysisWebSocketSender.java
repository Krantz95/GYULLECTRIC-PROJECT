package gyullectric.gyullectric;

import gyullectric.gyullectric.service.ProductionAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionAnalysisWebSocketSender {

    private final SimpMessagingTemplate messagingTemplate;
    private final ProductionAnalysisService analysisService;

    @Scheduled(fixedRate = 5000)
    public void sendProductionAnalysis() {
        Map<Integer, Double> avgProcessingTime = analysisService.getChartAvgProcessingTime();
        Map<Integer, Long> errorCounts = analysisService.getErrorCountByProcess();
        Map<Integer, Map<String, Double>> powerDefect = analysisService.getPowerVsDefectData();

        // 🔹 KPI 항목 계산 (예시값 — 실제 서비스에서 KPI 계산 메서드 활용)
        double currentSpeed = analysisService.getCurrentSpeed(); // 대/분
        double expectedRate = analysisService.getExpectedCompletionRate(); // %
        String estimatedTime = analysisService.getEstimatedTimeString(); // "3시간 24분"
        int completed = analysisService.getTodayCompletedCount();
        double achievementRate = (completed / 300.0) * 100;

        Map<String, Object> result = Map.of(
                "avgProcessingTime", avgProcessingTime,
                "errorCounts", errorCounts,
                "powerDefect", powerDefect,
                "kpi", Map.of(
                        "completed", completed,
                        "achievementRate", Math.round(achievementRate * 10.0) / 10.0,
                        "currentSpeed", currentSpeed,
                        "expectedRate", expectedRate,
                        "estimatedTime", estimatedTime
                )
        );

        messagingTemplate.convertAndSend("/topic/analysis", result);
    }

}
