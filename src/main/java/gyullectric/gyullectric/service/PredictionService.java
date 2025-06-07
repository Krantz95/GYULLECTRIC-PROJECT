package gyullectric.gyullectric.service;

import gyullectric.gyullectric.util.PredictionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionClient predictionClient;

    /**
     * Flask API를 호출하여 예측 결과를 받아옵니다.
     * @param product 제품명 (예: "A타입")
     * @param startDate 시작일 (예: "2025-05-01")
     * @param endDate 종료일 (예: "2025-05-30")
     * @return 예측 결과 Map (예: {"predicted_order_quantity": 123, ...})
     */
    public Map<String, Object> getPrediction(String product, String startDate, String endDate) {
        log.info("예측 요청 - 제품: {}, 기간: {} ~ {}", product, startDate, endDate);

        Map<String, Object> response = predictionClient.callPredictionApi(product, startDate, endDate);

        if (response == null || !"success".equals(response.get("status"))) {
            log.error("예측 API 응답 실패: {}", response);
            throw new IllegalStateException("예측 결과를 불러오는 데 실패했습니다.");
        }

        log.info("예측 결과 수신: {}", response);
        return response;
    }
}
