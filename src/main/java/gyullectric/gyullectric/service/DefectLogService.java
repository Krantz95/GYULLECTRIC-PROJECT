package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.DefectLog;
import gyullectric.gyullectric.domain.DefectLog_Chart;
import gyullectric.gyullectric.dto.DefectLogDto;
import gyullectric.gyullectric.repository.DefectLogChartRepository;
import gyullectric.gyullectric.repository.DefectLogRepository;
import gyullectric.gyullectric.util.DefectPredictionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 불량 감지 및 예측 점수 처리 서비스
// - Flask 서버와 연동하여 실시간 예측 점수 계산
// - 점수 및 경고 메시지를 반환하거나 DB에 저장하는 역할
@Service
@RequiredArgsConstructor
public class DefectLogService {

    private final DefectLogChartRepository defectLogChartRepository;  // 도넛 차트용 점수 저장소
    private final DefectLogRepository defectLogRepository;            // 경고 로그 저장소
    private final DefectPredictionClient defectPredictionClient;      // Flask 예측 API 호출 유틸

    // 예측 점수 및 경고 메시지 반환 (Flask 연동)
    // - Thymeleaf 렌더링용 Map 구성 (castingScore, weldingScore, castingWarning 등 포함)
    public Map<String, Object> getDefectResultFromFlask(DefectLogDto dto) {
        Map<String, Object> flaskResult = defectPredictionClient.predictDefect(
                dto.getPressure(), dto.getUpperTemp(), dto.getLowerTemp()
        );

        int castingScore = (flaskResult.get("castingScore") instanceof Number)
                ? ((Number) flaskResult.get("castingScore")).intValue()
                : 0;
        int weldingScore = (flaskResult.get("weldingScore") instanceof Number)
                ? ((Number) flaskResult.get("weldingScore")).intValue()
                : 0;

        Map<String, String> warnings = getWarningMessages(castingScore, weldingScore);

        // 미점검 경고가 이미 있으면 해당 메시지 유지
        String castingWarning;
        Optional<DefectLog> latestUnresolved =
                defectLogRepository.findTopByInspectionRequestedFalseOrderByDetectedAtDesc();
        if (latestUnresolved.isPresent()) {
            castingWarning = latestUnresolved.get().getSymptom();
        } else {
            castingWarning = warnings.getOrDefault("casting", "");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("castingScore", castingScore);
        result.put("weldingScore", weldingScore);
        result.put("castingWarning", warnings.getOrDefault("casting", ""));
        result.put("weldingWarning", warnings.getOrDefault("welding", ""));

        return result;
    }

    // 공정 점수 기준에 따른 경고 메시지 구성
    // - 기준: castingScore 또는 weldingScore가 40 이상이면 경고 반환
    public Map<String, String> getWarningMessages(int castingScore, int weldingScore) {
        Map<String, String> result = new HashMap<>();
        if (castingScore >= 40) result.put("casting", "공정에서 이상 상태 감지 (압력/온도 초과)");
        if (weldingScore >= 40) result.put("welding", "공정에서 이상 상태 감지 (용접 출력 이상)");
        return result;
    }

    // 예측 점수를 차트용 데이터로 저장 (공정 단계와 설명 포함)
    @Transactional
    public void saveChartDataFromSensor(DefectLogDto dto, int processStep, String summary) {
        Map<String, Object> flaskResult = getDefectResultFromFlask(dto);
        int score = (flaskResult.get("castingScore") instanceof Number)
                ? ((Number) flaskResult.get("castingScore")).intValue()
                : 0;

        DefectLog_Chart data = DefectLog_Chart.builder()
                .processStep(processStep)
                .defectScore(score)
                .summary(summary)
                .createdAt(LocalDateTime.now())
                .build();

        defectLogChartRepository.save(data);
    }

    // 경고 로그 저장 (최근 1분 이내 동일 경고가 없을 경우만 저장)
    // - castingWarning이 존재할 경우에만 로그로 기록
    @Transactional
    public void saveWarningLogsFromFlaskResult(DefectLogDto dto) {
        Map<String, Object> flaskResult = getDefectResultFromFlask(dto);
        String castingWarning = (String) flaskResult.getOrDefault("castingWarning", "");

        if (!castingWarning.isEmpty()) {
            boolean alreadyExists = defectLogRepository.existsByProcessStepAndSymptomAndDetectedAtAfter(
                    "2공정", castingWarning, LocalDateTime.now().minusMinutes(1));

            if (!alreadyExists) {
                DefectLog log = DefectLog.builder()
                        .detectedAt(LocalDateTime.now())
                        .processStep("2공정")
                        .symptom(castingWarning)
                        .inspectionRequested(false)
                        .build();
                defectLogRepository.save(log);
            }
        }
    }
}