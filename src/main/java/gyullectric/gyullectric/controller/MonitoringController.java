package gyullectric.gyullectric.controller;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.ComplateSummaryDto;
import gyullectric.gyullectric.dto.MonitoringDto;
import gyullectric.gyullectric.dto.OrderSummaryDto;
import gyullectric.gyullectric.dto.ProcessDataDto;
import gyullectric.gyullectric.repository.OrderListRepository;
import gyullectric.gyullectric.service.MonitoringDataService;
import gyullectric.gyullectric.service.MonitoringService;
import gyullectric.gyullectric.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MonitoringController {
    private final ProductService productService;
    private final MonitoringService monitoringService;
    private final MonitoringDataService monitoringDataService;
    private final OrderListRepository orderListRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/monitoring/new/{id}")
    public String getMonitoringStatus(@PathVariable("id")Long id) {
        OrderList orderList = productService.oneFindOrderList(id).orElseThrow(()->new IllegalArgumentException("해당 주문을 찾을 수 없습니다"));
        orderList.setProcessStatus(ProcessStatus.IN_PROGRESS);
        productService.updateOrderState(orderList.getId(), orderList.getProcessStatus());
        monitoringService.processSave(orderList);
        return String.format("redirect:/monitoring/list/%s", id);
    }

    @GetMapping("/monitoring/list/{id}")
    public String getProductList(@PathVariable("id")Long id, Model model, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember == null){
            return "redirect:/login";
        }

        ProcessDataDto processDataDto = monitoringDataService.getProcessData(id);
        List<ProcessLog> sortedProcesses = processDataDto.getProcesses().stream()
                .sorted(Comparator.comparing(ProcessLog::getCreateAt).reversed()
                        .thenComparing(ProcessLog::getLotNumber)
                        .thenComparingInt(ProcessLog::getProcessStep))
                .collect(Collectors.toList());

        model.addAttribute("processes", sortedProcesses);


        model.addAttribute("loginMember", loginMember);

        model.addAttribute("orderId", id);
        model.addAttribute("productCount", processDataDto.getProductCount());
        model.addAttribute("totalCount", processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        return "process/processList";
    }

    @GetMapping("/monitoring/list")
    public String monitoringListChart(Model model, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember == null){
            return "redirect:/login";
        }
        // 현재 진행 중인 작업 조회
        List<OrderList> orderLists = orderListRepository.findByProcessStatus(ProcessStatus.IN_PROGRESS);
        Long orderId = !orderLists.isEmpty()
                ? orderLists.get(0).getId()
                : orderListRepository.findTopByOrderByIdDesc().map(OrderList::getId).orElse(null);

        ProcessDataDto processDataDto = monitoringDataService.getProcessData(orderId);
        log.info("현재작업중인 id: {}", orderId);

        List<ProcessLog> sortedProcesses = processDataDto.getProcesses().stream()
                .sorted(Comparator.comparing(ProcessLog::getCreateAt).reversed()
                        .thenComparing(ProcessLog::getLotNumber)
                        .thenComparingInt(ProcessLog::getProcessStep))
                .collect(Collectors.toList());

        for(ProcessLog processLog : sortedProcesses) {
            String errorDesc = getErrorDescription(processLog.getErrorCode(), processLog.getErrorValue());
            processLog.setErrorDescription(errorDesc);
        }

        model.addAttribute("processes", sortedProcesses);

        model.addAttribute("orderId", orderId);

        // 차트용 데이터
        model.addAttribute("productCount", processDataDto.getProductCount());
        model.addAttribute("totalCount", processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        model.addAttribute("countByStep", processDataDto.getCountBystep());

        // 날짜별 완제품 수 계산
        LocalDate today = LocalDate.now();
        ProcessDataDto dto = monitoringDataService.getProcessDataByDate(today);
        model.addAttribute("date", today);
        model.addAttribute("productCountDate", dto.getProductCount());

        return "process/processWebSocket";

    }

    private String getErrorDescription(String errorCode, Double value) {
        switch (errorCode) {
            case "ERROR_101":
                return value != null ? String.format("용접 출력 과다 (%.1fV)", value) : "용접 출력 과다";
            case "ERROR_102":
                return value != null ? String.format("용접 출력 부족 (%.1fV)", value) : "용접 출력 부족";
            case "ERROR_201":
                return value != null ? String.format("온도 이상 (%.1f℃)", value) : "온도 이상";
            case "ERROR_202":
                return value != null ? String.format("온도 이하 (%.1f℃)", value) : "온도 이하";
            case "ERROR_103":
                return "스크래치 불량";
            case "ERROR_110":
                return "조립 불량";
            case "ERROR_111":
                return "기능 불량";
            case "EXCEPTION_ERROR":
                return "EXCEPTION_ERROR";
            default:
                return "";
        }
    }

    @GetMapping("/monitoring/product/list")
    public String monitoringProduct(Model model, HttpSession session) throws JsonProcessingException {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember == null){
            return "redirect:/login";
        }
        List<OrderList> orderLists = productService.allFindOrderList();
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

        for (OrderList order : orderLists) {
            Long orderId = order.getId();
            int orderQty = order.getQuantity();
            String productName = order.getProductName().name();

            OrderSummaryDto summary = orderSummaryMap.getOrDefault(orderId, new OrderSummaryDto(0, 0, 0));
            int completeCount = summary.getFinishedLots();
            int ngCount = summary.getNgLots();

            orderCompleteCountMap.put(orderId, completeCount);
            orderNgCountMap.put(orderId, ngCount);

            // 달성률 계산 (주문수 대비 완제품 비율)
            double achievementRate = (orderQty == 0) ? 0.0 : Math.round((completeCount * 1000.0 / orderQty)) / 10.0;
            orderAchievementRateMap.put(orderId, achievementRate);

            // 제품별 누적 데이터 갱신
            totalOrderQtyByProduct.put(productName,
                    totalOrderQtyByProduct.getOrDefault(productName, 0L) + orderQty);
            totalCompleteByProduct.put(productName,
                    totalCompleteByProduct.getOrDefault(productName, 0L) + completeCount);
            totalNgByProduct.put(productName,
                    totalNgByProduct.getOrDefault(productName, 0L) + ngCount);
        }

        // 각 주문별 제품 누적 정보 저장
        for (OrderList order : orderLists) {
            Long orderId = order.getId();
            String productName = order.getProductName().name();

            long totalOrder = totalOrderQtyByProduct.getOrDefault(productName, 0L);
            long totalComplete = totalCompleteByProduct.getOrDefault(productName, 0L);
            double productLevelAchievement = (totalOrder == 0) ? 0.0 : Math.round((totalComplete * 1000.0 / totalOrder)) / 10.0;

            orderProductTotalOrderMap.put(orderId, totalOrder);
            orderProductTotalCompleteMap.put(orderId, totalComplete);
            orderProductLevelAchievementMap.put(orderId, productLevelAchievement);
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

        // 주문 리스트 및 주문별 통계
        model.addAttribute("orderLists", orderLists);
        model.addAttribute("orderCompleteCountMap", orderCompleteCountMap);
        model.addAttribute("orderNgCountMap", orderNgCountMap);
        model.addAttribute("orderAchievementRateMap", orderAchievementRateMap);

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

        return "process/completeProductList";
    }

}

