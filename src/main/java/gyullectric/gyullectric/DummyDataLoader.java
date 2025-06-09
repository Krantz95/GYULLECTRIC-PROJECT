package gyullectric.gyullectric;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.repository.BikeProductionRepository;
import gyullectric.gyullectric.service.MemberService;
import gyullectric.gyullectric.service.OrderService;
import gyullectric.gyullectric.service.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//@Component
@AllArgsConstructor
public class DummyDataLoader implements CommandLineRunner {
    private final BikeProductionRepository bikeProductionRepository;
    private final Random random = new Random();
    private final ProductService productService;
    private final MemberService memberService;
    private final OrderService orderService;

    @Override
    public void run(String... args) throws Exception {
//        // 더미 멤버 생성 및 저장
        Members members = Members.builder()
                .loginId("1")
                .password("1")
                .name("장민정")
                .phone("010-1234-5678")
                .createDate(LocalDateTime.now())
                .positionName(PositionName.ADMIN)
                .build();
        memberService.save(members);

//         부품 목록
        List<PartName> partNames = List.of(
                PartName.FRAME,
                PartName.MOTOR,
                PartName.CONTROLLER,
                PartName.WHEEL,
                PartName.BATTERY_PACK
        );

        // 공급업체 2종 반복
        for (PartName partName : partNames) {
            for (Supplier supplier : List.of(Supplier.NEOCONTROL, Supplier.ECO_POWER_CELL)) {
                Inventory inventory = Inventory.builder()
                        .members(members)
                        .orderAt(LocalDateTime.now())
                        .supplier(supplier)
                        .quantity(100)
                        .partName(partName)
                        .build();
                orderService.saveInventory(inventory);
            }
        }

        // 생산 목표량 더미 데이터 생성
        initDailyTargetProduction();
    }

    // 생산 목표량 더미 데이터 생성 메서드 추가
    @PostConstruct
    private void initDailyTargetProduction() {

        LocalDate today = LocalDate.now();
        // 이미 오늘 날짜의 데이터가 존재하는지 확인
        boolean exists = bikeProductionRepository.existsByProductionDate(today);
        if (exists) {
            System.out.println("⏭️ 오늘의 목표 생산량 데이터가 이미 존재합니다. 생성 생략.");
            return;
        }

        int totalTarget = 300; // 총 목표 생산량


        Map<ProductName, Integer> targetMap = distributeProduction(totalTarget);

        for (ProductName productName : ProductName.values()) {
            BikeProduction production = new BikeProduction();
            production.setProductionDate(today);
            production.setProductName(productName);
            production.setTargetCount(targetMap.get(productName)); // 랜덤 목표 수량
            production.setActualCount(0); // 주문 전이므로 0으로 초기화

            bikeProductionRepository.save(production);
        }

        System.out.println("✅ 오늘의 목표 생산량 더미 데이터 생성 완료!");
    }

    private Map<ProductName, Integer> distributeProduction(int total) {
        int r1 = random.nextInt(total + 1);
        int r2 = random.nextInt(total - r1 + 1);
        int a = r1;
        int b = r2;
        int c = total - a - b;

        Map<ProductName, Integer> map = new HashMap<>();
        map.put(ProductName.GyulRide, a);
        map.put(ProductName.InteliBike, b);
        map.put(ProductName.PedalAt4, c);

        return map;
    }






        // 제품 더미 (필요 시 주석 해제하여 사용)
        /*
        List<ProductName> productNames = List.of(
                ProductName.PedalAt4,
                ProductName.GyulRide,
                ProductName.InteliBike
        );

        List<OrderList> dummyOrders = productNames.stream()
                .map(productName -> OrderList.builder()
                        .productName(productName)
                        .processStatus(ProcessStatus.PENDING)
                        .quantity(30)
                        .members(members)
                        .orderDate(LocalDateTime.now())
                        .dueDate(LocalDateTime.now().plusDays(7))
                        .build()
                ).toList();

        dummyOrders.forEach(productService::saveOrderList);
        */
    }



