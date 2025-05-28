package gyullectric.gyullectric;

import gyullectric.gyullectric.domain.ErrorCode;
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

        Random random = new Random();
            boolean isOk1 = random.nextInt(10) + 1 <= 9;
            String errorCode1;
        if (random.nextInt(100) < 5) {  // 5% 확률 예외 발생
            isOk1 = false;
            errorCode1 = "EXCEPTION_ERROR";
        } else {
            ErrorCode[] errorCodes = ErrorCode.values();
            errorCode1 = isOk1 ? "_" : errorCodes[random.nextInt(errorCodes.length)].name();
        }
            step1.setProcessResultStatus(isOk1 ? ProcessResultStatus.OK : ProcessResultStatus.NG);
            step1.setCreateAt(LocalDateTime.now());
            step1.setErrorCode(isOk1 ? "_" : errorCode1);
            monitoringRepository.save(step1);
            sendProcessLog(step1); // WebSocket 전송

            // Step2 생성
            int step2Num = baseStep + 1;
            String lotStep2 = step1.getLotNumber().replace(
                    "_" + String.format("%02d", baseStep) + "_",
                    "_" + String.format("%02d", step2Num) + "_"
            );


            boolean isOk2 = random.nextInt(10) + 1 <= 9;
        String errorCode2;
        if (random.nextInt(100) < 6) {
            isOk2 = false;
            errorCode2 = "EXCEPTION_ERROR";
        } else {
            ErrorCode[] errorCodes2 = ErrorCode.values();
            errorCode2 = isOk2 ? "_" : errorCodes2[random.nextInt(errorCodes2.length)].name();
        }
            ProcessLog step2 = ProcessLog.builder()
                    .lotNumber(lotStep2)
                    .processStep(step2Num)
                    .orderList(step1.getOrderList())
                    .productName(step1.getProductName())
                    .processResultStatus(isOk2 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                    .createAt(LocalDateTime.now())
                    .errorCode(isOk2 ? "_" : errorCode2)
                    .build();
            monitoringRepository.save(step2);
            sendProcessLog(step2); // WebSocket 전송

            // Step3 생성
            int step3Num = baseStep + 2;
            String lotStep3 = step2.getLotNumber().replace(
                    "_" + String.format("%02d", step2Num) + "_",
                    "_" + String.format("%02d", step3Num) + "_"
            );


            boolean isOk3 = random.nextInt(10) + 1 <= 9;
        String errorCode3;
        if (random.nextInt(100) < 3) {
            isOk3 = false;
            errorCode3 = "EXCEPTION_ERROR";
        } else {
            ErrorCode[] errorCodes3 = ErrorCode.values();
            errorCode3 = isOk2 ? "_" : errorCodes3[random.nextInt(errorCodes3.length)].name();
        }
            ProcessLog step3 = ProcessLog.builder()
                    .lotNumber(lotStep3)
                    .processStep(step3Num)
                    .orderList(step1.getOrderList())
                    .productName(step1.getProductName())
                    .processResultStatus(isOk3 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                    .createAt(LocalDateTime.now())
                    .errorCode(isOk3 ? "_" : errorCode3)
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
