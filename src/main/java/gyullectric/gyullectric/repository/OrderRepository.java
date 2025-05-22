package gyullectric.gyullectric.repository;

import gyullectric.gyullectric.domain.Inventory;
import gyullectric.gyullectric.domain.Members;
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



    //    제목에 문자열 포함
    List<Inventory> findByPartNameLike(PartName partName);

    //    페이징구현
    Page<Inventory> findAll(Specification<Inventory> spec, Pageable pageable);

    Page<Inventory> findByPartNameContaining(PartName partName, Pageable pageable);
    Page<Inventory> findBySupplierContaining(Supplier supplier, Pageable pageable);
}
