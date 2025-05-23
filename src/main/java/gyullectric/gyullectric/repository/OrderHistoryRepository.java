package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.OrderHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long>
{
    List<OrderHistory> findAll(Sort sort);
}
