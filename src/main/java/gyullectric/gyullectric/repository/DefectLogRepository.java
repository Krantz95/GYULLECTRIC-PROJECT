// DefectLogRepository.java
package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.DefectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 불량예측전용 리포지터리1. 원본 경고 로그 저장
@Repository
public interface DefectLogRepository extends JpaRepository<DefectLog, LocalDateTime> {

    // 전체 불량 로그를 최신 순으로 조회
    List<DefectLog> findAllByOrderByDetectedAtDesc();

    // 동일한 공정 + 증상 조합의 경고가 1분 이내에 이미 존재하는지 확인
    // - 중복 경고 방지용
    boolean existsByProcessStepAndSymptomAndDetectedAtAfter(
            String processStep,
            String symptom,
            LocalDateTime after
    );

    // 아직 점검 요청되지 않은 최신 경고 1건 조회
    // - 대시보드에 표시하거나 점검 알림 용도로 사용
    Optional<DefectLog> findTopByInspectionRequestedFalseOrderByDetectedAtDesc();
}

