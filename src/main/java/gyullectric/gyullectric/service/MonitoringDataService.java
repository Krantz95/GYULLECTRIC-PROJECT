package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProcessResultStatus;
import gyullectric.gyullectric.dto.OrderSummaryDto;
import gyullectric.gyullectric.dto.ProcessDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringDataService {

    private final MonitoringService monitoringService;

    //오더기준
    public ProcessDataDto getProcessData(Long orderId) {
        List<ProcessLog> processes = monitoringService.allFindProcess(orderId);
        return analyzeProcesses(processes);
    }

    //    날짜기준
    public ProcessDataDto getProcessDataByDate(LocalDate date) {
        List<ProcessLog> processes = monitoringService.findAllByDate(date);
        return analyzeProcesses(processes);
    }

    public ProcessDataDto analyzeProcesses(List<ProcessLog> processes) {
        // 차트를 위한 추가
//        공정별 통계. Map<Integer, Long>형태로 저장됨
        Map<Integer, Long> countByStep = processes.stream()
                .collect(Collectors.groupingBy(ProcessLog::getProcessStep, Collectors.counting()));
        log.info("공정별 개수 :{}", countByStep);

//        공정별 에러
        Map<Integer, Long> ngCountByStep = processes.stream()
                .filter(p -> ProcessResultStatus.NG.equals(p.getProcessResultStatus()))
                .collect(Collectors.groupingBy(ProcessLog::getProcessStep, Collectors.counting()));
        log.info("공정별 에러개수 : {}", ngCountByStep);

//        공정별 OK
        Map<Integer, Long> okCountByStep = processes.stream()
                .filter(p -> ProcessResultStatus.OK.equals(p.getProcessResultStatus()))
                .collect(Collectors.groupingBy(ProcessLog::getProcessStep, Collectors.counting()));
        log.info("공정별 오케이개수 : {}", okCountByStep);
//lot넘버별로 모든 공정상태를 Set<Stream>으로 저장
        Map<String, Set<ProcessResultStatus>> lotSatusMap =
                processes.stream()
                        .collect(Collectors.groupingBy(p -> lotNumberGroup(p.getLotNumber()),
                                Collectors.mapping(ProcessLog::getProcessResultStatus, Collectors.toSet())));
        log.info("lot별 :{}", lotSatusMap);
// lot 아이디 : 제품이름_오더아이디_순서_공정순서_생성일
//        로트번호에서 공통부분만 추출하는 함수,

//        모든 상태가 ok인 lot만 선택하여 개수계산
//        Set<String>의 크기가 1이고 그값이 ok라면 해당 lot는 완제품
        long productCount = lotSatusMap.values().stream()
                .filter(statuses -> statuses.size() == 1 && statuses.contains(ProcessResultStatus.OK)).count();
        log.info("lot별 모두 ok인 완제품 개수 :{}", productCount);

//        공정 전체개수, 주문수
        int totalCount = (processes.size() / 3);
        log.info("주문수 :{}", totalCount);

        return new ProcessDataDto(processes, countByStep, ngCountByStep, okCountByStep, productCount, totalCount);
    }

    private String lotNumberGroup(String lotNumber) {
        String[] parts = lotNumber.split("_");
        if (parts.length >= 3) {
            return parts[0] + "_" + parts[1] + "_" + parts[2];
        }
        return lotNumber;
    }

    // lotNumber에서 제품명 추출 (예: "A_123_01_1_20230528" -> "A")
    public String extractProductNameFromLot(String lotNumber) {
        String[] parts = lotNumber.split("_");
        return parts.length > 0 ? parts[0] : "UNKNOWN";
    }

    private Map<Long, Map<String, Set<ProcessResultStatus>>> groupStatusesByOrderAndLot(List<ProcessLog> processes) {
        // 주문번호(Long) -> 로트번호(String) -> 공정상태 Set<ProcessResultStatus>
        return processes.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getOrderList().getId(),       // 주문번호로 그룹핑
                        Collectors.groupingBy(
                                p -> lotNumberGroup(p.getLotNumber()), // 로트번호 기준 그룹핑
                                Collectors.mapping(ProcessLog::getProcessResultStatus, Collectors.toSet())
                        )
                ));
    }
    public Map<Long, OrderSummaryDto> getOrderSummaryByOrderId(List<ProcessLog> processes) {
        Map<Long, Map<String, Set<ProcessResultStatus>>> orderLotStatusMap = groupStatusesByOrderAndLot(processes);

        Map<Long, OrderSummaryDto> result = new HashMap<>();

        for (Map.Entry<Long, Map<String, Set<ProcessResultStatus>>> orderEntry : orderLotStatusMap.entrySet()) {
            Long orderId = orderEntry.getKey();
            Map<String, Set<ProcessResultStatus>> lotStatusMap = orderEntry.getValue();

            int totalLots = lotStatusMap.size();
            int finishedLots = 0;
            int ngLots = 0;

            for (Set<ProcessResultStatus> statuses : lotStatusMap.values()) {
                if (statuses.size() == 1 && statuses.contains(ProcessResultStatus.OK)) {
                    finishedLots++;
                } else {
                    ngLots++;
                }
            }

            result.put(orderId, new OrderSummaryDto(totalLots, finishedLots, ngLots));
        }

        return result;
    }

}