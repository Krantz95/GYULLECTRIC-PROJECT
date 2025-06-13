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
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MonitoringDataService monitoringDataService;
    private final MonitoringService monitoringService;
    private final OrderListRepository orderListRepository;
    private final ProductService productService;
    private final BikeProductionRepository bikeProductionRepository;
    private final KpiService kpiService;
    private final ObjectMapper objectMapper; // ✅ 스프링 빈으로 주입받기

    /* ---------- 대시보드 메인 ---------- */
    @GetMapping("/main")
    public String getDashboard(Model model, HttpSession session) throws JsonProcessingException {
        // 0. 로그인 체크 (feat/minjeong 브랜치 로직 통합)
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        /* 1) 현재 진행 중인 주문 ID (없으면 최근 주문) */
        Long orderId = findCurrentOrderId();
        model.addAttribute("orderId", orderId);
        log.info("현재 작업 중인 주문 id: {}", orderId);

        /* 2) 공정 모니터링 데이터 */
        addProcessData(model, orderId);

        /* 3) 금일 KPI 요약 */
        addTodayKpi(model);

        /* 4) 도넛 차트 초기값 (고정) */
        model.addAttribute("initialChartData", buildStaticDonutChartJson());

        /* 5) 주문 목록 & 6) 주문/공정 로그 결합 달성률 Bar 차트 */
        List<OrderList> orderLists = productService.allFindOrderList();
        List<ProcessLog> processLogs = monitoringService.allFindProcesses();
        model.addAttribute("orderList", orderLists);
        model.addAttribute("chartData", buildBarChartJson(orderLists, processLogs));

        /* 7) 금일 생산 실적 */
        model.addAttribute("productionList", buildTodayProductionList());

        /* 8) 에러 알림 제품 목록 (상수) */
        model.addAttribute("productNames", List.of("PedalAt4", "GyulRide", "InteliBike"));

        return "dashboard";
    }

    /* ---------- 공지 페이지 ---------- */
    @GetMapping("/notice")
    public String showNoticePage() {
        return "notice";
    }

    /* ================== Private Helpers ================== */

    /** 현재 진행 중인 주문 ID → 없으면 최근 주문 */
    private Long findCurrentOrderId() {
        return orderListRepository.findByProcessStatus(ProcessStatus.IN_PROGRESS).stream()
                .findFirst()
                .map(OrderList::getId)
                .or(() -> orderListRepository.findTopByOrderByIdDesc().map(OrderList::getId))
                .orElse(null);
    }

    /** 공정별 데이터 → Model 바인딩 */
    private void addProcessData(Model model, Long orderId) {
        ProcessDataDto dto = monitoringDataService.getProcessData(orderId);
        model.addAttribute("productCount", dto.getProductCount());
        model.addAttribute("totalCount", dto.getTotalCount());
        model.addAttribute("ngCountByStep", dto.getNgCountByStep());
        model.addAttribute("okCountByStep", dto.getOkCountByStep());
        model.addAttribute("countByStep", dto.getCountBystep());
    }

    /** 금일 KPI 요약 → Model 바인딩 */
    private void addTodayKpi(Model model) {
        ProductionKpiDto kpi = kpiService.getTodayProductionKpi();
        model.addAttribute("kpi", kpi);
        model.addAttribute("currentSpeed", kpi.getCurrentSpeed());
        model.addAttribute("expectedRate", kpi.getExpectedRate());
        model.addAttribute("onTime", kpi.isOnTime());
        model.addAttribute("estimatedTime", kpi.getEstimatedTime());
    }

    /** 도넛 차트 초기 데이터 (고정) */
    private String buildStaticDonutChartJson() throws JsonProcessingException {
        Map<String, Object> data = Map.of(
                "labels", List.of("GyulRide", "InteliBike", "PedalAt4"),
                "targetCounts", List.of(100, 100, 100)
        );
        return objectMapper.writeValueAsString(data);
    }

    /** 주문 + 공정 로그 → 달성률 Bar 차트 JSON */
    private String buildBarChartJson(List<OrderList> orderLists, List<ProcessLog> processLogs) throws JsonProcessingException {
        // 주문별 공정 요약
        Map<Long, OrderSummaryDto> summaryByOrder = monitoringDataService.getOrderSummaryByOrderId(processLogs);

        // 제품별 누적 집계
        Map<String, Long> totalOrderQty = new HashMap<>();
        Map<String, Long> totalComplete = new HashMap<>();

        for (OrderList order : orderLists) {
            String product = order.getProductName().name();
            totalOrderQty.merge(product, (long) order.getQuantity(), Long::sum);

            OrderSummaryDto s = summaryByOrder.getOrDefault(order.getId(), new OrderSummaryDto(0, 0, 0));
            totalComplete.merge(product, (long) s.getFinishedLots(), Long::sum);
        }

        List<String> labels = new ArrayList<>(totalOrderQty.keySet());
        List<Long> orderQty = labels.stream().map(totalOrderQty::get).collect(Collectors.toList());
        List<Long> productCnt = labels.stream().map(totalComplete::get).collect(Collectors.toList());
        List<Double> achievementRates = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            long total = orderQty.get(i);
            long done = productCnt.get(i);
            achievementRates.add(total == 0 ? 0.0 : Math.round(done * 1000.0 / total) / 10.0);
        }

        Map<String, Object> chart = Map.of(
                "labels", labels,
                "orderQuantity", orderQty,
                "productCount", productCnt,
                "achievementRates", achievementRates
        );
        return objectMapper.writeValueAsString(chart);
    }

    /** 금일 생산 실적 테이블 DTO */
    private List<BikeProductionDto> buildTodayProductionList() {
        return bikeProductionRepository.findByProductionDate(LocalDate.now()).stream()
                .map(p -> new BikeProductionDto(p.getProductName(), p.getTargetCount(), p.getActualCount()))
                .collect(Collectors.toList());
    }
}
