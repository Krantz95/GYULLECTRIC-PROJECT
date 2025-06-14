package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.repository.InventoryRepository;
import gyullectric.gyullectric.repository.OrderHistoryRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final InventoryRepository inventoryRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    /* ------------------------------------------------------------------
       🚲 제품별 필요 부품 매핑 (제품 추가 시 여기만 수정)
    ------------------------------------------------------------------ */
    private static final Map<ProductName, List<PartName>> PRODUCT_NAME_LIST_MAP = Map.of(
            ProductName.GyulRide,   List.of(PartName.FRAME, PartName.MOTOR, PartName.CONTROLLER, PartName.WHEEL, PartName.BATTERY_PACK),
            ProductName.InteliBike, List.of(PartName.FRAME, PartName.MOTOR, PartName.CONTROLLER, PartName.WHEEL, PartName.BATTERY_PACK),
            ProductName.PedalAt4,   List.of(PartName.FRAME, PartName.MOTOR, PartName.CONTROLLER, PartName.WHEEL, PartName.BATTERY_PACK)
    );

    /* ------------------------------------------------------------------
       🔄 인벤토리 기본 CRUD
    ------------------------------------------------------------------ */
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

    /* ------------------------------------------------------------------
       📊 재고 집계/조회 로직
    ------------------------------------------------------------------ */
    public Map<PartName, Long> getInventoryQuantity() {
        return inventoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Inventory::getPartName,
                        Collectors.summingLong(Inventory::getQuantity)
                ));
    }

    public Map<PartName, Long> getRequiredInventoryStock(ProductName productName) {
        Map<PartName, Long> current = getInventoryQuantity();
        List<PartName> required = PRODUCT_NAME_LIST_MAP.getOrDefault(productName, List.of());

        return required.stream()
                .collect(Collectors.toMap(
                        part -> part,
                        part -> current.getOrDefault(part, 0L)
                ));
    }

    public boolean isEnoughInventory(ProductName productName, int quantity) {
        Map<PartName, Long> current = getInventoryQuantity();
        List<PartName> required = PRODUCT_NAME_LIST_MAP.getOrDefault(productName, List.of());

        return required.stream()
                .allMatch(part -> current.getOrDefault(part, 0L) >= quantity);
    }

    /* ------------------------------------------------------------------
       📑 재고 목록(검색+페이징)
    ------------------------------------------------------------------ */
    public Page<Inventory> orderGetList(int page, String kw, String partName, String supplier) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("orderAt")));

        // 검색 조건 없으면 전체 조회
        if ((kw == null || kw.isBlank()) &&
                (partName == null || partName.isBlank()) &&
                (supplier == null || supplier.isBlank())) {
            return inventoryRepository.findAll(pageable);
        }

        Specification<Inventory> spec = buildSearchSpec(kw, partName, supplier);
        return inventoryRepository.findAll(spec, pageable);
    }

    private Specification<Inventory> buildSearchSpec(String kw, String partName, String supplier) {
        return (Root<Inventory> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (kw != null && !kw.isBlank()) {
                Predicate partLike = cb.like(cb.lower(root.get("partName").as(String.class)), "%" + kw.toLowerCase() + "%");
                Predicate supLike  = cb.like(cb.lower(root.get("supplier").as(String.class)), "%" + kw.toLowerCase() + "%");
                predicates.add(cb.or(partLike, supLike));
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

    /* ------------------------------------------------------------------
       🗂️ 발주 이력 처리
    ------------------------------------------------------------------ */
    @Transactional
    public void saveOrderHistory(OrderHistory history) {
        orderHistoryRepository.save(history);
    }

    public List<OrderHistory> historyAllFind() {
        return orderHistoryRepository.findAll();
    }

    /** 📄 발주 이력 페이지용 인벤토리 페이징 */
    public Page<Inventory> orderHistoryGetList(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("orderAt")));
        return inventoryRepository.findAll(pageable);          // ← 변경 포인트
    }

    /** 마지막 페이지(취소 시)용 정렬 리스트 */
    public List<OrderHistory> lastPageCancelOrder(int currentPage) {
        int totalPages = orderHistoryGetList(currentPage).getTotalPages();
        if (currentPage >= totalPages - 1) {
            return orderHistoryRepository.findAll(
                    Sort.by(Sort.Order.desc("orderedAt")));
        }
        return Collections.emptyList();
    }

    /* ------------------------------------------------------------------
       🔎 기타 편의 메서드
    ------------------------------------------------------------------ */
    public int getInventoryQuantity(PartName partName) {
        return getInventoryQuantity().getOrDefault(partName, 0L).intValue();
    }

    /** 부족한 부품 리스트 */
    public List<PartName> getInsufficientParts(ProductName productName, int orderQty) {
        Map<PartName, Long> required = getRequiredInventoryStock(productName);
        Map<PartName, Long> current  = getInventoryQuantity();

        return required.entrySet().stream()
                .filter(e -> current.getOrDefault(e.getKey(), 0L) < e.getValue() * orderQty)
                .map(Map.Entry::getKey)
                .toList();
    }
}
