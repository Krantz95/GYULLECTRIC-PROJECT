package gyullectric.gyullectric.controller;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.dto.MonitoringDto;
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

        ProcessDataDto processDataDto = monitoringDataService.getProcessData(id);
        model.addAttribute("loginMember", loginMember);

        model.addAttribute("processes", processDataDto.getProcesses());
        model.addAttribute("orderId", id);
        model.addAttribute("productCount", processDataDto.getProductCount());
        model.addAttribute("totalCount", processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        return "process/processList";
    }

    @GetMapping("/monitoring/list")
    public String monitoringListChart(Model model){
        List<OrderList> orderLists = orderListRepository.findByProcessStatus(ProcessStatus.IN_PROGRESS);
        Long orderId = !orderLists.isEmpty()
                ? orderLists.get(0).getId()
                : orderListRepository.findTopByOrderByIdDesc().map(OrderList::getId).orElse(null);

        ProcessDataDto processDataDto =  monitoringDataService.getProcessData(orderId);

        log.info("현재작업중인 id{}", orderId);

        model.addAttribute("processes", processDataDto.getProcesses());
        model.addAttribute("orderId", orderId);

//        차트를 위한 추가 ProcessController의 processList메서드를 복붙
        model.addAttribute("productCount", processDataDto.getProductCount());
        model.addAttribute("totalCount", processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep", processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep", processDataDto.getOkCountByStep());
        model.addAttribute("countByStep", processDataDto.getCountBystep());

//        날짜별 완제품 수 계산
        LocalDate today = LocalDate.now();
        ProcessDataDto dto = monitoringDataService.getProcessDataByDate(today);

        model.addAttribute("date", today);
        model.addAttribute("productCountDate", dto.getProductCount());




        return "process/processWebSocket";

    }

    @GetMapping("/monitoring/product/list")
    public String monitoringProduct(Model model) throws JsonProcessingException {
        List<OrderList> orderLists = productService.allFindOrderList();

        // 제품별 누적 데이터
        Map<String, Long> totalOrderQuantityByProduct = new HashMap<>();
        Map<String, Long> totalProductCountByProduct = new HashMap<>();
        Map<String, Long> totalNgCountByProduct = new HashMap<>();

        // 주문별 상세 데이터
        Map<Long, Long> errorCountMap = new HashMap<>();
        Map<Long, Long> completeCountMap = new HashMap<>();
        Map<Long, Double> achievementRateMap = new HashMap<>();

        for (OrderList orderList : orderLists) {
            String productName = String.valueOf(orderList.getProductName());
            Long orderId = orderList.getId();
            int orderQty = orderList.getQuantity();

            // 제품별 누적 데이터 계산
            totalOrderQuantityByProduct.put(productName,
                    totalOrderQuantityByProduct.getOrDefault(productName, 0L) + orderQty);

            ProcessDataDto processData = monitoringDataService.getProcessData(orderId);

            long completeCount = processData.getProductCount();
            long ngCount = processData.getNgCountByStep().values().stream().mapToLong(Long::longValue).sum();

            totalProductCountByProduct.put(productName,
                    totalProductCountByProduct.getOrDefault(productName, 0L) + completeCount);

            totalNgCountByProduct.put(productName,
                    totalNgCountByProduct.getOrDefault(productName, 0L) + ngCount);

            // 주문별 데이터 계산
            errorCountMap.put(orderId, ngCount);
            completeCountMap.put(orderId, completeCount);
            double achievementRate = (orderQty == 0) ? 0.0 : (completeCount * 100.0 / orderQty);
            achievementRateMap.put(orderId, achievementRate);
        }

        // 제품명 리스트 (차트용)
        List<String> productNames = new ArrayList<>(totalOrderQuantityByProduct.keySet());

        // 제품별 달성률 리스트 계산
        List<Double> achievementRates = new ArrayList<>();
        for (String productName : productNames) {
            long orderQty = totalOrderQuantityByProduct.getOrDefault(productName, 0L);
            long productCount = totalProductCountByProduct.getOrDefault(productName, 0L);
            double rate = (orderQty == 0) ? 0.0 : (productCount * 100.0 / orderQty);
            achievementRates.add(rate);
        }

        // 차트 데이터
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", productNames);
        chartData.put("data", achievementRates);

        // 모델에 데이터 전달
        model.addAttribute("chartData", objectMapper.writeValueAsString(chartData));

        model.addAttribute("productNames", productNames);
        model.addAttribute("totalOrderQuantityByProduct", totalOrderQuantityByProduct);
        model.addAttribute("totalProductCountByProduct", totalProductCountByProduct);
        model.addAttribute("totalNgCountByProduct", totalNgCountByProduct);

        model.addAttribute("orderLists", orderLists);
        model.addAttribute("errorCountMap", errorCountMap);
        model.addAttribute("completeCountMap", completeCountMap);
        model.addAttribute("achievementRateMap", achievementRateMap);

        return "process/completeProductList";
    }


    }

