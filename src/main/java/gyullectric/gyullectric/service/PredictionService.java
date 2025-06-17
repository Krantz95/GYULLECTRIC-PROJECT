package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.util.PredictionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionClient predictionClient;
    private final OrderService orderService; // 현재 재고 조회용

    /**
     * 📦 3일치 수요 입력 → 7일 수요 예측 → 재고 조회 → 권장 발주량 계산
     */
    public List<Map<String, Object>> getForecastAndRecommendations(List<Integer> demandValues) {
        Map<String, Object> response = predictionClient.callPredictionApi(demandValues);

        if (response == null || !"success".equals(response.get("status"))) {
            log.error("❌ 예측 API 응답 실패 또는 오류 발생: {}", response);
            return Collections.emptyList();
        }

        List<Integer> predictedDaily = (List<Integer>) response.get("predicted_daily");
        int predictedTotal = (int) response.get("predicted_total");

        List<Map<String, Object>> result = new ArrayList<>();

        for (PartName partName : PartName.values()) {
            log.info("✅ 예측 대상 부품: {}", partName);

            int currentStock = orderService.getInventoryQuantity(partName);
            int recommendedOrder = Math.max(predictedTotal - currentStock, 0);

            log.info("📦 {} 현재 재고 수량: {}", partName, currentStock);

            Map<String, Object> row = new HashMap<>();
            row.put("part", partName.getLabel());
            row.put("predicted", predictedTotal);
            row.put("currentStock", currentStock);
            row.put("recommended", recommendedOrder);
            row.put("dailyForecast", predictedDaily);

            log.info("📝 결과 추가 - 부품: {}, 예측수요: {}, 재고: {}, 권장발주: {}",
                    partName.getLabel(), predictedTotal, currentStock, recommendedOrder);

            result.add(row);
        }

        return result;
    }

    /**
     * 📊 예측 없이 현재 재고만 조회 (초기 진입 시 사용)
     */
    public List<Map<String, Object>> getInventoryStatusOnly() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (PartName partName : PartName.values()) {
            int currentStock = orderService.getInventoryQuantity(partName);

            Map<String, Object> row = new HashMap<>();
            row.put("part", partName.getLabel());
            row.put("predicted", null);
            row.put("currentStock", currentStock);
            row.put("recommended", null);
            row.put("dailyForecast", null);

            result.add(row);
        }

        return result;
    }
}
