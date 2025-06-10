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

    private static final String API_URL = "http://localhost:5000/predict"; // Flask 서버 주소
    private final RestTemplate restTemplate;

    public PredictionClient(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 날짜 3개에 대한 수요량을 기반으로 예측 요청
     * @param demandValues 예: [120, 135, 142]
     * @return 예측결과 Map (status, predicted_daily, predicted_total 등)
     */
    public Map<String, Object> callPredictionApi(List<Integer> demandValues) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("demandValues", demandValues);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("❌ 예측 API 응답 실패: 상태코드 = {}", response.getStatusCode());
            }

        } catch (HttpStatusCodeException e) {
            log.error("❌ HTTP 예측 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("❌ Flask 서버 연결 실패 - 서버가 실행 중인지 확인하세요.", e);
        } catch (Exception e) {
            log.error("❌ 예측 API 호출 중 알 수 없는 오류", e);
        }

        return Map.of("status", "error", "message", "Flask 서버 호출 실패");
    }
}
