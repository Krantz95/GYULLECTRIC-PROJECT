package gyullectric.gyullectric.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PredictionClient {

    private static final String API_URL = "http://localhost:5000/predict";
    private final RestTemplate restTemplate;

    public PredictionClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Map<String, Object> callPredictionApi(String product, String startDate, String endDate) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product", product);
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("예측 API 호출 실패 - 상태코드: {}", response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            log.error("HTTP 오류 응답: {}, 내용: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("예측 API 호출 중 예외 발생", e);
        }

        return Map.of("status", "error", "message", "Flask 서버 호출 실패");
    }
}
