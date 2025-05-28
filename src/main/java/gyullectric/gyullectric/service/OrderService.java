package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.repository.InventoryRepository;
import gyullectric.gyullectric.repository.OrderHistoryRepository;
import gyullectric.gyullectric.repository.OrderListRepository;
import gyullectric.gyullectric.repository.OrderRepository;

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

    // 두 브랜치에서 사용하던 리포지토리들을 모두 포함
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    // ======== Inventory 관련 기능 ========

    @Transactional
    public void saveInventory(Inventory inventory) {
        // 원래는 inventoryRepository.save()였으나 orderRepository.save() 쪽 코드도 있으니
        // 만약 Inventory 관련은 inventoryRepository로 저장하는게 맞다면 아래처럼 유지하세요.
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

//    제품별 원재료 맵핑
    private static final Map<ProductName, List<PartName>> PRODUCT_NAME_LIST_MAP = Map.of(
            ProductName.GyulRide, List.of(PartName.FRAME, PartName.MOTOR, PartName.CONTROLLER, PartName.WHEEL, PartName.BATTERY_PACK),
            ProductName.InteliBike, List.of(PartName.FRAME, PartName.MOTOR, PartName.CONTROLLER, PartName.WHEEL, PartName.BATTERY_PACK),
            ProductName.Pedal_at_4, List.of(PartName.FRAME, PartName.MOTOR, PartName.CONTROLLER, PartName.WHEEL, PartName.BATTERY_PACK)
);

//    원재료 수 총합 구하기
    public Map<PartName, Long> getInventoryQuantity(){
        List<Inventory> inventories = inventoryRepository.findAll();

        Map<PartName, Long> partNameLongMap = inventories.stream().collect(Collectors.groupingBy(
                Inventory::getPartName,
                Collectors.summingLong(Inventory::getQuantity)
        ));
        return  partNameLongMap;
    }

    public Map<PartName, Long> getRequiredInventoryStock(ProductName productName){
        Map<PartName, Long> inventoryQuantities = getInventoryQuantity();
        List<PartName> requiredInventories = PRODUCT_NAME_LIST_MAP.getOrDefault(productName, List.of());

        log.info("원재료 리스트 : {}", requiredInventories);

        Map<PartName, Long> partNameLongMap = requiredInventories.stream().collect(Collectors.toMap(
                partName -> partName,
                partName -> inventoryQuantities.getOrDefault(partName, 0L)
        ));
        return partNameLongMap;
    }

    public boolean isEnoughInventory(ProductName productName, int quantity){
        Map<PartName, Long> requiredInventoryStock = getRequiredInventoryStock(productName);
        Map<PartName, Long> inventoryQuantities = getInventoryQuantity();

        return  requiredInventoryStock.entrySet().stream().allMatch(entry-> inventoryQuantities.getOrDefault(
                entry.getKey(), 0L) >= quantity);
    }

    public Page<Inventory> orderGetList(int page, String kw, String partName, String supplier) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("orderAt")));

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




    // ======== 재고 OrderHistory 관련 기능 ========
    @Transactional
    public void saveOrderHistory(OrderHistory history) {
        orderHistoryRepository.save(history);
    }

    public List<OrderHistory> historyAllFind() {
        return orderHistoryRepository.findAll();
    }

    public Page<Inventory> orderHistoryGetList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("orderAt"));
        PageRequest pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return orderRepository.findAll(pageable);
    }

    public List<OrderHistory> lastPageCancelOrder(int currentPage) {
        Page<Inventory> pagedOrders = orderHistoryGetList(currentPage);
        int totalPages = pagedOrders.getTotalPages();

        if (currentPage >= totalPages - 1) {
            return orderHistoryRepository.findAll(Sort.by(Sort.Order.desc("orderedAt")));
        }
        return Collections.emptyList();
    }
}
