package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.OrderSummaryDto;
import gyullectric.gyullectric.dto.ProcessDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 생산 모니터링 및 분석 전용 서비스
 * <p>
 * ◆ 핵심 규칙
 *   1. "완제품" = 동일 LOT 에 대해 공정 1·2·3 모두가 OK 일 때 <br/>
 *   2. LOT 규격 : <code>제품명_주문PK_순번_공정단계_YYYYMMDD</code>  (MonitoringService.processSave 참고) <br/>
 *   3. 실시간 KPI, 공정 달성률, 주문 요약 등 모든 지표가 동일 완제품 판정 로직을 공유하도록 구현
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringDataService {

    private final MonitoringService monitoringService;

    /* 📊 1) 주문(ID) 단위 공정 데이터 분석 */
    public ProcessDataDto getProcessData(Long orderId) {
        List<ProcessLog> processes = monitoringService.allFindProcess(orderId);
        return analyzeProcesses(processes);
    }

    /* 📊 2) 날짜 단위 공정 데이터 분석 */
    public ProcessDataDto getProcessDataByDate(LocalDate date) {
        List<ProcessLog> processes = monitoringService.findAllByDate(date);
        return analyzeProcesses(processes);
    }

    /* 🔍 공정 로그 리스트 분석 (공통 내부 메서드) */
    public ProcessDataDto analyzeProcesses(List<ProcessLog> processes) {

        Map<Integer, Long> countByStep = processes.stream()
                .collect(Collectors.groupingBy(ProcessLog::getProcessStep, Collectors.counting()));

        Map<Integer, Long> ngCountByStep = processes.stream()
                .filter(p -> p.getProcessResultStatus() == ProcessResultStatus.NG)
                .collect(Collectors.groupingBy(ProcessLog::getProcessStep, Collectors.counting()));

        Map<Integer, Long> okCountByStep = processes.stream()
                .filter(p -> p.getProcessResultStatus() == ProcessResultStatus.OK)
                .collect(Collectors.groupingBy(ProcessLog::getProcessStep, Collectors.counting()));

        Map<String, Set<ProcessResultStatus>> lotStatusMap = processes.stream()
                .collect(Collectors.groupingBy(
                        p -> lotKey(p.getLotNumber()),
                        Collectors.mapping(ProcessLog::getProcessResultStatus, Collectors.toSet())));

        long finishedLots = lotStatusMap.values().stream()
                .filter(statuses -> statuses.size() == 1 && statuses.contains(ProcessResultStatus.OK))
                .count();

        long totalLots = lotStatusMap.size();

        return new ProcessDataDto(
                processes,
                countByStep,
                ngCountByStep,
                okCountByStep,
                finishedLots,
                (int) totalLots);
    }

    /* 🧩 Helper – LOT 키 추출 (제품_주문PK_순번) */
    private String lotKey(String lotNumber) {
        String[] parts = lotNumber.split("_");
        if (parts.length >= 3) {
            return parts[0] + "_" + parts[1] + "_" + parts[2];
        }
        return lotNumber;
    }

    /* 📦 주문별 LOT 요약 */
    private Map<Long, Map<String, Set<ProcessResultStatus>>> groupByOrderAndLot(List<ProcessLog> processes) {
        return processes.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getOrderList().getId(),
                        Collectors.groupingBy(
                                p -> lotKey(p.getLotNumber()),
                                Collectors.mapping(ProcessLog::getProcessResultStatus, Collectors.toSet()))));
    }

    public Map<Long, OrderSummaryDto> getOrderSummaryByOrderId(List<ProcessLog> processes) {
        Map<Long, Map<String, Set<ProcessResultStatus>>> map = groupByOrderAndLot(processes);
        Map<Long, OrderSummaryDto> result = new HashMap<>();

        for (var entry : map.entrySet()) {
            int finished = 0, ng = 0;
            for (Set<ProcessResultStatus> statuses : entry.getValue().values()) {
                if (statuses.size() == 1 && statuses.contains(ProcessResultStatus.OK)) finished++;
                else ng++;
            }
            result.put(entry.getKey(), new OrderSummaryDto(entry.getValue().size(), finished, ng));
        }
        return result;
    }

    /* 📈 제품명별 달성률 계산 (대시보드용) */
    public Map<String, Object> calculateProductAchievementAndCounts(List<OrderList> orders, List<ProcessLog> logs) {
        Map<Long, OrderSummaryDto> summaryByOrder = getOrderSummaryByOrderId(logs);

        Map<String, Long> orderQtyByProduct = new HashMap<>();
        Map<String, Long> completeByProduct = new HashMap<>();
        Map<String, Long> ngByProduct = new HashMap<>();

        for (OrderList order : orders) {
            String product = order.getProductName().name();
            int qty = order.getQuantity();

            OrderSummaryDto s = summaryByOrder.getOrDefault(order.getId(), new OrderSummaryDto(0, 0, 0));

            orderQtyByProduct.merge(product, (long) qty, Long::sum);
            completeByProduct.merge(product, (long) s.getFinishedLots(), Long::sum);
            ngByProduct.merge(product, (long) s.getNgLots(), Long::sum);
        }

        List<String> labels = new ArrayList<>(orderQtyByProduct.keySet());
        List<Double> achievementRates = labels.stream()
                .map(p -> {
                    long ordered = orderQtyByProduct.get(p);
                    long done = completeByProduct.getOrDefault(p, 0L);
                    return ordered == 0 ? 0.0 : Math.round(done * 1000.0 / ordered) / 10.0;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("totalOrderQtyByProduct", orderQtyByProduct);
        result.put("totalCompleteByProduct", completeByProduct);
        result.put("totalNgByProduct", ngByProduct);
        result.put("achievementRates", achievementRates);

        return result;
    }

    /* ✅ 추가: 금일 완제품 수 (LOT 단위 기준) */
    public long countTodayCompleteLots() {
        LocalDate today = LocalDate.now();
        List<ProcessLog> logs = monitoringService.findAllByDate(today);

        Map<String, Set<ProcessResultStatus>> lotStatusMap = logs.stream()
                .collect(Collectors.groupingBy(
                        p -> lotKey(p.getLotNumber()),
                        Collectors.mapping(ProcessLog::getProcessResultStatus, Collectors.toSet())));

        return lotStatusMap.values().stream()
                .filter(statuses -> statuses.size() == 1 && statuses.contains(ProcessResultStatus.OK))
                .count();
    }
}
