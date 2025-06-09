package gyullectric.gyullectric.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class PredictionClient {

    private static final String API_URL = "http://localhost:5000/predict";
    private final RestTemplate restTemplate;

    public PredictionClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, Object> callPredictionApi(String startDate, String endDate) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.postForEntity(API_URL, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();

                log.info("📨 예측 API 응답 바디: {}", body);

                if (body == null) {
                    log.warn("⚠️ 예측 API 응답은 200이지만 body가 null입니다.");
                    return Map.of("status", "error", "message", "예측 응답 내용이 없습니다.");
                }

                if (!"success".equals(body.get("status"))) {
                    log.warn("⚠️ 예측 API status가 success가 아님 → {}", body.get("status"));
                    return Map.of("status", "error", "message", "예측 실패: status != success");
                }

                Object dataObj = body.get("data");
                if (!(dataObj instanceof List)) {
                    log.error("❌ 예측 API 응답 형식 오류: 'data'는 List가 아닙니다 → {}", dataObj);
                    return Map.of("status", "error", "message", "예측 데이터 형식 오류");
                }

                return body;
            } else {
                log.warn("⚠️ 예측 API 응답 실패 - 상태코드: {}", response.getStatusCode());
            }

        } catch (HttpStatusCodeException e) {
            log.error("❌ HTTP 오류 응답 - 상태: {}, 응답 내용: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("❌ Flask 서버에 접근할 수 없습니다. 서버가 실행 중인지 확인하세요.", e);
        } catch (Exception e) {
            log.error("❌ 예측 API 호출 중 알 수 없는 예외 발생", e);
        }

        return Map.of("status", "error", "message", "Flask 서버 호출 실패");
    }
}
