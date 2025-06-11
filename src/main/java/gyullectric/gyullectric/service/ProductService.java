package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.repository.BikeProductionRepository;
import gyullectric.gyullectric.repository.OrderListRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final OrderListRepository orderListRepository;
    private final OrderService orderService;
    private final BikeProductionRepository bikeProductionRepository;

    // ======== OrderList 관련 기능 ========

//    주문재고저장
   @Transactional
    public void saveOrderList(OrderList orderList){
      int orderQuantity = orderList.getQuantity();
      ProductName productName = orderList.getProductName();

//       재고확인
//       if(!orderService.isEnoughInventory(productName, orderQuantity)){
//           throw new IllegalStateException("재료가 부족합니다");
//       }

//       해당 제품에 필요한 원재료 목록을 조회
       Map<PartName, Long> requiredInventoryStock = orderService.getRequiredInventoryStock(productName);
       log.info("requiredInventoryStock, 원재료 재고수량{}", requiredInventoryStock);

       for(Map.Entry<PartName, Long> entry : requiredInventoryStock.entrySet()) {
           PartName partName = entry.getKey();
           long requiredOrderQuantity = orderQuantity;

           log.info("entry- 원재료이름{}", entry.getKey());
           log.info("entry-원자재 재고수량{}", entry.getValue());
           log.info("orderQuantity 주문수량{}", requiredOrderQuantity);

           List<Inventory> inventories = orderService.findInventoryName(partName);

//           if (inventories.isEmpty()) {
//               throw new IllegalStateException(partName + "재료가 존재하지 않습니다");
//           }
           for (Inventory inventory : inventories) {
               if (requiredOrderQuantity <= 0) break;

               long inventoryQuantity = inventory.getQuantity();
               long minQuantity = Math.min(inventoryQuantity, requiredOrderQuantity);

               inventory.setQuantity((int) (inventoryQuantity - minQuantity));

               log.info("차감된 원재로:{}, 차감량:{}, 남은 주문 필요량{}", partName, minQuantity, requiredOrderQuantity);
               orderService.saveInventory(inventory);

               OrderListInventory jm = new OrderListInventory();
               jm.setInventory(inventory);
               jm.setDeductedQuantity((int) minQuantity);
               orderList.addOrderListInventory(jm);

               requiredOrderQuantity -= minQuantity;
           }
           if (requiredOrderQuantity > 0) {
               throw new IllegalStateException(partName + "의 재고가 부족합니다. (부족 수량: " + requiredOrderQuantity + ")");
           }
       }
           orderListRepository.save(orderList);
       }
       @Transactional
    public void updateOrderState(Long orderId, ProcessStatus processStatus){
       OrderList orderList = oneFindOrderList(orderId).orElseThrow(()-> new IllegalArgumentException("주문이 없습니다"));
       orderList.setProcessStatus(processStatus);
       }

       public Optional<OrderList> oneFindOrderList(Long id){
       return orderListRepository.findById(id);
       }

       public List<OrderList> allFindOrderList(){
       return orderListRepository.findAll();
       }

// 삭제
    @Transactional
    public void deleteOrderList(Long id){
       OrderList orderList = orderListRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("주문을 찾을 수 없습니다" + id));

    for(OrderListInventory jm : orderList.getOrderListInventoryLists())  {
        Inventory inventory = jm.getInventory();
        log.info("기존에 있던 재고 : {}", inventory.getQuantity());
        log.info("더해야하는 재고 수량 : {}", jm.getDeductedQuantity());

        inventory.setQuantity(inventory.getQuantity() + jm.getDeductedQuantity());

        orderService.saveInventory(inventory);
    }
        orderListRepository.deleteById(id);
    }

    public Optional<OrderList> findLatestOrderByOrderId(Long orderId) {
        return orderListRepository.findTopByIdOrderByOrderDateDesc(orderId);
    }

    public List<BikeProduction> getTodayProductionTargets() {
        LocalDate today = LocalDate.now();
        return bikeProductionRepository.findByProductionDate(today);
    }


    // 남은 주문 수량 계산 (목표 - 주문량)
    public int getRemainingOrderQuantity(ProductName productName) {
        LocalDate today = LocalDate.now();
        BikeProduction production = bikeProductionRepository.findByProductionDateAndProductName(today, productName)
                .orElseThrow(() -> new IllegalArgumentException("오늘의 생산 목표가 없습니다"));

        int targetCount = production.getTargetCount();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        Integer orderedCount = orderListRepository.sumQuantityByProductNameAndDateBetween(productName, startOfDay, startOfNextDay);

        if (orderedCount == null) orderedCount = 0;

        return targetCount - orderedCount;
    }

    public Map<ProductName, Integer> getTodayTargetMap() {
        LocalDate today = LocalDate.now();

        // 오늘 날짜의 생산 목표 불러오기
        List<BikeProduction> productions = bikeProductionRepository.findByProductionDate(today);

        // 원하는 정렬 순서 정의
        List<ProductName> customOrder = List.of(
                ProductName.GyulRide,
                ProductName.InteliBike,
                ProductName.PedalAt4
        );

        // 정렬 후 LinkedHashMap으로 수집 (순서 유지)
        Map<ProductName, Integer> targetMap = productions.stream()
                .sorted(Comparator.comparing(bp -> customOrder.indexOf(bp.getProductName())))
                .collect(Collectors.toMap(
                        BikeProduction::getProductName,
                        BikeProduction::getTargetCount,
                        (e1, e2) -> e1,              // 키 충돌 처리
                        LinkedHashMap::new           // 순서 보장
                ));

        return targetMap;
    }

    // 오늘 목표, 실제 생산량 리스트 조회
    public List<BikeProduction> getTodayProductions() {
        return bikeProductionRepository.findByProductionDate(LocalDate.now());
    }
}




