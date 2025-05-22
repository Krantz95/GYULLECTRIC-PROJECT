package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.Inventory;
import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.repository.OrderRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public void saveInventory(Inventory inventory){
        orderRepository.save(inventory);
    }

    public List<Inventory> findAllInventory(){
        return orderRepository.findAll();
    }

    public Optional<Inventory> findOneInventory(Long id){
        return orderRepository.findById(id);
    }
    public List<Inventory> findInventoryName(PartName partName){
        return orderRepository.findAllByPartName(partName);
    }

    @Transactional
    public void deleteInventory(Long id) {
        orderRepository.deleteById(id);
    }


    public Page<Inventory> orderGetList(int page, String kw, String partName, String supplier) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("orderAt"));
        PageRequest pageable = PageRequest.of(page, 10, Sort.by(sorts));

        // 검색어, 필터 모두 비었을 경우 전체 조회
        if ((kw == null || kw.trim().isEmpty()) &&
                (partName == null || partName.trim().isEmpty()) &&
                (supplier == null || supplier.trim().isEmpty())) {
            return orderRepository.findAll(pageable);
        }

        Specification<Inventory> spec = buildSearchSpec(kw, partName, supplier);
        return orderRepository.findAll(spec, pageable);
    }

//    검색부분

    private Specification<Inventory> buildSearchSpec(String kw, String partName, String supplier) {
        return (Root<Inventory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (kw != null && !kw.trim().isEmpty()) {
                Predicate partNameLike = cb.like(root.get("partName"), "%" + kw + "%");
                Predicate supplierLike = cb.like(root.get("supplier"), "%" + kw + "%");
                predicates.add(cb.or(partNameLike, supplierLike));
            }

            if (partName != null && !partName.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("partName"), partName));
            }

            if (supplier != null && !supplier.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("supplier"), supplier));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
