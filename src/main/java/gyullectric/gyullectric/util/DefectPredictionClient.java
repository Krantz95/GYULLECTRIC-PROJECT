package gyullectric.gyullectric.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 불량예측 페이지 전용 (2개의 데이터분석 )
@Slf4j
@Component
public class DefectPredictionClient {

    // 통합된 Flask API 주소 (압력 + 금형온도 + 용접시퀀스 한 번에 보냄)
    private static final String API_URL_DEFECT_ALL = "http://localhost:5000/predict/defect";

    private final RestTemplate restTemplate;

    public DefectPredictionClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Flask 통합 API 호출 메서드
    public Map<String, Object> predictDefectAndWelding(double pressure, double upperTemp, double lowerTemp, List<Double> realPowerSeq) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pressure", pressure);
        requestBody.put("upperTemp", upperTemp);
        requestBody.put("lowerTemp", lowerTemp);
        requestBody.put("realPowerSeq", realPowerSeq);  // 용접 시퀀스도 같이 보냄

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL_DEFECT_ALL, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body == null) {
                    log.warn("예측: 응답 body가 비어있음");
                    return Map.of("status", "error", "message", "예측 응답 없음");
                }

                // 📝 통합된 응답 결과: castingScore, weldingScore, weldingWarning, defectLogs
                return body;

            } else {
                log.warn("예측 응답 코드 오류: {}", response.getStatusCode());
            }

        } catch (HttpStatusCodeException e) {
            log.error("예측 HTTP 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("예측 서버 접근 불가", e);
        } catch (Exception e) {
            log.error("예측 중 알 수 없는 예외", e);
        }

        return Map.of("status", "error", "message", "예측 실패");
    }
}
