package gyullectric.gyullectric.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.*;
import gyullectric.gyullectric.repository.BikeProductionRepository;
import gyullectric.gyullectric.repository.OrderListRepository;
import gyullectric.gyullectric.service.KpiService;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.MonitoringService;
import gyullectric.gyullectric.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MonitoringDataService monitoringDataService;
    private final MonitoringService monitoringService;
    private final OrderListRepository orderListRepository;
    private final ProductService productService;
    private final BikeProductionRepository bikeProductionRepository;
    private final KpiService kpiService;

    @GetMapping("/main")
    public String getDashboard(Model model) throws JsonProcessingException {
        // 1. 현재 작업 중인 주문 ID 결정
        List<OrderList> orderLists = orderListRepository.findByProcessStatus(ProcessStatus.IN_PROGRESS);
        Long orderId = !orderLists.isEmpty()
                ? orderLists.get(0).getId()
                : orderListRepository.findTopByOrderByIdDesc().map(OrderList::getId).orElse(null);
        log.info("현재작업중인 id: {}", orderId);

        // 2. 공정 데이터 조회 및 모델 추가
        ProcessDataDto processDataDto = monitoringDataService.getProcessData(orderId);
        model.addAttribute("orderId", orderId);
        model.addAttribute("productCount", processDataDto.getProductCount());
        model.addAttribute("totalCount", processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        model.addAttribute("countByStep", processDataDto.getCountBystep());

        // 3. KPI 요약 데이터
        ProductionKpiDto kpi = kpiService.getTodayProductionKpi();
        model.addAttribute("kpi", kpi);
        model.addAttribute("currentSpeed", kpi.getCurrentSpeed());
        model.addAttribute("expectedRate", kpi.getExpectedRate());
        model.addAttribute("onTime", kpi.isOnTime());
        model.addAttribute("estimatedTime", kpi.getEstimatedTime());

        // 4. 도넛 차트 초기 데이터
        Map<String, Object> initialChartData = new HashMap<>();
        initialChartData.put("labels", List.of("GyulRide", "InteliBike", "PedalAt4"));
        initialChartData.put("targetCounts", List.of(100, 100, 100));
        model.addAttribute("initialChartData", new ObjectMapper().writeValueAsString(initialChartData));

        // 5. 제품 주문 목록 (표 및 분석용)
        List<OrderList> productOrderList = productService.allFindOrderList();
        model.addAttribute("orderList", productOrderList);

        // 6. 공정 로그 분석을 통한 주문별 요약 정보 생성
        List<ProcessLog> processLogs = monitoringService.allFindProcesses();
        Map<Long, OrderSummaryDto> orderSummaryMap = monitoringDataService.getOrderSummaryByOrderId(processLogs);

        // 누적 통계용 맵
        Map<String, Long> totalOrderQtyByProduct = new HashMap<>();
        Map<String, Long> totalCompleteByProduct = new HashMap<>();
        Map<String, Long> totalNgByProduct = new HashMap<>();

        // 달성률용 리스트
        List<String> labels = new ArrayList<>();
        List<Double> achievementRates = new ArrayList<>();

        for (OrderList order : productOrderList) {
            Long productOrderId = order.getId();
            int orderQty = order.getQuantity();
            String productName = order.getProductName().name();

            OrderSummaryDto summary = orderSummaryMap.getOrDefault(productOrderId, new OrderSummaryDto(0, 0, 0));
            int completeCount = summary.getFinishedLots();
            int ngCount = summary.getNgLots();

            totalOrderQtyByProduct.merge(productName, (long) orderQty, Long::sum);
            totalCompleteByProduct.merge(productName, (long) completeCount, Long::sum);
            totalNgByProduct.merge(productName, (long) ngCount, Long::sum);
        }

        for (String productName : totalOrderQtyByProduct.keySet()) {
            long totalOrder = totalOrderQtyByProduct.get(productName);
            long totalComplete = totalCompleteByProduct.getOrDefault(productName, 0L);
            double rate = (totalOrder == 0) ? 0.0 : Math.round((totalComplete * 1000.0 / totalOrder)) / 10.0;
            labels.add(productName);
            achievementRates.add(rate);
        }

        // 차트용 데이터 JSON 변환
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("orderQuantity", labels.stream().map(totalOrderQtyByProduct::get).toList());
        chartData.put("productCount", labels.stream().map(totalCompleteByProduct::get).toList());
        String jsonChartData = new ObjectMapper().writeValueAsString(chartData);
        model.addAttribute("chartData", jsonChartData);

        // 7. 금일 생산 실적 (생산량 테이블용)
        LocalDate today = LocalDate.now();
        List<BikeProduction> productions = bikeProductionRepository.findByProductionDate(today);
        List<BikeProductionDto> dtoList = productions.stream()
                .map(p -> new BikeProductionDto(p.getProductName(), p.getTargetCount(), p.getActualCount()))
                .collect(Collectors.toList());
        model.addAttribute("productionList", dtoList);

        // 8. 에러 알림용 제품 목록
        model.addAttribute("productNames", List.of("PedalAt4", "GyulRide", "InteliBike"));

        return "dashboard";
    }

    @GetMapping("/notice")
    public String showNoticePage() {
        return "notice";
    }
}
