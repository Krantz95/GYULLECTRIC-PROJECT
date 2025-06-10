package gyullectric.gyullectric.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// Flask 서버의 불량 예측 API를 호출하는 유틸 클래스
@Slf4j
@Component
public class DefectPredictionClient {

    // Flask 서버의 예측 API 주소
    private static final String API_URL = "http://localhost:5000/predict/defect";

    // HTTP 통신을 위한 RestTemplate
    private final RestTemplate restTemplate;

    // 생성자: 타임아웃 설정 포함된 RestTemplate 구성
    public DefectPredictionClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    // 예측 요청 메서드: pressure, upperTemp, lowerTemp를 전달하여 예측 결과를 받아옴
    public Map<String, Object> predictDefect(double pressure, double upperTemp, double lowerTemp) {

        // 요청 바디 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pressure", pressure);
        requestBody.put("upperTemp", upperTemp);
        requestBody.put("lowerTemp", lowerTemp);

        // JSON 요청 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 예측 API에 POST 요청 (응답 타입: Map)
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            // 응답 코드가 200번대일 경우
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body == null) {
                    log.warn("응답은 200 OK지만 body가 비어있습니다.");
                    return Map.of("status", "error", "message", "응답 body 없음");
                }
                return body;
            } else {
                // 2xx가 아닌 경우
                log.warn("예측 API 상태코드 오류: {}", response.getStatusCode());
            }

        } catch (HttpStatusCodeException e) {
            // 4xx, 5xx 등 HTTP 상태 예외 처리
            log.error("HTTP 상태 예외 발생: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            // 서버 연결 불가 (예: Flask 꺼짐)
            log.error("Flask 서버에 접근할 수 없습니다. 서버가 켜져있는지 확인하세요.", e);
        } catch (Exception e) {
            // 그 외 모든 예외
            log.error("예측 API 호출 중 알 수 없는 예외 발생", e);
        }

        // 실패 시 공통 반환값
        return Map.of("status", "error", "message", "서버 오류 또는 예측 실패");
    }
}