package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final PredictionService predictionService;

    // 불량 예측 페이지
    @GetMapping("/defect-predict")
    public String getDefectPredict() {
        return "productionIndex/defectLog";
    }

    // 발주 예측 페이지 GET: 기본 값으로 처음 렌더링
    @GetMapping("/order-predict")
    public String getOrderPredict(
            @RequestParam(defaultValue = "A타입") String product,
            @RequestParam(defaultValue = "2025-05-01") String startDate,
            @RequestParam(defaultValue = "2025-05-30") String endDate,
            Model model) {

        // 로그: Flask 예측 API 호출 전 시간 측정 시작
        long start = System.currentTimeMillis();

        // Flask API 호출
        Map<String, Object> result = predictionService.getPrediction(product, startDate, endDate);

        // 로그: Flask 예측 API 응답 완료
        long end = System.currentTimeMillis();
        System.out.println("🔍 Flask 예측 API 응답 소요 시간: " + (end - start) + "ms");

        // 예측 결과를 뷰에 전달
        model.addAttribute("predictedData", result);
        model.addAttribute("selectedProduct", product);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "productionIndex/orderPrediction";
    }


    // 발주 예측 POST: 폼 제출 시
    @PostMapping("/order-predict")
    public String postOrderPredict(
            @RequestParam String product,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model) {

        Map<String, Object> result = predictionService.getPrediction(product, startDate, endDate);

        model.addAttribute("predictedData", result);
        model.addAttribute("selectedProduct", product);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "productionIndex/orderPrediction";
    }

    // 생산 분석 페이지
    @GetMapping("/analysis")
    public String getAnalysis() {
        return "productionIndex/productionAnalysis";
    }

    // 생산 통계 페이지
    @GetMapping("/statistics")
    public String getStatistics() {
        return "productionIndex/productionStats";
    }
}
