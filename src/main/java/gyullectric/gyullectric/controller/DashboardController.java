package gyullectric.gyullectric.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyullectric.gyullectric.domain.BikeProduction;
import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.dto.BikeProductionDto;
import gyullectric.gyullectric.dto.OrderSummaryDto;
import gyullectric.gyullectric.dto.ProcessDataDto;
import gyullectric.gyullectric.repository.BikeProductionRepository;
import gyullectric.gyullectric.repository.OrderListRepository;
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

        //  도넛차트용 데이터
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


//        대시보드 달성률 차트
        List<OrderList> productOrderList = productService.allFindOrderList();
        List<ProcessLog> processLogs = monitoringService.allFindProcesses();

        // 주문별 완제품/불량품/전체 로트 수 집계
        Map<Long, OrderSummaryDto> orderSummaryMap = monitoringDataService.getOrderSummaryByOrderId(processLogs);

        Map<Long, Integer> orderCompleteCountMap = new HashMap<>();
        Map<Long, Integer> orderNgCountMap = new HashMap<>();
        Map<Long, Double> orderAchievementRateMap = new HashMap<>();

        // 제품명 기준 누적 집계용
        Map<String, Long> totalOrderQtyByProduct = new HashMap<>();
        Map<String, Long> totalCompleteByProduct = new HashMap<>();
        Map<String, Long> totalNgByProduct = new HashMap<>();

        // 주문 ID별 제품 기준 누적 정보 맵
        Map<Long, Long> orderProductTotalOrderMap = new HashMap<>();
        Map<Long, Long> orderProductTotalCompleteMap = new HashMap<>();
        Map<Long, Double> orderProductLevelAchievementMap = new HashMap<>();

        for (OrderList order : productOrderList) {
            Long productOrderId = order.getId();
            int orderQty = order.getQuantity();
            String productName = order.getProductName().name();

            OrderSummaryDto summary = orderSummaryMap.getOrDefault(productOrderId, new OrderSummaryDto(0, 0, 0));
            int completeCount = summary.getFinishedLots();
            int ngCount = summary.getNgLots();

            orderCompleteCountMap.put(productOrderId, completeCount);
            orderNgCountMap.put(productOrderId, ngCount);

            // 달성률 계산 (주문수 대비 완제품 비율)
            double achievementRate = (orderQty == 0) ? 0.0 : Math.round((completeCount * 1000.0 / orderQty)) / 10.0;
            orderAchievementRateMap.put(productOrderId, achievementRate);

            // 제품별 누적 데이터 갱신
            totalOrderQtyByProduct.put(productName,
                    totalOrderQtyByProduct.getOrDefault(productName, 0L) + orderQty);
            totalCompleteByProduct.put(productName,
                    totalCompleteByProduct.getOrDefault(productName, 0L) + completeCount);
            totalNgByProduct.put(productName,
                    totalNgByProduct.getOrDefault(productName, 0L) + ngCount);
        }

        // 제품명 기준 달성률 계산
        List<String> labels = new ArrayList<>();
        List<Double> achievementRates = new ArrayList<>();

        for (String productName : totalOrderQtyByProduct.keySet()) {
            long totalOrder = totalOrderQtyByProduct.get(productName);
            long totalComplete = totalCompleteByProduct.getOrDefault(productName, 0L);
            double rate = (totalOrder == 0) ? 0.0 : Math.round((totalComplete * 1000.0 / totalOrder)) / 10.0;

            labels.add(productName);
            achievementRates.add(rate);
        }

        // 제품명 기준 누적 정보 전달 (주문별 기반)
        model.addAttribute("orderProductTotalOrderMap", orderProductTotalOrderMap);
        model.addAttribute("orderProductTotalCompleteMap", orderProductTotalCompleteMap);
        model.addAttribute("orderProductLevelAchievementMap", orderProductLevelAchievementMap);

        // 제품명 기준 누적 정보 전달 (직접 조회 가능하도록)
        model.addAttribute("totalOrderQtyByProduct", totalOrderQtyByProduct);
        model.addAttribute("totalCompleteByProduct", totalCompleteByProduct);
        model.addAttribute("totalNgByProduct", totalNgByProduct);

        for (OrderList order : orderLists) {
            String productName = order.getProductName().name();
            System.out.println("OrderID=" + order.getId() + ", productName=" + productName
                    + ", orderQty=" + order.getQuantity());
        }
        System.out.println("totalOrderQtyByProduct: " + totalOrderQtyByProduct);
        System.out.println("totalCompleteByProduct: " + totalCompleteByProduct);
        for (Map.Entry<Long, OrderSummaryDto> entry : orderSummaryMap.entrySet()) {
            System.out.println("OrderID=" + entry.getKey()
                    + ", finishedLots=" + entry.getValue().getFinishedLots()
                    + ", ngLots=" + entry.getValue().getNgLots());
        }

//         차트 데이터 구성
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("orderQuantity", labels.stream().map(totalOrderQtyByProduct::get).toList());
        chartData.put("productCount", labels.stream().map(totalCompleteByProduct::get).toList());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonChartData = objectMapper.writeValueAsString(chartData);
        model.addAttribute("chartData", jsonChartData);

        LocalDate today = LocalDate.now();
        List<BikeProduction> productions = bikeProductionRepository.findByProductionDate(today);

        List<BikeProductionDto> dtoList = productions.stream()
                .map(p -> new BikeProductionDto(p.getProductName(), p.getTargetCount(), p.getActualCount()))
                .collect(Collectors.toList());

        model.addAttribute("productionList", dtoList);

//        에러 알람기능
        model.addAttribute("productNames", List.of("PedalAt4", "GyulRide", "InteliBike"));
        return "dashboard";

    }

    @GetMapping("/notice")
    public String showNoticePage() {
        return "notice";
    }
}
