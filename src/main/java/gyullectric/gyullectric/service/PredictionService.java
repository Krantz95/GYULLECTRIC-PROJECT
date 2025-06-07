//package gyullectric.gyullectric.service;
//
//import gyullectric.gyullectric.domain.PartName;
//import gyullectric.gyullectric.util.PredictionClient;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//
//public class PredictionService {
//
//    private final PredictionClient predictionClient;
//
//    /**
//     * Flask API를 호출하여 예측 결과를 받아옵니다.
//     *
//     * @param product   제품명 (예: "A타입")
//     * @param startDate 시작일 (예: "2025-05-01")
//     * @param endDate   종료일 (예: "2025-05-30")
//     * @return 예측 결과 Map (예: {"predicted_order_quantity": 123, ...})
//     */
//    public Map<String, Object> getPrediction(String product, String startDate, String endDate) {
//        log.info("예측 요청 - 제품: {}, 기간: {} ~ {}", product, startDate, endDate);
//
//        Map<String, Object> response = predictionClient.callPredictionApi(product, startDate, endDate);
//
//        @Slf4j
//        public class PredictionService {
//
//            private final PredictionClient predictionClient;
//            private final OrderService orderService;  // InventoryService → OrderService
//
//            public List<Map<String, Object>> getPrediction(String startDate, String endDate) {
//                Map<String, Object> response = predictionClient.callPredictionApi(startDate, endDate);
//
//
//                if (response == null || !"success".equals(response.get("status"))) {
//                    log.error("예측 API 응답 실패: {}", response);
//                    return Collections.emptyList();
//                }
//
//                List<Map<String, Object>> predicted = (List<Map<String, Object>>) response.get("data");
//                List<Map<String, Object>> result = new ArrayList<>();
//
//                for (Map<String, Object> row : predicted) {
//                    String part = (String) row.get("part");
//                    int predictedDemand = (int) row.get("predicted");
//
//                    try {
//                        // 👇 label이나 name을 모두 처리 가능
//                        PartName partName = PartName.fromString(part);
//                        int currentStock = orderService.getInventoryQuantity(partName);
//                        int recommendedOrder = Math.max(predictedDemand - currentStock, 0);
//
//                        Map<String, Object> rowMap = new HashMap<>();
//                        rowMap.put("part", partName.getLabel());  // 한글 라벨로 출력
//                        rowMap.put("predicted", predictedDemand);
//                        rowMap.put("currentStock", currentStock);
//                        rowMap.put("recommended", recommendedOrder);
//
//                        result.add(rowMap);
//                    } catch (IllegalArgumentException e) {
//                        log.warn("⚠️ 알 수 없는 부품명: '{}' → 스킵됨", part);
//                    }
//                }
//
//                return result;
//            }
//        }
//
