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

        List<PartName> partNames = List.of(
                PartName.FRAME,
                PartName.MOTOR,
                PartName.CONTROLLER,
                PartName.WHEEL,
                PartName.BATTERY_PACK
        );

        List<Inventory> inventories = partNames.stream().map(partName -> Inventory.builder()
                .members(members)
                .orderAt(LocalDateTime.now())
                .supplier(Supplier.NEOCONTROL)
                .quantity(10)
                .partName(partName)
                .build()).toList();
        inventories.forEach(orderService::saveInventory);

        for(PartName partName : partNames){
            Inventory inventory = Inventory.builder()
                    .partName(partName)
                    .quantity(10)
                    .supplier(Supplier.ECO_POWER_CELL)
                    .orderAt(LocalDateTime.now())
                    .members(members)
                    .build();
            orderService.saveInventory(inventory);
        }

        List<ProductName> productNames = List.of(
                ProductName.Pedal_at_4,
                ProductName.GyulRide,
                ProductName.InteliBike
        );

//        List<OrderList> dummyOrders = productNames.stream()
//                .map(productName -> OrderList.builder()
//                        .productName(productName)
//                        .processStatus(ProcessStatus.PENDING)
//                        .quantity(30)
//                        .members(members)  // 로그인한 멤버 객체 넣음
//                        .orderDate(LocalDateTime.now())
//                        .dueDate(LocalDateTime.now().plusDays(7))
//                        .build()
//                ).toList();
//
//
//        dummyOrders.forEach(productService::saveOrderList);
//
//    }
//        for (ProductName productName : productNames) {
//            OrderList order = OrderList.builder()
//                    .productName(productName)
//                    .processStatus(ProcessStatus.PENDING)
//                    .quantity(30)
//                    .members(members)
//                    .orderDate(LocalDateTime.now())
//                    .dueDate(LocalDateTime.now().plusDays(7))
//                    .build();
//
//            productService.saveOrderList(order);
//        }
    }
}