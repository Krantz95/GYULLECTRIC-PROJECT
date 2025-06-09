package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.BikeProduction;
import gyullectric.gyullectric.domain.ProductName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BikeProductionRepository extends JpaRepository<BikeProduction, Long> {

    Optional<BikeProduction> findByProductionDateAndProductName(LocalDate date, ProductName productName);
    List<BikeProduction> findByProductionDate(LocalDate date);
    boolean existsByProductionDate(LocalDate date);

    // 주문량 합계 구하는 커스텀 쿼리 (OrderList 테이블로부터 합계 구해야 한다면 별도 쿼리 필요)
    @Query("SELECT SUM(o.quantity) FROM OrderList o WHERE o.productName = :productName")
    Integer sumOrderedQuantityByProductName(ProductName productName);


}
