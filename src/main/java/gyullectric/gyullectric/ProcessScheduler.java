package gyullectric.gyullectric;

import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProcessResultStatus;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.dto.MonitoringDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import gyullectric.gyullectric.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessScheduler {
    private final MonitoringRepository monitoringRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ProductService productService;

    @Scheduled(fixedRate = 5000)
    public void processOneByOne() {
        log.info("스케쥴러가 호출되나?");

        Optional<ProcessLog> orderByIdAsc = monitoringRepository.findFirstByProcessResultStatusOrderByOrderList_IdAsc(ProcessResultStatus.WAITING);

        if (orderByIdAsc.isEmpty()) return;

        ProcessLog current = orderByIdAsc.get();

        log.info("가져온 공정step1{},{}", current.getProcessStep(), current.getOrderList().getId());

//        상태 업데이트
        boolean isOk = new Random().nextInt(10) + 1 <= 9;
        current.setProcessResultStatus(isOk ? ProcessResultStatus.OK : ProcessResultStatus.NG);
        current.setCreateAt(LocalDateTime.now());
        current.setErrorCode(isOk ? "_" : "10" + current.getProcessStep());
        monitoringRepository.save(current);

//        stemp2 생성
        boolean isOk2 = new Random().nextInt(10) + 1 <= 9;
        log.info("가져온 공정 step2 {}, isOk2 {}", current.getProcessStep() + 1, isOk2);
        ProcessLog step2 = ProcessLog.builder()
                .lotNumber(current.getLotNumber().replace(
                        "_" + String.format("%02d", current.getProcessStep()) + "_",
                        "-" + String.format("%02d", current.getProcessStep() + 1) + "_"))
                .processStep(current.getProcessStep() + 1)
                .processResultStatus(isOk2 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                .createAt(LocalDateTime.now())
                .errorCode(isOk2 ? "_" : "10" + (current.getProcessStep() + 1))
                .orderList(current.getOrderList())
                .build();
        monitoringRepository.save(step2);

        //        step3 생성 공정이 많다면 걍 for 문 돌리요
        boolean isOk3 = new Random().nextInt(10) + 1 <= 9;
        log.info("가져온 공정 step3 {}, isOk3 {}", current.getProcessStep() + 2, isOk3);
        ProcessLog step3 = ProcessLog.builder()
                .lotNumber(current.getLotNumber().replace(
                        "_" + String.format("%02d", current.getProcessStep()) + "_",
                        "-" + String.format("%02d", current.getProcessStep() + 2) + "_"))
                .processStep(current.getProcessStep() + 2)
                .processResultStatus(isOk3 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                .createAt(LocalDateTime.now())
                .errorCode(isOk3 ? "_" : "10" + (current.getProcessStep() + 2))
                .orderList(current.getOrderList())
                .build();
        monitoringRepository.save(step3);

//        jobOrderService.updateOrderState(current.getJobOrder().getId(), JobOrderState.COMPLETED);

//        모든 제품의 공정이 끝났는지 확인
        Long orderId = current.getOrderList().getId();
        int totalQuantity = current.getOrderList().getQuantity();

//        step ==3 인 공정수 조회 (ok+ng 포함)
        long finishedCount = monitoringRepository.countByOrderList_IdAndProcessStep(orderId, 3);
        if (finishedCount >= totalQuantity) {
            productService.updateOrderState(orderId, ProcessStatus.COMPLETED);
        }
        //        current 전송
        MonitoringDto dto = new MonitoringDto(
                current.getLotNumber(),
                current.getProcessStep(),
                current.getProcessResultStatus(),
                current.getCreateAt() != null? current.getCreateAt().toString() : null,
                current.getErrorCode(),
                current.getOrderList().getProductName()
        );
        log.info("Sending to WebSocket:{}", current.getLotNumber());

//        전송
        simpMessagingTemplate.convertAndSend("/topic/process", dto);

//        step2 전송
        MonitoringDto dto2 = new MonitoringDto(
                step2.getLotNumber(),
                step2.getProcessStep(),
                step2.getProcessResultStatus(),
                step2.getCreateAt() != null? current.getCreateAt().toString() : null,
                step2.getErrorCode(),
                step2.getOrderList().getProductName()
        );
        log.info("Sending to WebSocket:{}", current.getLotNumber());

//        전송
        simpMessagingTemplate.convertAndSend("/topic/process", dto2);

        //        step3 전송
        MonitoringDto dto3 = new MonitoringDto(
                step3.getLotNumber(),
                step3.getProcessStep(),
                step3.getProcessResultStatus(),
                step3.getCreateAt() != null? current.getCreateAt().toString() : null,
                step3.getErrorCode(),
                step3.getOrderList().getProductName()
        );
        log.info("Sending to WebSocket:{}", current.getLotNumber());

//        전송
        simpMessagingTemplate.convertAndSend("/topic/process", dto3);
    }

}
