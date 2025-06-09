package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.DefectLog;
import gyullectric.gyullectric.dto.DefectLogDto;
import gyullectric.gyullectric.repository.DefectLogRepository;
import gyullectric.gyullectric.service.DefectLogService;
import gyullectric.gyullectric.Dummy_DefectLog;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


// 불량 예측 결과 + 경고 로그 관리 컨트롤러
// Flask 연동 기반 점수 예측 + 경고 메시지 출력 + 로그 DB 저장

@Controller
@RequiredArgsConstructor
@RequestMapping("/indicators")
public class DefectLogController {

    private final DefectLogService defectLogService;
    private final DefectLogRepository defectLogRepository;
    private final Dummy_DefectLog dummyDefectLog;

    // 불량 예측 페이지 출력
    // 더미 센서 데이터 기반 예측 점수 + 경고 메시지 출력
    // Flask 예측 결과로부터 경고 로그도 자동 저장
    @GetMapping("/defect-predict")
    public String getDefectPredictPage(Model model) {
        // 1. 더미 센서 데이터 생성
        DefectLogDto dto = dummyDefectLog.getDummySensorData();

        // 2. Flask 예측 점수 + 경고 메시지 호출
        Map<String, Object> result = defectLogService.getDefectResultFromFlask(dto);

        // 3. casting 공정에서 경고 있을 경우 DB에 경고 로그 저장
        defectLogService.saveWarningLogsFromFlaskResult(dto);

        // 4. 경고 로그 전체 조회 (최근순 정렬)
        List<DefectLog> defectLogs = defectLogRepository.findAllByOrderByDetectedAtDesc();

        // 5. Thymeleaf에 바인딩
        model.addAttribute("castingScore", result.get("castingScore"));
        model.addAttribute("weldingScore", result.get("weldingScore"));
        model.addAttribute("castingWarningMsg", result.get("castingWarning"));
        model.addAttribute("weldingWarningMsg", result.get("weldingWarning"));
        model.addAttribute("defectLogs", defectLogs);

        return "productionIndex/defectLog";
    }

    // 경고 로그 점검 요청 버튼 처리
    @PostMapping("/defect-predict/inspect")
    public String markAsInspected(@RequestParam("detectedAt")
                                  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime detectedAt) {
        Optional<DefectLog> logOpt = defectLogRepository.findById(detectedAt);
        logOpt.ifPresent(log -> {
            log.setInspectionRequested(true);
            defectLogRepository.save(log);
        });
        return "redirect:/indicators/defect-predict";
    }
}
