package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.MonitoringDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MonitoringService {
    private final MonitoringRepository monitoringRepository;
    private final ProductService productService;

    public void processSave(OrderList orderList) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        String currentDate = LocalDateTime.now().format(dateTimeFormatter);

        int orderQuantity = orderList.getQuantity();

        ProductName productName = orderList.getProductName();

        Long orderId = orderList.getId();

        for (int i = 1; i <= orderQuantity; i++) {
            String sequence = String.format("%03d", i);
            String processStep = String.format("%02d", 1);

            String lotNumber = productName.name() + "_" + orderId + "_" + sequence + "_" + processStep + "_" + currentDate;

            ProcessLog processLog = ProcessLog.builder()
                    .lotNumber(lotNumber)
                    .createAt(LocalDateTime.now())
                    .processResultStatus(ProcessResultStatus.WAITING)
                    .errorCode("-")
                    .orderList(orderList)
                    .processStep(1)
                    .productName(productName)
                    .build();

            monitoringRepository.save(processLog);
        }
    }

    public boolean randomStatus() {
        return new Random().nextInt(10) + 1 <= 9;
    }

    public List<ProcessLog> allFindProcess(Long id) {
        return monitoringRepository.findAll();
    }

    public List<ProcessLog> allFindProcesses() {
        return monitoringRepository.findAll();
    }

    public Optional<ProcessLog> oneFindProcess(Long id) {
        return monitoringRepository.findById(id);
    }

    public void deleteProcess(Long id) {
        monitoringRepository.deleteById(id);
    }

    //    날짜를 기준으로 주문조회
    public List<ProcessLog> findAllByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return monitoringRepository.findAllByCreateAtBetween(start, end);
    }

    // 저장 후 정렬된 리스트 반환 메서드
    public List<ProcessLog> getSortedProcessLogsByOrderList(OrderList orderList) {
        return monitoringRepository.findByOrderListOrderByCreateAtAscProductNameAscProcessStepAsc(orderList);
    }

    public long countCompleteProductsByOrderId(Long orderId) {
        List<ProcessLog> logs = monitoringRepository.findByOrderList_Id(orderId);

        Map<String, List<ProcessLog>> groupedByLot = logs.stream()
                .collect(Collectors.groupingBy(ProcessLog::getLotNumber));

        long completeCount = 0;
        Set<Integer> requiredSteps = Set.of(1, 2, 3);

        for (List<ProcessLog> lotLogs : groupedByLot.values()) {
            Map<Integer, List<ProcessLog>> stepMap = lotLogs.stream()
                    .collect(Collectors.groupingBy(ProcessLog::getProcessStep));

            boolean hasAllSteps = requiredSteps.stream()
                    .allMatch(step -> stepMap.containsKey(step) && stepMap.get(step).size() == 1);

            if (!hasAllSteps) continue;

            boolean allOk = requiredSteps.stream()
                    .allMatch(step -> stepMap.get(step).get(0).getProcessResultStatus() == ProcessResultStatus.OK);

            if (allOk) {
                completeCount++;
            }
        }

        return completeCount;
    }


    public long countNgProductsByOrderId(Long orderId) {
        List<ProcessLog> logs = monitoringRepository.findByOrderList_Id(orderId);

        Map<String, List<ProcessLog>> groupedByLot = logs.stream()
                .collect(Collectors.groupingBy(ProcessLog::getLotNumber));

        long ngCount = 0;
        Set<Integer> requiredSteps = Set.of(1, 2, 3);

        for (List<ProcessLog> lotLogs : groupedByLot.values()) {
            Map<Integer, List<ProcessLog>> stepMap = lotLogs.stream()
                    .collect(Collectors.groupingBy(ProcessLog::getProcessStep));

            boolean hasAllSteps = requiredSteps.stream()
                    .allMatch(step -> stepMap.containsKey(step) && stepMap.get(step).size() == 1);

            if (!hasAllSteps) {
                // 공정이 누락되었거나 여러 개 있는 경우, NG로 처리하지 않음 (제외)
                continue;
            }

            boolean allOk = requiredSteps.stream()
                    .allMatch(step -> stepMap.get(step).get(0).getProcessResultStatus() == ProcessResultStatus.OK);

            if (!allOk) {
                ngCount++;
            }
        }

        return ngCount;
    }

    public Optional<ProcessLog> getLatestProcessLog() {
        return monitoringRepository.findTopByOrderByCreateAtDesc(); // JPA 예시
    }
}


