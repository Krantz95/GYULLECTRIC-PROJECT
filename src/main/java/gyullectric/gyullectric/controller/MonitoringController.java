package gyullectric.gyullectric.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitoringController {

    @GetMapping("/monitoring")
    public String getMonitoringStatus() {
        return "process/processList";
    }

    @GetMapping("/productList")
    public String getProductList() {
        return "process/completeProductList";
    }
}
