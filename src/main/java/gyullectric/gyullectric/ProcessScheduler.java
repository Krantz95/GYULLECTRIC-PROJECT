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

            Optional<ProcessLog> processOpt = monitoringRepository
                    .findFirstByProcessResultStatusOrderByOrderList_IdAsc(ProcessResultStatus.WAITING);

            if (processOpt.isEmpty()) return;

            ProcessLog step1 = processOpt.get();
            int baseStep = step1.getProcessStep();

            // Step1 처리
            boolean isOk1 = new Random().nextInt(10) + 1 <= 9;
            step1.setProcessResultStatus(isOk1 ? ProcessResultStatus.OK : ProcessResultStatus.NG);
            step1.setCreateAt(LocalDateTime.now());
            step1.setErrorCode(isOk1 ? "_" : "10" + baseStep);
            monitoringRepository.save(step1);
            sendProcessLog(step1); // WebSocket 전송

            // Step2 생성
            int step2Num = baseStep + 1;
            String lotStep2 = step1.getLotNumber().replace(
                    "_" + String.format("%02d", baseStep) + "_",
                    "_" + String.format("%02d", step2Num) + "_"
            );

            boolean isOk2 = new Random().nextInt(10) + 1 <= 9;
            ProcessLog step2 = ProcessLog.builder()
                    .lotNumber(lotStep2)
                    .processStep(step2Num)
                    .orderList(step1.getOrderList())
                    .productName(step1.getProductName())
                    .processResultStatus(isOk2 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                    .createAt(LocalDateTime.now())
                    .errorCode(isOk2 ? "_" : "10" + step2Num)
                    .build();
            monitoringRepository.save(step2);
            sendProcessLog(step2); // WebSocket 전송

            // Step3 생성
            int step3Num = baseStep + 2;
            String lotStep3 = step2.getLotNumber().replace(
                    "_" + String.format("%02d", step2Num) + "_",
                    "_" + String.format("%02d", step3Num) + "_"
            );

            boolean isOk3 = new Random().nextInt(10) + 1 <= 9;
            ProcessLog step3 = ProcessLog.builder()
                    .lotNumber(lotStep3)
                    .processStep(step3Num)
                    .orderList(step1.getOrderList())
                    .productName(step1.getProductName())
                    .processResultStatus(isOk3 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                    .createAt(LocalDateTime.now())
                    .errorCode(isOk3 ? "_" : "10" + step3Num)
                    .build();
            monitoringRepository.save(step3);
            sendProcessLog(step3); // WebSocket 전송

            // 주문 완료 여부 확인 (step == 3인 공정이 주문 수량만큼인지 확인)
            Long orderId = step1.getOrderList().getId();
            int totalQty = step1.getOrderList().getQuantity();
            long finishedCount = monitoringRepository.countByOrderList_IdAndProcessStep(orderId, 3);

            if (finishedCount >= totalQty) {
                productService.updateOrderState(orderId, ProcessStatus.COMPLETED);
            }
        }
        private void sendProcessLog (ProcessLog process){
            MonitoringDto dto = new MonitoringDto(
                    process.getLotNumber(),
                    process.getProcessStep(),
                    process.getProcessResultStatus().name(),
                    process.getCreateAt() != null ? process.getCreateAt().toString() : null,
                    process.getErrorCode(),
                    process.getProductName().name()
            );
            simpMessagingTemplate.convertAndSend("/topic/process", dto);
            log.info("WebSocket 전송: {}", process.getLotNumber());
        }
    }
