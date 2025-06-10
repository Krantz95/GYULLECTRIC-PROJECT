package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.dto.BottleneckDto;
import gyullectric.gyullectric.dto.ProductionKpiDto;
import gyullectric.gyullectric.service.BottleneckService;
import gyullectric.gyullectric.service.KpiService;
import gyullectric.gyullectric.service.PredictionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final PredictionService predictionService;
    private final KpiService kpiService;
    private final BottleneckService bottleneckService;

    /** 📌 발주 예측 페이지 (초기 진입 시 재고만 표시) */
    @GetMapping("/order-predict")
    public String getManualPredictionForm(Model model, HttpSession session) {
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/login";
        }

        model.addAttribute("inputValues", List.of("", "", ""));
        model.addAttribute("predictedData", predictionService.getInventoryStatusOnly());
        model.addAttribute("predictedTotal", null);
        model.addAttribute("shortageParts", null); // KPI 초기화
        return "productionIndex/orderPrediction";
    }

    /** ✅ 발주 예측 실행: 수요량 3개 → 예측 결과 + 재고 기반 권장 발주량 표시 */
    @PostMapping("/order-predict")
    public String postManualPrediction(@RequestParam int demand1,
                                       @RequestParam int demand2,
                                       @RequestParam int demand3,
                                       Model model,
                                       HttpSession session) {
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/login";
        }

        List<Integer> inputValues = List.of(demand1, demand2, demand3);

        // 예측 결과 + 권장 발주량 계산
        List<Map<String, Object>> predictedData = predictionService.getForecastAndRecommendations(inputValues);

        model.addAttribute("inputValues", inputValues);
        model.addAttribute("predictedData", predictedData);

        // KPI 출력용 데이터 바인딩
        if (!predictedData.isEmpty()) {
            model.addAttribute("predictedTotal", predictedData.get(0).get("predicted"));

            List<String> shortageParts = predictedData.stream()
                    .filter(row -> {
                        Object recommended = row.get("recommended");
                        return recommended instanceof Integer && ((Integer) recommended) > 0;
                    })
                    .map(row -> (String) row.get("part"))
                    .collect(Collectors.toList());

            model.addAttribute("shortageParts", shortageParts);
        } else {
            model.addAttribute("predictedTotal", 0);
            model.addAttribute("shortageParts", null);
            model.addAttribute("error", "예측 결과가 없습니다. 다시 시도해주세요.");
        }

        return "productionIndex/orderPrediction";
    }

    /** 📌 생산 분석 페이지 (KPI 출력 포함) */
    @GetMapping("/analysis")
    public String getAnalysis(Model model, HttpSession session) {
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/login";
        }

        ProductionKpiDto kpi = kpiService.getTodayProductionKpi();
        model.addAttribute("kpi", kpi);
        model.addAttribute("currentSpeed", kpi.getCurrentSpeed());
        model.addAttribute("expectedRate", kpi.getExpectedRate());
        model.addAttribute("onTime", kpi.isOnTime());
        model.addAttribute("estimatedTime", kpi.getEstimatedTime());

        // ✅ 병목 분석 결과 추가
        BottleneckDto bottleneckDto = bottleneckService.analyzeTodayBottleneck();
        model.addAttribute("bottleneck", bottleneckDto);


        return "productionIndex/productionAnalysis";
    }
}
