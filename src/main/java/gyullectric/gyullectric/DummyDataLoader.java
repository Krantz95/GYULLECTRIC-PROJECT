package gyullectric.gyullectric;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.repository.BikeProductionRepository;
import gyullectric.gyullectric.service.MemberService;
import gyullectric.gyullectric.service.OrderService;
import gyullectric.gyullectric.service.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

// ✅ 운영 시 자동 실행되도록 활성화
@Component
@AllArgsConstructor
public class DummyDataLoader implements CommandLineRunner {

    private final BikeProductionRepository bikeProductionRepository;
    private final Random random = new Random();
    private final ProductService productService;
    private final MemberService memberService;
    private final OrderService orderService;

    @PostConstruct
    public void init() {
        Faker faker = new Faker(new Locale("ko"));
        Set<String> usedLoginIds = new HashSet<>();

        // ✅ 고정 관리자 계정들
        List<Members> admins = List.of(
                Members.builder()
                        .loginId("1").password("1").name("장민정")
                        .phone("010-8760-6541").createDate(LocalDateTime.now())
                        .positionName(PositionName.ADMIN).build(),
                Members.builder()
                        .loginId("2").password("2").name("장아름")
                        .phone("010-2222-2222").createDate(LocalDateTime.now())
                        .positionName(PositionName.ADMIN).build(),
                Members.builder()
                        .loginId("3").password("3").name("김민수")
                        .phone("010-3333-3333").createDate(LocalDateTime.now())
                        .positionName(PositionName.ADMIN).build(),
                Members.builder()
                        .loginId("test").password("1111").name("test")
                        .phone("010-0000-0000").createDate(LocalDateTime.now())
                        .positionName(PositionName.ADMIN).build()
        );

        for (Members admin : admins) {
            if (!memberService.existsByLoginId(admin.getLoginId())) {
                admin.setCreateDate(LocalDateTime.now());
                memberService.save(admin);
            }
            usedLoginIds.add(admin.getLoginId());
        }

        // ✅ 더미 엔지니어 100명 생성 (중복 방지)
        for (int i = 0; i < 100; i++) {
            String name = faker.name().fullName();
            String phone = "010-" + faker.number().digits(4) + "-" + faker.number().digits(4);

            String loginId;
            do {
                loginId = faker.bothify("eng??##");
            } while (usedLoginIds.contains(loginId));
            usedLoginIds.add(loginId);

            String password = "pass" + (i + 1);

            if (!memberService.existsByLoginId(loginId)) {
                Members member = Members.builder()
                        .loginId(loginId)
                        .password(password)
                        .name(name)
                        .phone(phone)
                        .createDate(LocalDateTime.now())
                        .positionName(PositionName.ENGINEER)
                        .build();
                memberService.save(member);
            }
        }
    }

    @Override
    public void run(String... args) {
        // ✅ 관리자 계정 조회
        Members fixedAdmin = memberService.findByLoginId("1")
                .orElseThrow(() -> new IllegalStateException("고정 관리자 계정(loginId=1)을 찾을 수 없습니다."));

        // ✅ 부품 목록
        List<PartName> partNames = List.of(
                PartName.FRAME,
                PartName.MOTOR,
                PartName.CONTROLLER,
                PartName.WHEEL,
                PartName.BATTERY_PACK
        );

        // ✅ 재고 생성 (중복 방지)
        for (PartName partName : partNames) {
            for (Supplier supplier : List.of(Supplier.NEOCONTROL, Supplier.ECO_POWER_CELL)) {
                boolean exists = orderService.existsInventoryByPartNameAndSupplier(partName, supplier);
                if (!exists) {
                    Inventory inventory = Inventory.builder()
                            .members(fixedAdmin)
                            .orderAt(LocalDateTime.now())
                            .supplier(supplier)
                            .quantity(400)
                            .partName(partName)
                            .build();
                    orderService.saveInventory(inventory);
                }
            }
        }

        // ✅ 생산 목표량 더미 생성
        initDailyTargetProduction();
    }

    // ✅ 생산 목표량 생성 (중복 방지 있음)
    private void initDailyTargetProduction() {
        LocalDate today = LocalDate.now();
        boolean exists = bikeProductionRepository.existsByProductionDate(today);
        if (exists) {
            System.out.println("⏭️ 오늘의 목표 생산량 데이터가 이미 존재합니다. 생성 생략.");
            return;
        }

        int totalTarget = 300;
        Map<ProductName, Integer> targetMap = distributeProduction(totalTarget);

        for (ProductName productName : ProductName.values()) {
            BikeProduction production = new BikeProduction();
            production.setProductionDate(today);
            production.setProductName(productName);
            production.setTargetCount(targetMap.get(productName));
            production.setActualCount(0);

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
}
