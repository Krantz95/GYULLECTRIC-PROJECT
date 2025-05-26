package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderListRepository extends JpaRepository<OrderList, Long> {

    List<OrderList> findByProcessStatus(ProcessStatus status);

//    주문리스트 내림차순
   Optional<OrderList> findTopByOrderByIdDesc();

    Optional<OrderList> findTopByIdOrderByOrderDateDesc(Long id);



}
