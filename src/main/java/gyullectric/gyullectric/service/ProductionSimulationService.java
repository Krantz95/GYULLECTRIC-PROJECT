package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.BikeProduction;
import gyullectric.gyullectric.repository.BikeProductionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProductionSimulationService {

    @Autowired
    private BikeProductionRepository bikeProductionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private Random random = new Random();

    // 더미 데이터 생성(이미 만들어놓은 initDailyTargetProduction 재사용 가능)
    @PostConstruct
    public void initDailyTargetProduction() {
        // 기존 코드 복사
    }

    // 가상 시간 시뮬레이션: 실제 생산량 누적 증가 (예: 1분마다)
    @Scheduled(fixedRate = 60000)
    public void simulateProductionProgress() {
        LocalDate today = LocalDate.now();
        List<BikeProduction> productions = bikeProductionRepository.findByProductionDate(today);

        for (BikeProduction p : productions) {
            if (p.getActualCount() < p.getTargetCount()) {
                int increment = random.nextInt(5) + 1;
                int newCount = Math.min(p.getActualCount() + increment, p.getTargetCount());
                p.setActualCount(newCount);
                bikeProductionRepository.save(p);
            }
        }
    }

    // WebSocket으로 실시간 진행률 전송 (예: 5초마다)
    @Scheduled(fixedRate = 5000)
    public void sendRealTimeProgress() {
        LocalDate today = LocalDate.now();
        List<BikeProduction> productions = bikeProductionRepository.findByProductionDate(today);

        Map<String, Object> progressData = new HashMap<>();
        List<String> products = new ArrayList<>();
        List<Integer> actualCounts = new ArrayList<>();
        List<Integer> targetCounts = new ArrayList<>();
        List<Double> achievementRates = new ArrayList<>();

        for (BikeProduction p : productions) {
            products.add(p.getProductName().name());
            actualCounts.add(p.getActualCount());
            targetCounts.add(p.getTargetCount());

            double rate = (p.getTargetCount() == 0) ? 0.0 :
                    Math.round((p.getActualCount() * 1000.0 / p.getTargetCount())) / 10.0;
            achievementRates.add(rate);
        }

        progressData.put("products", products);
        progressData.put("actualCounts", actualCounts);
        progressData.put("targetCounts", targetCounts);
        progressData.put("achievementRates", achievementRates);

        messagingTemplate.convertAndSend("/topic/progress", progressData);
    }
}


