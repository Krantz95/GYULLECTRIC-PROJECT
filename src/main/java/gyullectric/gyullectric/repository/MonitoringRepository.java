package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.OrderStatus;
import gyullectric.gyullectric.domain.ProcessLog;
import gyullectric.gyullectric.domain.ProcessResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MonitoringRepository extends JpaRepository<ProcessLog, Long> {

    List<ProcessLog> findAllByOrderList_Id(Long id);

    Optional<ProcessLog> findFirstByProcessResultStatusOrderByOrderList_IdAsc(ProcessResultStatus status);


    @Query("SELECT p FROM ProcessLog p WHERE p.createAt BETWEEN :start AND :end ORDER BY p.processStep, p.lotNumber")
    List<ProcessLog> findAllByCreateAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end")LocalDateTime end);

    long countByOrderList_IdAndProcessStep(Long orderId, int processStep);
}
