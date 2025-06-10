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
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
//
        Random random = new Random();


        // ===== Step 1: 프레임 용접 (출력 체크) =====
        double frameOutput = 198 + (24 * random.nextDouble()); // 198~222V
        boolean isOk1 = frameOutput >= 200 && frameOutput <= 220 ;
        String errorCode1 = "_";
        String errorMessage1 = "정상";

        if (!isOk1) {
            double errorProb = 0.5; // 50%
            if (random.nextDouble() < errorProb) {
                if (frameOutput < 200) {
                    errorCode1 = "ERROR_102";
                } else if (frameOutput > 220) {
                    errorCode1 = "ERROR_101";
                }
                errorMessage1 = getErrorDescription(errorCode1, frameOutput);
            } else {
                // 90% 확률로 정상 처리
                isOk1 = true;
                errorCode1 = "_";
                errorMessage1 = "정상";
            }
        }


        step1.setProcessResultStatus(isOk1 ? ProcessResultStatus.OK : ProcessResultStatus.NG);
        step1.setCreateAt(LocalDateTime.now());
        step1.setErrorCode(errorCode1);
        step1.setErrorValue(!isOk1 ? frameOutput : null);
        monitoringRepository.save(step1);
        sendProcessLog(step1, errorMessage1);



        // Step 2: 도장 온도 체크
        int step2Num = baseStep + 1;
        String lotStep2 = step1.getLotNumber().replace(
                "_" + String.format("%02d", baseStep) + "_",
                "_" + String.format("%02d", step2Num) + "_"
        );

        double castPressure = 280 + random.nextDouble() * 30; // 280~310
        double upperMoldTemp = 85 + random.nextDouble() * 20; // 85~105
        double lowerMoldTemp = 180 + random.nextDouble() * 20; // 180~200

        boolean isOk2 = castPressure < 299 && upperMoldTemp < 94 && lowerMoldTemp < 193 &&
                castPressure >= 280 && upperMoldTemp >= 85 && lowerMoldTemp >= 180;
        String errorCode2 = "_";
        String errorMessage2 = "정상";

        if (!isOk2) {
            double errorProb = 0.2;  // 20%
            if (random.nextDouble() < errorProb) {
                if (castPressure >= 299 || upperMoldTemp >= 94 || lowerMoldTemp >= 193) {
                    errorCode2 = "ERROR_201";
                    errorMessage2 = getErrorDescription(errorCode2, Math.max(castPressure, Math.max(upperMoldTemp, lowerMoldTemp)));
                } else if (castPressure < 280 || upperMoldTemp < 85 || lowerMoldTemp < 180) {
                    errorCode2 = "ERROR_202";
                    errorMessage2 = getErrorDescription(errorCode2, Math.min(castPressure, Math.min(upperMoldTemp, lowerMoldTemp)));
                }
            } else {
                // 80% 확률로 정상 처리
                isOk2 = true;
                errorCode2 = "_";
                errorMessage2 = "정상";
            }
        }

        ProcessLog step2 = ProcessLog.builder()
                .lotNumber(lotStep2)
                .processStep(step2Num)
                .orderList(step1.getOrderList())
                .productName(step1.getProductName())
                .processResultStatus(isOk2 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                .createAt(LocalDateTime.now())
                .errorCode(errorCode2)
                .errorValue(!isOk2 ? (
                        errorCode2.equals("ERROR_201")
                                ? Math.max(castPressure, Math.max(upperMoldTemp, lowerMoldTemp))
                                : Math.min(castPressure, Math.min(upperMoldTemp, lowerMoldTemp))
                ) : null)
                .build();
        monitoringRepository.save(step2);
        sendProcessLog(step2, errorMessage2);

        // Step3 : 검수 생성
        int step3Num = baseStep + 2;
        String lotStep3 = step2.getLotNumber().replace(
                "_" + String.format("%02d", step2Num) + "_",
                "_" + String.format("%02d", step3Num) + "_"
        );


        boolean isOk3 = random.nextDouble() >= 0.2;
        String errorCode3 = "_";
        String errorMessage3 = "정상";
        if (!isOk3) {
            // 에러 발생 시 4% 확률로 예외 에러, 나머지는 다른 에러 중 랜덤 선택
            double exceptionErrorProb = 0.4;  // 4% 확률

            if (random.nextDouble() < exceptionErrorProb) {
                errorCode3 = "EXCEPTION_ERROR";
            } else {
                // 나머지 에러 중 랜덤 선택 (3개)
                String[] possibleErrors = {"ERROR_103", "ERROR_110", "ERROR_111"};
                errorCode3 = possibleErrors[random.nextInt(possibleErrors.length)];
            }
            errorMessage3 = getErrorDescription(errorCode3, null);
        }
        ProcessLog step3 = ProcessLog.builder()
                .lotNumber(lotStep3)
                .processStep(step3Num)
                .orderList(step1.getOrderList())
                .productName(step1.getProductName())
                .processResultStatus(isOk3 ? ProcessResultStatus.OK : ProcessResultStatus.NG)
                .createAt(LocalDateTime.now())
                .errorCode(isOk3 ? "_" : errorCode3)
                .errorValue(null)
                .build();
        monitoringRepository.save(step3);
        sendProcessLog(step3, errorMessage3); // WebSocket 전송

        // 주문 완료 여부 확인 (step == 3인 공정이 주문 수량만큼인지 확인)
        Long orderId = step1.getOrderList().getId();
        int totalQty = step1.getOrderList().getQuantity();
        long finishedCount = monitoringRepository.countByOrderList_IdAndProcessStep(orderId, 3);

        if (finishedCount >= totalQty) {
            productService.updateOrderState(orderId, ProcessStatus.COMPLETED);
        }
    }
    private void sendProcessLog (ProcessLog process, String errorMessage) {


        MonitoringDto dto = new MonitoringDto(
                process.getLotNumber(),
                process.getProcessStep(),
                process.getProcessResultStatus().name(),
                process.getCreateAt() != null ? process.getCreateAt().toString() : null,
                process.getErrorCode(),
                process.getProductName().name(),
                errorMessage,
                process.getErrorValue()
        );
        simpMessagingTemplate.convertAndSend("/topic/process", dto);
        log.info("WebSocket 전송: {} - {}", process.getLotNumber(), errorMessage);

    }

    public String getErrorDescription(String errorCode, Double value) {
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
            default:
                return "EXCEPTION_ERROR";

        }
    }



}
