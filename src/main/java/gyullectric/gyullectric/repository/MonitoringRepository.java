package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.MonitoringDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MonitoringRepository extends JpaRepository<ProcessLog, Long> {

    /** ✅ 생산량 카운트 메서드 */
    int countByProductNameAndCreateAtBetween(ProductName productName, LocalDateTime start, LocalDateTime end);

    List<ProcessLog> findByOrderList_Id(Long orderId);

    Optional<ProcessLog> findFirstByProcessResultStatusOrderByOrderList_IdAsc(ProcessResultStatus processResultStatus);

    List<MonitoringDto> findAllByOrderByCreateAtDescProductNameAscProcessStepAsc();

    @Query("SELECT p FROM ProcessLog p WHERE p.createAt BETWEEN :start AND :end ORDER BY p.processStep, p.lotNumber")
    List<ProcessLog> findAllByCreateAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end")LocalDateTime end);

    List<ProcessLog> findAll(Sort sort);

    List<ProcessLog> findByOrderListOrderByCreateAtAscProductNameAscProcessStepAsc(OrderList orderList);


    long countByOrderList_IdAndProcessStep(Long orderId, int processStep);


    Optional<ProcessLog> findTopByOrderByCreateAtDesc();

}
