package gyullectric.gyullectric;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.service.MemberService;
import gyullectric.gyullectric.service.OrderService;
import gyullectric.gyullectric.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//@Component
@AllArgsConstructor
public class DummyDataLoader implements CommandLineRunner {

    private final ProductService productService;
    private final MemberService memberService;
    private final OrderService orderService;

    @Override
    public void run(String... args) throws Exception {
        // 더미 멤버 생성 및 저장
        Members members = Members.builder()
                .loginId("1")
                .password("1")
                .name("장민정")
                .phone("010-1234-5678")
                .createDate(LocalDateTime.now())
                .positionName(PositionName.ADMIN)
                .build();
        memberService.save(members);

        // 부품 목록
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

        // 제품 더미 (필요 시 주석 해제하여 사용)
        /*
        List<ProductName> productNames = List.of(
                ProductName.Pedal_at_4,
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
}
