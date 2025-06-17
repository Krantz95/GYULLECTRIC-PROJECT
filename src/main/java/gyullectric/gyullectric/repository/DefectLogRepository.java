package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.DefectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DefectLogRepository extends JpaRepository<DefectLog, Long> {

    // 경고 로그를 최근 10건 내림차순으로 조회
    List<DefectLog> findTop10ByOrderByDetectedAtDesc();

    // 특정 증상 항목(symptomItem)에 대해 가장 최근에 발생한 DefectLog 한 건을 조회
    Optional<DefectLog> findTopBySymptomItemOrderByDetectedAtDesc(String symptomItem);

}

