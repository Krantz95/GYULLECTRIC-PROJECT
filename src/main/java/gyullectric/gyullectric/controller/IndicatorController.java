package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // 초기 진입 시 예측하지 않음
    @GetMapping("/order-predict")
    public String getOrderPredict(Model model) {
        // 페이지 진입 시 예측 호출하지 않음
        model.addAttribute("predictedData", null); // 템플릿에서 안내 메시지 출력용
        return "productionIndex/orderPrediction";
    }

    // 버튼 누르면 POST 요청으로 예측 실행
    @PostMapping("/order-predict")
    public String postOrderPredict(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model) {

        List<Map<String, Object>> result = predictionService.getPrediction(startDate, endDate);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("predictedData", result);

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

    /** ✅ 발주 예측 뷰에 공통 데이터 바인딩 */
    private void populateModelForOrderPrediction(Model model, List<Map<String, Object>> result,
                                                 String startDate, String endDate) {
        model.addAttribute("predictedData", result);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
    }
}
