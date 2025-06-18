package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.MonitoringDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MonitoringRepository extends JpaRepository<ProcessLog, Long> {

    /* ────────────────────────────────────────
     * 기본 조회 메서드
     * ──────────────────────────────────────── */

    /** ✅ 제품별 오늘 생산량 */
    int countByProductNameAndCreateAtBetween(ProductName productName,
                                             LocalDateTime start,
                                             LocalDateTime end);

    /** ✅ 특정 주문 ID로 전체 공정 로그 조회 */
    List<ProcessLog> findByOrderList_Id(Long orderId);

    /** ✅ 특정 상태의 공정 로그 중 가장 먼저 생성된 주문 조회 */
    Optional<ProcessLog> findFirstByProcessResultStatusOrderByOrderList_IdAsc(
            ProcessResultStatus processResultStatus);

    /** ✅ 모든 공정 로그 최신순 조회 (모니터링용 DTO 변환 기반) */
    List<MonitoringDto> findAllByOrderByCreateAtDescProductNameAscProcessStepAsc();

    /** ✅ 특정 기간 내 공정 로그 전체 조회 (그래프/차트 등 시간 기반 분석용) */
    @Query("""
           SELECT p
           FROM ProcessLog p
           WHERE p.createAt BETWEEN :start AND :end
           ORDER BY p.processStep, p.lotNumber
           """)
    List<ProcessLog> findAllByCreateAtBetween(@Param("start") LocalDateTime start,
                                              @Param("end")   LocalDateTime end);

    /** ✅ 전체 공정 로그 정렬 조회 */
    List<ProcessLog> findAll(Sort sort);

    /** ✅ 특정 주문 기준 공정 로그 전체 조회 (시간순) */
    List<ProcessLog> findByOrderListOrderByCreateAtAscProductNameAscProcessStepAsc(
            OrderList orderList);

    /** ✅ 특정 주문의 특정 공정 단계 로그 수 카운트 */
    long countByOrderList_IdAndProcessStep(Long orderId, int processStep);

    /** ✅ 가장 최근에 등록된 공정 로그 조회 */
    Optional<ProcessLog> findTopByOrderByCreateAtDesc();

    /** ✅ 최근 1시간 공정 로그 조회 */
    @Query("""
           SELECT p
           FROM ProcessLog p
           WHERE p.createAt >= :since
           """)
    List<ProcessLog> findLastHour(@Param("since") LocalDateTime since);

    default List<ProcessLog> findLastHour() {
        return findLastHour(LocalDateTime.now().minusHours(1));
    }

    /* ────────────────────────────────────────
     * 통계성 집계 쿼리
     * ──────────────────────────────────────── */

    /** ✅ 금일 PASS(1·2·3공정 모두 OK) 제품 수
     *
     *  - lot_number(또는 product별 식별키) 단위로 그룹핑
     *  - 세 공정 로그가 모두 존재하고, 단 하나의 NG도 없을 때 PASS
     *  - H2 & MySQL 공용 표준 SQL (CASE WHEN …) 사용
     */
    @Query(value = """
            SELECT COUNT(*)                         -- PASS 로트 개수
            FROM (
                SELECT lot_number
                FROM process_log
                WHERE create_at BETWEEN :start AND :end
                GROUP BY lot_number
                HAVING COUNT(DISTINCT process_step) = 3               -- 1·2·3 공정 로그 모두 존재
                   AND SUM(CASE WHEN process_result_status <> 'OK' THEN 1 ELSE 0 END) = 0
            ) AS pass_lots
            """,
            nativeQuery = true)
    Long countTodayPassProduct(@Param("start") LocalDateTime start,
                               @Param("end")   LocalDateTime end);

    /** ✅ 공정별 평균 처리시간 (금일 완료 데이터, 초 단위)
     *
     *  - H2·MySQL 모두 지원하는 `TIMESTAMPDIFF(SECOND, …)` 사용
     *  - 두 DB 모두 ‘SECOND’ 단위를 동일 구문으로 인식 (H2는 MySQL 모드 필요)
     */
    @Query(value = """
            SELECT process_step,
                   AVG(TIMESTAMPDIFF(SECOND, start_at, end_at)) AS avg_sec
            FROM process_log
            WHERE start_at IS NOT NULL
              AND end_at   IS NOT NULL
              AND create_at BETWEEN :start AND :end
            GROUP BY process_step
            """,
            nativeQuery = true)
    List<Object[]> findAvgProcessTimeByStepTodayNative(@Param("start") LocalDateTime start,
                                                       @Param("end")   LocalDateTime end);

    /** ✅ 공정별 에러 발생 수 (금일 기준) */
    @Query("""
           SELECT p.processStep, COUNT(p)
           FROM ProcessLog p
           WHERE p.processResultStatus = 'NG'
             AND p.createAt BETWEEN :start AND :end
           GROUP BY p.processStep
           """)
    List<Object[]> countErrorsByProcessStepToday(@Param("start") LocalDateTime start,
                                                 @Param("end")   LocalDateTime end);

    /** ✅ 공정별 전체 로그 수 (금일 기준) */
    @Query("""
           SELECT p.processStep, COUNT(p)
           FROM ProcessLog p
           WHERE p.createAt BETWEEN :start AND :end
           GROUP BY p.processStep
           """)
    List<Object[]> countTotalLogsByProcessStepToday(@Param("start") LocalDateTime start,
                                                    @Param("end")   LocalDateTime end);

    /** ✅ 금일 첫 공정 로그 시각 (JPQL, H2 대응) */
    @Query("""
           SELECT MIN(p.createAt)
           FROM ProcessLog p
           WHERE p.createAt BETWEEN :start AND :end
           """)
    Optional<LocalDateTime> findFirstLogTimeOfToday(@Param("start") LocalDateTime start,
                                                    @Param("end")   LocalDateTime end);

    default Optional<LocalDateTime> findFirstLogTimeOfToday() {
        LocalDate today = LocalDate.now();
        return findFirstLogTimeOfToday(today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
    }
}
