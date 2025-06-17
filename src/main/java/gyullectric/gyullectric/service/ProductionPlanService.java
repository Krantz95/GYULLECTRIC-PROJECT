package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.ProductName;
import gyullectric.gyullectric.domain.ProductionPlan;
import gyullectric.gyullectric.dto.ProductionProgressDto;
import gyullectric.gyullectric.repository.MonitoringRepository;
import gyullectric.gyullectric.repository.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {

    private final MonitoringRepository monitoringRepository;
    private final ProductionPlanRepository planRepository;

    public ProductionProgressDto getProgressFor(ProductName product) {
        ProductionPlan plan = planRepository.findByProductName(product)
                .orElseThrow(() -> new IllegalArgumentException("생산 계획 없음"));

        int produced = monitoringRepository
                .countByProductNameAndCreateAtBetween(
                        product,
                        plan.getStartDate().atStartOfDay(),
                        plan.getDueDate().atTime(23, 59)
                );

        int total = plan.getTotalTarget();
        long elapsed = ChronoUnit.DAYS.between(plan.getStartDate(), LocalDate.now()) + 1;
        long totalDays = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getDueDate()) + 1;

        double currentRate = (double) produced / elapsed;
        double requiredRate = (double) total / totalDays;
        double efficiency = currentRate / requiredRate;
        double progress = (double) produced / total;

        String status = efficiency >= 1 ? "정상 진행 중" : "지연 우려";

        return new ProductionProgressDto(product, produced, total, progress, efficiency, status);
    }

    public List<ProductionProgressDto> getAllProgresses() {
        return Arrays.stream(ProductName.values())
                .map(this::getProgressFor)
                .collect(Collectors.toList());
    }
}
