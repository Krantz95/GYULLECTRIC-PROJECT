package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.OrderList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderListRepository extends JpaRepository<OrderList, Long> {

    @Query("SELECT o FROM OrderList o JOIN FETCH o.notiAuthor")
    List<OrderList> findAllWithAuthor();

    List<OrderList> findAllByOrderByOrderDateDesc();
}
