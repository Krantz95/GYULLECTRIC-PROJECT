package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.Inventory;
import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findAllByPartName(PartName partName);

    List<Inventory> findAllBySupplier(Supplier supplier);

    Inventory findByPartName(PartName partName);

    // enum 타입 필드에는 Like, Containing 메서드 제거
    // List<Inventory> findByPartNameLike(PartName partName);  // 삭제 권장

    Page<Inventory> findAll(Specification<Inventory> spec, Pageable pageable);

    // enum 타입에 Containing 사용 불가 → 삭제하거나 문자열 필드로 변경 후 사용
    // Page<Inventory> findByPartNameContaining(PartName partName, Pageable pageable);  // 삭제 권장
    // Page<Inventory> findBySupplierContaining(Supplier supplier, Pageable pageable);  // 삭제 권장

    Page<Inventory> findAll(Pageable pageable);
}
