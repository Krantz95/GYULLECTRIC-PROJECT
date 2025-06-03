package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.ProductName;
import gyullectric.gyullectric.dto.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
    Optional<ProductionPlan> findByProductName(ProductName productName);
}
