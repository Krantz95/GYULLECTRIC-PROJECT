package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.DefectLog_Chart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 불량예측전용 리포지터리2. 예측 점수 및 시각화용 결과 저장
@Repository
public interface DefectLogChartRepository extends JpaRepository<DefectLog_Chart, Long> {

    // 공정별 최신 불량 예측 점수 1건 조회
    // - 도넛 차트에 표시할 데이터
    // - 공정 단계별(createdAt 기준) 가장 최근 기록 1건을 반환
    // - 예: processStep = 1(주조), 2(용접)
    DefectLog_Chart findTopByProcessStepOrderByCreatedAtDesc(int processStep);
}
