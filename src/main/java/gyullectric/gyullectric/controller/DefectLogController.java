package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.DefectLog;
import gyullectric.gyullectric.dto.DefectLogDto;
import gyullectric.gyullectric.service.DefectLogService;
import gyullectric.gyullectric.Dummy_DefectLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/indicators/defect-predict")
public class DefectLogController {

    private final DefectLogService defectLogService;
    private final Dummy_DefectLog dummyDefectLog;

    // 불량 예측 페이지 진입 더미 데이터로 Flask에 예측 요청
    @GetMapping
    public String getDefectPredictPage(Model model) {
        DefectLogDto dto = dummyDefectLog.getDummySensorData2();

        // 1. Flask 예측 요청 → 점수 + 경고 메시지 수신
        Map<String, Object> result = defectLogService.getDefectResultFromFlask(dto);

        // 2. DB에 경고 로그 저장 (중복 방지 포함)
        defectLogService.saveWarningLogsFromFlaskResult(dto);

        // 3. 최근 경고 로그 조회
        List<DefectLog> defectLogs = defectLogService.getRecentLogs();

        // 4. 모델 전달값 설정
        model.addAttribute("castingScore", result.get("castingScore"));
        model.addAttribute("weldingScore", result.get("weldingScore"));
        model.addAttribute("castingWarningMsg", result.get("castingWarning"));
        model.addAttribute("weldingWarningMsg", result.get("weldingWarning"));
        model.addAttribute("defectLogs", defectLogs);
        model.addAttribute("defectLogsJson", defectLogs);

        return "productionIndex/defectLog";
    }
}
