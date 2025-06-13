package gyullectric.gyullectric.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.BikeProductionDto;
import gyullectric.gyullectric.dto.OrderSummaryDto;
import gyullectric.gyullectric.dto.ProcessDataDto;
import gyullectric.gyullectric.repository.BikeProductionRepository;
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
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public String getDashboard(Model model, HttpSession session)throws JsonProcessingException {
        // ───────────────────────────────── 로그인 체크
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        // ───────────────────────────────── 진행 중 주문 ID
        List<OrderList> inProgress = orderListRepository.findByProcessStatus(ProcessStatus.IN_PROGRESS);
        Long orderId = inProgress.isEmpty()
                ? orderListRepository.findTopByOrderByIdDesc().map(OrderList::getId).orElse(null)
                : inProgress.get(0).getId();

        // ───────────────────────────────── 공정별 OK/NG 카운트
        ProcessDataDto processDataDto = monitoringDataService.getProcessData(orderId);
        model.addAttribute("orderId",         orderId);
        model.addAttribute("productCount",    processDataDto.getProductCount());
        model.addAttribute("totalCount",      processDataDto.getTotalCount());
        model.addAttribute("ngCountByStep",   processDataDto.getNgCountByStep());
        model.addAttribute("okCountByStep",   processDataDto.getOkCountByStep());
        model.addAttribute("countByStep",     processDataDto.getCountBystep());

        // ───────────────────────────────── 오늘 목표량
        Map<ProductName, Integer> todayTargetMap = productService.getTodayTargetMap();

        // ───────────────────────────────── 완제품·불량 로트 집계
        List<ProcessLog> processLogs = monitoringService.allFindProcesses();

        Map<String, Long> totalCompleteByProduct = new HashMap<>();
        Map<String, Long> totalNgByProduct       = new HashMap<>();

        /*  로트별 공정상태 모으기
            productName → lotNumber → Set<ProcessResultStatus>
        */
        Map<String, Map<String, EnumSet<ProcessResultStatus>>> lotStatusMap = new HashMap<>();

        for (ProcessLog log : processLogs) {
            String productName = log.getOrderList().getProductName().name();
            String lotNumber   = log.getLotNumber();

            lotStatusMap
                    .computeIfAbsent(productName, k -> new HashMap<>())
                    .computeIfAbsent(lotNumber,   k -> EnumSet.noneOf(ProcessResultStatus.class))
                    .add(log.getProcessResultStatus());
        }

        // 1·2·3 공정 모두 OK → 완제품, 그 외(OK/NG 혼합 또는 NG 포함) → 불량
        for (var productEntry : lotStatusMap.entrySet()) {
            String productName = productEntry.getKey();
            long completeLots = 0;
            long ngLots       = 0;

            for (EnumSet<ProcessResultStatus> statuses : productEntry.getValue().values()) {
                if (statuses.size() == 1 && statuses.contains(ProcessResultStatus.OK)) {
                    completeLots++;
                } else {
                    ngLots++;
                }
            }
            totalCompleteByProduct.put(productName, completeLots);
            totalNgByProduct.put(productName, ngLots);
        }

        // ───────────────────────────────── 차트 데이터 계산
        List<String> labels = List.of("GyulRide", "InteliBike", "PedalAt4");

        List<Integer> targetCounts = labels.stream()
                .map(label -> todayTargetMap.getOrDefault(ProductName.valueOf(label), 0))
                .collect(Collectors.toList());

        List<Long> actualCounts = labels.stream()
                .map(label -> totalCompleteByProduct.getOrDefault(label, 0L))
                .collect(Collectors.toList());

        List<Double> achievementRates = IntStream.range(0, labels.size())
                .mapToObj(i -> {
                    int  target = targetCounts.get(i);
                    long actual = actualCounts.get(i);
                    return target == 0 ? 0.0
                            : Math.round(actual * 1000.0 / target) / 10.0;
                })
                .collect(Collectors.toList());

        // ───────────────────────────────── 모델 바인딩
        model.addAttribute("totalCompleteByProduct", totalCompleteByProduct);
        model.addAttribute("totalNgByProduct",       totalNgByProduct);
        model.addAttribute("todayTargetMap",         todayTargetMap);

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels",           labels);
        chartData.put("targetCounts",     targetCounts);
        chartData.put("actualCounts",     actualCounts);
        chartData.put("achievementRates", achievementRates);

        model.addAttribute("chartData", new ObjectMapper().writeValueAsString(chartData));
        model.addAttribute("productNames", labels);

        // ───────────────────────────────── 오늘 생산 계획(목표·실적)
        List<BikeProductionDto> dtoList = bikeProductionRepository
                .findByProductionDate(LocalDate.now())
                .stream()
                .map(p -> new BikeProductionDto(
                        p.getProductName(),
                        p.getTargetCount(),
                        p.getActualCount()))
                .collect(Collectors.toList());
        model.addAttribute("productionList", dtoList);

        return "dashboard";
    }

    @GetMapping("/notice")
    public String showNoticePage() {
        return "notice";
    }
}
