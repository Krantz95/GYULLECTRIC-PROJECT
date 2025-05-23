package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.repository.OrderListRepository;
import gyullectric.gyullectric.repository.InventoryRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {
    private final OrderListRepository orderListRepository;    // 주문 리포지터리
    private final InventoryRepository inventoryRepository;    // 재고 리포지터리

    // ======== Inventory 관련 기능 ========

    @Transactional
    public void saveInventory(Inventory inventory) {
        inventoryRepository.save(inventory);
    }

    public List<Inventory> findAllInventory() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> findOneInventory(Long id) {
        return inventoryRepository.findById(id);
    }

    public List<Inventory> findInventoryName(PartName partName) {
        return inventoryRepository.findAllByPartName(partName);
    }

    @Transactional
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    public Page<Inventory> orderGetList(int page, String kw, String partName, String supplier) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("orderAt")));
        if ((kw == null || kw.isBlank()) &&
                (partName == null || partName.isBlank()) &&
                (supplier == null || supplier.isBlank())) {
            return inventoryRepository.findAll(pageable);
        }
        return inventoryRepository.findAll(buildSearchSpec(kw, partName, supplier), pageable);
    }

    private Specification<Inventory> buildSearchSpec(String kw, String partName, String supplier) {
        return (Root<Inventory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (kw != null && !kw.isBlank()) {
                predicates.add(cb.or(
                        cb.like(root.get("partName").as(String.class), "%" + kw + "%"),
                        cb.like(root.get("supplier").as(String.class), "%" + kw + "%")
                ));
            }
            if (partName != null && !partName.isBlank()) {
                predicates.add(cb.equal(root.get("partName"), partName));
            }
            if (supplier != null && !supplier.isBlank()) {
                predicates.add(cb.equal(root.get("supplier"), supplier));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ======== OrderList 관련 기능 ========

    public List<OrderList> getAllOrdersOrderByOrderDateDesc() {
        return orderListRepository.findAllByOrderByOrderDateDesc();
    }

    @Transactional
    public void updateOrderStatusToCompleted(Long orderId) {
        OrderList order = orderListRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. id=" + orderId));
        order.setProcessStatus(ProcessStatus.COMPLETED);
        orderListRepository.save(order);
    }

    @Transactional
    public void deleteOrderById(Long orderId) {
        orderListRepository.deleteById(orderId);
    }
}
