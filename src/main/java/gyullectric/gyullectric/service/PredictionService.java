package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.util.PredictionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final PredictionClient predictionClient;
    private final OrderService orderService;

    public List<Map<String, Object>> getPrediction(String startDate, String endDate) {
        Map<String, Object> response = predictionClient.callPredictionApi(startDate, endDate);

        if (response == null) {
            log.error("❌ 예측 API 호출 실패: 응답이 null입니다.");
            return Collections.emptyList();
        }

        Object status = response.get("status");
        if (!(status instanceof String) || !"success".equals(status)) {
            log.error("❌ 예측 API 실패 상태 반환: {}", status);
            return Collections.emptyList();
        }

        Object dataObj = response.get("data");
        if (!(dataObj instanceof List<?>)) {
            log.error("❌ 예측 API 데이터 형식 오류: {}", dataObj);
            return Collections.emptyList();
        }

        List<?> rawList = (List<?>) dataObj;
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object item : rawList) {
            if (!(item instanceof Map)) {
                log.warn("⚠️ 예측 항목이 Map 타입이 아님: {}", item);
                continue;
            }

            Map<?, ?> rawMap = (Map<?, ?>) item;
            try {
                String part = Objects.toString(rawMap.get("part"), null);
                Number predictedNum = (Number) rawMap.get("predicted");

                if (part == null || predictedNum == null) {
                    log.warn("⚠️ part 또는 predicted 값이 null임 → 건너뜀: {}", rawMap);
                    continue;
                }

                PartName partName = PartName.fromString(part);
                int predictedDemand = predictedNum.intValue();
                int currentStock = orderService.getInventoryQuantity(partName);
                int recommendedOrder = Math.max(predictedDemand - currentStock, 0);

                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("part", partName.getLabel());  // 한글 라벨
                rowMap.put("predicted", predictedDemand);
                rowMap.put("currentStock", currentStock);
                rowMap.put("recommended", recommendedOrder);

                result.add(rowMap);

            } catch (IllegalArgumentException e) {
                log.warn("⚠️ 알 수 없는 부품명: '{}' → 스킵됨", rawMap.get("part"));
            } catch (ClassCastException e) {
                log.warn("⚠️ 형변환 실패 (predicted 값): {}", rawMap.get("predicted"));
            }
        }

        return result;
    }
}
