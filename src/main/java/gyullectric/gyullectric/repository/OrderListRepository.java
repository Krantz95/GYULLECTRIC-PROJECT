package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.domain.ProductName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderListRepository extends JpaRepository<OrderList, Long> {

    List<OrderList> findByProcessStatus(ProcessStatus status);

//    주문리스트 내림차순
   Optional<OrderList> findTopByOrderByIdDesc();

    Optional<OrderList> findTopByIdOrderByOrderDateDesc(Long id);

    @Query("SELECT SUM(o.quantity) FROM OrderList o WHERE o.productName = :productName AND o.orderDate >= :startDateTime AND o.orderDate < :endDateTime")
    Integer sumQuantityByProductNameAndDateBetween(
            @Param("productName") ProductName productName,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    @Query("SELECT SUM(o.quantity) FROM OrderList o WHERE o.productName = :productName")
    Integer sumQuantityByProductName(@Param("productName") ProductName productName);


}
