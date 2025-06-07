package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.ErrorCode;
import gyullectric.gyullectric.domain.ErrorReport;
import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ErrorRepository extends JpaRepository<ErrorReport, Long> {
    @Query("SELECT e FROM ErrorReport e JOIN e.members m WHERE m.name LIKE %:name%")
    Page<ErrorReport> findByMemberName(@Param("name") String name, Pageable pageable);
    Page<ErrorReport> findAll(Specification<ErrorReport> spec, Pageable pageable);

    Page<ErrorReport> findByErrorCode(ErrorCode errorCode, Pageable pageable);

}
