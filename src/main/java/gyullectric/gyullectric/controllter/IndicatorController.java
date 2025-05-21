package gyullectric.gyullectric.controllter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/indicators")
public class IndicatorController {

    @GetMapping("/defect-predict")
    public String getDefectPredict() {
        return "productionIndex/defectLog";
    }

    @GetMapping("/order-predict")
    public String getOrderPredict() {
        return "productionIndex/orderPrediction";
    }

    @GetMapping("/analysis")
    public String getAnalysis() {
        return "productionIndex/productionAnalysis";
    }

    @GetMapping("/statistics")
    public String getStatistics() {
        return "productionIndex/productionStats";
    }
}
