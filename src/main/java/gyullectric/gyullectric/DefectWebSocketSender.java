package gyullectric.gyullectric;

import gyullectric.gyullectric.dto.DefectLogDto;
import gyullectric.gyullectric.service.DefectLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

// DefectWebSocketSender
// - 더미 센서 데이터 → 점수 예측 → WebSocket으로 실시간 전송
// - 5초마다 자동 실행
// - 클라이언트는 /topic/defect 경로를 통해 수신
@Slf4j
@Component
@RequiredArgsConstructor
public class DefectWebSocketSender {

    private final SimpMessagingTemplate messagingTemplate;
    private final Dummy_DefectLog dummyDefectLog;
    private final DefectLogService defectLogService;

    // 5초마다 자동 실행되는 스케줄러
    @Scheduled(fixedRate = 5000)
    public void sendDefectPrediction() {
        // 1. 더미 센서 데이터 생성
        DefectLogDto dto = dummyDefectLog.getDummySensorData2();

        // 2. Flask 예측 호출 및 결과 수신 (점수 + 경고 메시지 포함)
        Map<String, Object> result = defectLogService.getDefectResultFromFlask(dto);

        // 3. WebSocket으로 클라이언트에 데이터 전송
        messagingTemplate.convertAndSend("/topic/defect", result);

        // 4. 로그 출력
        log.info("[WebSocket] 예측 결과 전송 완료: {}", result);
    }

    // 차트1 : API받은 값에 가중치 계산
    private int calculateCastingScore(Map<String, Object> result) {
        double pressure = toDouble(result.get("pressure"));
        double upperTemp = toDouble(result.get("upperTemp"));
        double lowerTemp = toDouble(result.get("lowerTemp"));

        int score = 0;
        if (pressure >= 299) score += 40;
        if (upperTemp >= 94) score += 30;
        if (lowerTemp >= 193) score += 30;

        return score;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return 0.0;
    }
}
