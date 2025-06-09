package gyullectric.gyullectric.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.dto.ProcessDataDto;
import gyullectric.gyullectric.repository.OrderListRepository;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MonitoringDataService monitoringDataService;
    private final OrderListRepository orderListRepository;
    private final ProductService productService;

    @GetMapping("/main")
    public String getDashboard(Model model)throws JsonProcessingException {
// 공정모니터링
        List<OrderList> orderLists = orderListRepository.findByProcessStatus(ProcessStatus.IN_PROGRESS);
        Long orderId = !orderLists.isEmpty()
                ? orderLists.get(0).getId()
                : orderListRepository.findTopByOrderByIdDesc().map(OrderList::getId).orElse(null);

        ProcessDataDto processDataDto = monitoringDataService.getProcessData(orderId);
        log.info("현재작업중인 id: {}", orderId);

        model.addAttribute("orderId", orderId);

        // 차트용 데이터
        model.addAttribute("productCount", processDataDto.getProductCount());
        model.addAttribute("totalCount", processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        model.addAttribute("countByStep", processDataDto.getCountBystep());

//        실시간 목표달성량
        // 기존에 화면에 뿌려줄 정적/초기 데이터 처리 (주문 리스트, 통계 등)
        List<OrderList> orderList2 = productService.allFindOrderList();
        // 필요한 데이터 가공 후 모델에 넣기
        model.addAttribute("orderList", orderList2);

        // (필요하면) 차트 초기 데이터 JSON도 같이 보내기
        Map<String, Object> initialChartData = new HashMap<>();
        // 예) 제품명, 목표 수량 등
        initialChartData.put("labels", List.of("GyulRide", "InteliBike", "PedalAt4"));
        initialChartData.put("targetCounts", List.of(100, 100, 100));

        ObjectMapper mapper = new ObjectMapper();
        model.addAttribute("initialChartData", mapper.writeValueAsString(initialChartData));

        return "dashboard";
    }

    @GetMapping("/notice")
    public String showNoticePage() {
        return "notice";
    }
}
