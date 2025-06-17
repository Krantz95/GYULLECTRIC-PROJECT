package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.Inventory;
import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.domain.Supplier;
import gyullectric.gyullectric.dto.InventoryStockSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findAllByPartName(PartName partName);

    List<Inventory> findAllBySupplier(Supplier supplier);

    Inventory findByPartName(PartName partName);

    List<Inventory> findByPartNameLike(PartName partName);

    Page<Inventory> findAll(Specification<Inventory> spec, Pageable pageable);

    Page<Inventory> findByPartNameContaining(PartName partName, Pageable pageable);

    Page<Inventory> findBySupplierContaining(Supplier supplier, Pageable pageable);

    /** ✅ 부품명별 재고 수량 합계 조회 */
    @Query("SELECT i.partName AS partName, SUM(i.quantity) AS totalQuantity FROM Inventory i GROUP BY i.partName")
    List<InventoryStockSummary> getTotalStockSummary();

    boolean existsByPartNameAndSupplier(PartName partName, Supplier supplier);

}
