package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.domain.ProductName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderListRepository extends JpaRepository<OrderList, Long> {

    // 공정 상태로 조회
    List<OrderList> findByProcessStatus(ProcessStatus status);

    // 최신 주문 1건 (id 기준)
    Optional<OrderList> findTopByOrderByIdDesc();

    // 특정 주문 ID 기준 최신 주문일
    Optional<OrderList> findTopByIdOrderByOrderDateDesc(Long id);

    // 특정 제품의 기간 내 주문 수량 합계
    @Query("SELECT SUM(o.quantity) FROM OrderList o WHERE o.productName = :productName AND o.orderDate >= :startDateTime AND o.orderDate < :endDateTime")
    Integer sumQuantityByProductNameAndDateBetween(
            @Param("productName") ProductName productName,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // 특정 제품의 전체 주문 수량
    @Query("SELECT SUM(o.quantity) FROM OrderList o WHERE o.productName = :productName")
    Integer sumQuantityByProductName(@Param("productName") ProductName productName);

    // ✅ 금일 전체 주문 수량 합계 (KPI용)
    @Query("SELECT SUM(o.quantity) FROM OrderList o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Integer sumQuantityByDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
