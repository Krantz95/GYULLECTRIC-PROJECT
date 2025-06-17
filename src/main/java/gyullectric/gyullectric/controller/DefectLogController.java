package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.dto.DefectLogDto;
import gyullectric.gyullectric.service.DefectLogService;
import gyullectric.gyullectric.Dummy_DefectLog;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/indicators/defect-predict")
public class DefectLogController {

    private final DefectLogService defectLogService;
    private final Dummy_DefectLog dummyDefectLog;

    // 페이지 진입 - 예측 실행 없이 UI
    @GetMapping
    public String getDefectPredictPage(HttpSession session, Model model) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        // 차트/로그 비우고 화면만 보여주기
        model.addAttribute("castingScore", null);
        model.addAttribute("weldingScore", null);
        model.addAttribute("castingWarningMsg", null);
        model.addAttribute("weldingWarningMsg", null);
        model.addAttribute("defectLogs", Collections.emptyList());
        model.addAttribute("defectLogsJson", Collections.emptyList());

        return "productionIndex/defectLog";
    }

    /**
     * [2️⃣ 예측 요청] 버튼 클릭 시 Flask 호출 및 DB 저장
     */
    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<?> startDefectPrediction() {
        try {
            DefectLogDto dto = dummyDefectLog.getDummySensorData2();
            Map<String, Object> result = defectLogService.getDefectResultFromFlask(dto);

            return ResponseEntity.ok(result); // ✅ 정상 응답
        } catch (Exception e) {
            log.error("❌ 예측 요청 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "예측 실패", "message", e.getMessage()));
        }
    }
}