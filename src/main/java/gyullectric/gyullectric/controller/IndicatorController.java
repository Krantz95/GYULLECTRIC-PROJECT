package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final PredictionService predictionService;

    /** 📌 불량 예측 페이지 */
    @GetMapping("/defect-predict")
    public String getDefectPredict() {
        return "productionIndex/defectLog";
    }

    /** 📌 발주 예측 페이지 (초기 진입 시 예측 안함) */
    @GetMapping("/order-predict")
    public String getOrderPredict(Model model) {
        model.addAttribute("predictedData", null);
        return "productionIndex/orderPrediction";
    }

    /** ✅ 예측 실행: Flask API 호출 후 결과 바인딩 */
    @PostMapping("/order-predict")
    public String postOrderPredict(@RequestParam String startDate,
                                   @RequestParam String endDate,
                                   Model model) {

        // 1. Flask API 호출
        String flaskUrl = "http://127.0.0.1:5000/predict";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, entity, Map.class);

        // 2. 응답 데이터 파싱
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> predictedData = (List<Map<String, Object>>) responseBody.get("data");

        // 3. 모델에 바인딩
        populateModelForOrderPrediction(model, predictedData, startDate, endDate);
        return "productionIndex/orderPrediction";
    }

    /** 📌 생산 분석 페이지 */
    @GetMapping("/analysis")
    public String getAnalysis() {
        return "productionIndex/productionAnalysis";
    }

    /** 📌 생산 통계 페이지 */
    @GetMapping("/statistics")
    public String getStatistics() {
        return "productionIndex/productionStats";
    }

    /** ✅ 공통 모델 바인딩 메서드 */
    private void populateModelForOrderPrediction(Model model,
                                                 List<Map<String, Object>> result,
                                                 String startDate,
                                                 String endDate) {
        model.addAttribute("predictedData", result);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
    }
}
