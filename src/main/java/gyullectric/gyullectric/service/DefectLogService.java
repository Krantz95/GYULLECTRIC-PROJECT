package gyullectric.gyullectric.service;

import gyullectric.gyullectric.Dummy_DefectLog;
import gyullectric.gyullectric.domain.DefectLog;
import gyullectric.gyullectric.domain.DefectLog_Chart;
import gyullectric.gyullectric.dto.DefectLogDto;
import gyullectric.gyullectric.dto.WarningLogDto;
import gyullectric.gyullectric.repository.DefectLogChartRepository;
import gyullectric.gyullectric.repository.DefectLogRepository;
import gyullectric.gyullectric.util.DefectPredictionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefectLogService {

    private final DefectLogChartRepository defectLogChartRepository;
    private final DefectLogRepository defectLogRepository;
    private final DefectPredictionClient defectPredictionClient;
    private final Dummy_DefectLog dummyDefectLog;

    // ========== [1] 예측 요청 및 결과 처리 ==========
    public Map<String, Object> getDefectResultFromFlask(DefectLogDto dto) {
        // 시계열기반 예측 더미 시퀀스 생성(ex.20초간 진동값)
        List<Double> weldingSeq = dummyDefectLog.getDummyWeldingSequence();
        System.out.println("🔥 용접 시퀀스: " + weldingSeq);

        // 도장 + 용접 예측값 받아오기
        Map<String, Object> flaskResult = defectPredictionClient.predictDefectAndWelding(
                dto.getPressure(), dto.getUpperTemp(), dto.getLowerTemp(), weldingSeq);

        // 도장 점수는 로그 기반으로 재계산
        List<WarningLogDto> warningLogs = evaluateCastingWarnings(dto, 0); // 초기 점수는 의미 없음
        int castingScore = calculateCastingScore(warningLogs);             //  새 점수 계산!
        System.out.println("🔥 재계산된 도장 점수(castingScore): " + castingScore);

        // 차트2 용접 : 예측 출력 값(실측 단위)
        double predictedReal = ((Number) flaskResult.getOrDefault("weldingPredictedReal", 0)).doubleValue();
        int weldingScore = calculateWeldingScore(predictedReal);

        // 차트2 용접 : 퍼센트 및 평균 출력 계산
        Map<String, Object> weldingData = calculateWeldingPercentScore(weldingSeq);
        int weldingPercentScore = (int) weldingData.get("percent");
        double avg = (double) weldingData.get("average");
        System.out.println("📊 용접 퍼센트 계산 결과: " + weldingPercentScore);
        flaskResult.put("weldingScore", weldingPercentScore);

        // ❗ 퍼센트 기반으로 이모지 판단 (✔ 변경된 부분)
        String weldingLevelEmoji = getWeldingWarningLevel(weldingPercentScore);

        // 차트2 용접출력 : 경고 로그 '감지현상' 3단계로 구성
        List<WarningLogDto> weldingLogs = new ArrayList<>();

        String weldingSymptomText;
        if (weldingPercentScore >= 80) {
            weldingSymptomText = "급감 감지";          // 🔴 위험
        } else if (weldingPercentScore >= 61) {
            weldingSymptomText = "출력 변화 감지";     // 🟡 주의
        } else {
            weldingSymptomText = "정상 범위";          // 🟢 안정
        }
        weldingLogs.add(new WarningLogDto(
                "용접 출력",
                weldingSymptomText,
                (int) Math.round(avg),
                "1공정 - 용접"
        ));
        weldingLogs.get(0).setWarningLevel(weldingLevelEmoji);  // 이모지도 퍼센트 기준

        // 차트 1번 경고메시지
        String castingWarningMsg = !getWarningLevel(castingScore).equals("🟢 안정") ?
                "공정에서 이상 상태 감지 (압력/온도 초과)" : "";

        // 차트 2번 경고메시지
        String weldingWarningMsg;
        if (weldingPercentScore >= 80) {
            weldingWarningMsg = "설비 용접 출력 이상 (점검 필요)";           // 🔴 위험
        } else if (weldingPercentScore >= 61) {
            weldingWarningMsg = "설비 출력 변화 감지됨 (주의 요망)";       // 🟡 주의
        } else {
            weldingWarningMsg = ""; // 🟢 안정 → 메시지 없음
        }

        Map<String, Object> result = new HashMap<>();
        result.put("castingScore", castingScore);
        result.put("castingWarning", castingWarningMsg);
        result.put("defectLogs", warningLogs);
        result.put("weldingScore", weldingScore);
        result.put("weldingWarning", weldingWarningMsg);
        result.put("weldingLogs", weldingLogs);
        result.put("weldingPercent", weldingPercentScore);

        return result;
    }


    // ========== [2] 경고 로그 저장/업데이트 ==========
    @Transactional
    public void saveWarningLogsFromFlaskResult(DefectLogDto dto) {
        Map<String, Object> flaskResult = getDefectResultFromFlask(dto);
        int castingScore = (int) flaskResult.getOrDefault("castingScore", 0);

        // 차트1+2 : 예측값을 받아온뒤 자바 로직으로 평가 후 경고메시지 생성하여 WarningLogDto에 저장
        List<WarningLogDto> castingLogs = evaluateCastingWarnings(dto, castingScore);
        List<WarningLogDto> weldingLogs = (List<WarningLogDto>) flaskResult.get("weldingLogs");

        // 차트1+2 : 하나의 리스트에 통합해서 for문으로 일괄 처리
        List<WarningLogDto> totalLogs = new ArrayList<>();
        if (castingLogs != null) totalLogs.addAll(castingLogs);
        if (weldingLogs != null) totalLogs.addAll(weldingLogs);

        for (WarningLogDto warnLog : totalLogs) {
            String symptomItem = warnLog.getSymptomItem();     // 증상현상 컬럼별로 묶는다
            String incomingLevel = warnLog.getWarningLevel();  // 등급 ex: 주의, 위험
            int currentScore = warnLog.getScore();             // 점수 ex: 90

            // 같은 증상에 대해 이미 기록된 로그가 있는지 확인 (점검완료 제외)
            Optional<DefectLog> existingOpt = defectLogRepository
                    .findTopBySymptomItemOrderByDetectedAtDesc(symptomItem);

            // [1] 기존 로그가 있을 경우: 상태 비교 후 갱신 여부 판단
            if (existingOpt.isPresent()) {
                DefectLog existing = existingOpt.get();

                // 점검완료된 항목은 새로 쌓을 수 있으므로 → 새 로그로 저장
                if (existing.isInspectionRequested()) {
                    saveNewDefectLog(warnLog); // 별도 메서드로 빼도 가독성 Good
                    log.info("🆕 점검완료된 항목 → 새 로그 저장: {}", warnLog.getSymptom());
                    continue;
                }

                String currentLevel = existing.getWarningLevel();

                // 상태가 같거나 하락 → 무시
                if (!isLevelUp(currentLevel, incomingLevel)) {
                    log.info("⏩ 상태 하락/같음 → 무시: {}", warnLog.getSymptom());
                    continue;
                }
                // 상태가 상승일 경우 → 기존 로그 갱신
                existing.setSymptom(warnLog.getSymptom());
                existing.setWarningLevel(incomingLevel);
                existing.setScore(currentScore);
                existing.setDetectedAt(LocalDateTime.now());
                defectLogRepository.save(existing);
                log.info("🔁 기존 로그 갱신: {} → {}", currentLevel, incomingLevel);

                // 기존 로그가 없는 경우: 새 로그로 저장
            } else {
                saveNewDefectLog(warnLog);
                log.info("✅ 신규 로그 저장: {}", warnLog.getSymptom());
            }
        }
    }
    // 신규 로그 저장
    private void saveNewDefectLog(WarningLogDto warnLog) {
        defectLogRepository.save(DefectLog.builder()
                .detectedAt(LocalDateTime.now())
                .processStep(warnLog.getProcessStep())
                .symptom(warnLog.getSymptom())
                .symptomItem(warnLog.getSymptomItem())
                .score(warnLog.getScore())
                .warningLevel(warnLog.getWarningLevel())
                .inspectionRequested(false)
                .build());
    }
    // 기존 심각도보다 심할때만
    private boolean isLevelUp(String current, String incoming) {
        if (current == null || incoming == null) return false;

        List<String> levels = List.of("🟢 안정", "🟡 주의", "🔴 위험");
        int currentIndex = levels.indexOf(current);
        int incomingIndex = levels.indexOf(incoming);

        return incomingIndex > currentIndex;
    }

    // ========== [4] 차트/시각화용 저장 ==========

    @Transactional
    public void saveChartDataFromSensor(DefectLogDto dto, int processStep, String summary) {
        // 🔍 Flask에서 castingScore 값 받아오기
        int castingScore = ((Number) getDefectResultFromFlask(dto).getOrDefault("castingScore", 0)).intValue();

        // 점수 기반으로 로그 평가
        List<WarningLogDto> warningLogs = evaluateCastingWarnings(dto, castingScore);

        // 차트용 데이터 저장
        DefectLog_Chart data = DefectLog_Chart.builder()
                .processStep(processStep)
                .defectScore(castingScore)
                .summary(summary)
                .createdAt(LocalDateTime.now())
                .build();

        defectLogChartRepository.save(data);
    }

    // 최신 발생일시 기준으로 최근 10개 로그
    public List<DefectLog> getRecentLogs() {
        return defectLogRepository.findTop10ByOrderByDetectedAtDesc();
    }
    // 차트 1 점수계산방식 (
    private int calculateCastingScore(List<WarningLogDto> logs) {
        int dangerCount = 0;
        int cautionCount = 0;

        for (WarningLogDto log : logs) {
            String level = log.getWarningLevel();
            if (level.contains("위험")) dangerCount++;
            else if (level.contains("주의")) cautionCount++;
        }

        int score;
        if (dangerCount >= 1) {
            score = 80 + (dangerCount - 1) * 10 + cautionCount * 5;
        } else {
            score = cautionCount * 20;
        }

        return Math.min(score, 100);
    }


    // ======================== 차트 별 정리  ========================
    // -------------------------차트1 ------------------------------------------------
    // 차트1 : 경고 로그 저장 WarningLogDto 객체를 생성해 상태별(압력, 상단 온도, 하단 온도)
    // 차트1의 심각도 부분 (🔴위험🟡주의🟢안정)
    public List<WarningLogDto> evaluateCastingWarnings(DefectLogDto dto, int castingScore) {
        List<WarningLogDto> logs = new ArrayList<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        // [1] 센서 실측값 추출
        double pressure = dto.getPressure();          // 압착 압력 (예: kgf/㎠)
        double upperTemp = dto.getUpperTemp();        // 상단 금형 온도 (예: ℃)
        double lowerTemp = dto.getLowerTemp();        // 하향 금형 온도 (예: ℃)
        // [2] 공정 기준값 정의 (공정별 품질 기준)
        double pressureStandard = 299;
        double upperTempStandard = 94;
        double lowerTempStandard = 193;
        // [3] 각 항목별 로그 생성 (안정 포함)
        addCastingLog(logs, "압착 압력", pressure, pressureStandard, timestamp);
        addCastingLog(logs, "상단 금형 온도", upperTemp, upperTempStandard, timestamp);
        addCastingLog(logs, "하향 금형 온도", lowerTemp, lowerTempStandard, timestamp);

        return logs;
    }
    // 차트1 :  압착 압력 / 상단 온도 / 하향 온도에 대해 실측값을 기준값과 비교
    private void addCastingLog(List<WarningLogDto> logs, String symptomItem, double value, double standard, String timestamp) {
        String warningLevel = getWarningLevel(value, standard);  // 🔴🟡🟢 중 하나 반환
        // 심각도 이모지에 따라 텍스트 설명 선택
        String symptomLabel = getSymptomLabel(warningLevel);
        logs.add(WarningLogDto.builder()
                .timestamp(timestamp)
                .processStep("2공정 - 도장")
                .symptomItem(symptomItem)       // 예: 압착 압력
                .symptom(symptomLabel)          // 예: 기준 미달 / 기준 근접 / 정상 범위
                .score((int) value)             // 실측값 그대로 기록
                .warningLevel(warningLevel)     // 경고 레벨 이모지 포함 (🔴, 🟡, 🟢)
                .inspectionRequested(false)     // 기본값: 점검 미요청 상태
                .build());
    }
    // 차트1 : 기준의 85% 미만이면 "위험", 그 이상이면 "주의", 기준 이상은 "정상"
    private String getWarningLevel(double value, double standard) {
        if (value < standard * 0.85) return "🔴 위험";    // 기준의 85% 미만 → 위험
        else if (value < standard) return "🟡 주의";     // 기준 미만 → 주의
        else return "🟢 안정";                          // 기준 이상 → 안정
    }
    // 차트1 : 점수 → 이모지 경고 등급 변환용 (예: 80 → 🔴 위험)
    private String getWarningLevel(int score) {
        if (score >= 80) return "🔴 위험";
        if (score >= 60) return "🟡 주의";
        return "🟢  안정";
    }
    // 차트1 : 경고 레벨(이모지 포함 문자열)에 따라 사용자에게 보여줄 증상 설명
    private String getSymptomLabel(String level) {
        switch (level) {
            case "🔴 위험":
                return "기준 미달";      // 많이 낮음
            case "🟡 주의":
                return "기준 근접";      // 살짝 낮음
            case "🟢 안정":
                return "정상 범위";      // 기준 충족
            default:
                return "측정 오류";            // 예외 방어용
        }
    }

    // -------------------------차트2 ------------------------------------------------
    // 차트2 : 용접 공정의 '심각도' 출력
    private String getWeldingWarningLevel(int percent) {
        if (percent <= 60) return "🟢 안정";
        else if (percent <= 79) return "🟡 주의";
        else return "🔴 위험";
    }
    // 차트 2 : 용접 공정의 '심각도' 점수계산법
    private Map<String, Object> calculateWeldingPercentScore(List<Double> realSeq) {
        Map<String, Object> result = new HashMap<>();
        double avg = 0.0;

        if (realSeq != null && !realSeq.isEmpty()) {
            avg = realSeq.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
        System.out.println("📊 평균 출력값: " + avg);
        int percent;

        if (avg >= 1700) {
            // 안정 범위 내에서도 출력 정도에 따라 퍼센트 분포 (60~0%)
            double ratio = (avg - 1700) / 200.0; // 비율: 0~1 (1700~1900)
            percent = (int) Math.round((1 - Math.min(ratio, 1.0)) * 60); // 60~0 사이로
        } else if (avg >= 1300) {
            // 🟡 주의: 출력 감소, 1699~1300 → 61~79%
            double ratio = (1700 - avg) / 400.0; // 0~1
            percent = 61 + (int) Math.round(ratio * 18); // 최대 61+18 = 79
        } else {
            // 🔴 위험: 출력 급감, 1299 이하 → 80~100%
            double ratio = (1300 - avg) / 1300.0; // 0~1
            percent = 80 + (int) Math.round(Math.min(ratio * 20, 20)); // 최대 100
        }

        result.put("percent", percent);
        result.put("average", avg);
        return result;
    }

    private int calculateWeldingScore(double y) {
        return (y >= 1700) ? 0 : 80;
    }
}
