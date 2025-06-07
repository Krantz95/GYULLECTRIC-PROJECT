package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.dto.ProcessDataDto;
import gyullectric.gyullectric.repository.OrderListRepository;
import gyullectric.gyullectric.service.MonitoringDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MonitoringDataService monitoringDataService;
    private final OrderListRepository orderListRepository;

    @GetMapping("/main")
    public String getDashboard(Model model) {
        // 현재 진행 중인 오더 ID 가져오기
        List<OrderList> orders = orderListRepository.findByProcessStatus(ProcessStatus.IN_PROGRESS);
        Long orderId = !orders.isEmpty()
                ? orders.get(0).getId()
                : orderListRepository.findTopByOrderByIdDesc().map(OrderList::getId).orElse(null);

        // 공정별 OK/NG 수 가져오기
        ProcessDataDto processDataDto = monitoringDataService.getProcessData(orderId);

        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("orderId", orderId);
        return "dashboard";
    }

    @GetMapping("/notice")
    public String showNoticePage() {
        return "notice";
    }
}
