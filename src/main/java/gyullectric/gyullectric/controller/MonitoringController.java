package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.dto.ProcessDataDto;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.MonitoringService;
import gyullectric.gyullectric.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MonitoringController {
    private final ProductService productService;
    private final MonitoringService monitoringService;
    private final MonitoringDataService monitoringDataService;

    @PostMapping("/monitoring/new/{id}")
    public String getMonitoringStatus(@PathVariable("id")Long id) {
        OrderList orderList = productService.oneFindOrderList(id).orElseThrow(()->new IllegalArgumentException("해당 아이디를 찾을 수 없습니다"));
        orderList.setProcessStatus(ProcessStatus.IN_PROGRESS);
        productService.updateOrderState(orderList.getId(), orderList.getProcessStatus());
        monitoringService.processSave(orderList);
        return String.format("redirect:/monitoring/list/%s", id);
    }

    @GetMapping("/monitoring/list/{id}")
    public String getProductList(@PathVariable("id")Long id, Model model) {

        ProcessDataDto processDataDto = monitoringDataService.getProcessData(id);

        model.addAttribute("processes", processDataDto.getProcesses());
        model.addAttribute("orderId", id);
        model.addAttribute("productCount", processDataDto.getProductCount());
        model.addAttribute("totalCount", processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        return "process/processList";
    }
}
