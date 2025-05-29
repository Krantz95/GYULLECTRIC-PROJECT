package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.ErrorReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorRepository extends JpaRepository<ErrorReport, Long> {
}
