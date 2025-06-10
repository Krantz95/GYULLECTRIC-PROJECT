package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.ProductOrderForm;
import gyullectric.gyullectric.repository.BikeProductionRepository;
import gyullectric.gyullectric.repository.OrderListRepository;
import gyullectric.gyullectric.service.OrderService;
import gyullectric.gyullectric.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private final OrderListRepository orderListRepository;
    private final BikeProductionRepository bikeProductionRepository;


    @GetMapping("/create")
    public String productOrderNew(Model model, HttpSession session){
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember == null){
            return "redirect:/login";
        }
        Map<PartName, Long> inventoryQuantities = orderService.getInventoryQuantity();
        log.info("총 재료 : {}", inventoryQuantities);

        // 오늘의 생산 목표 Map<ProductName, Integer>
        Map<ProductName, Integer> dailyTargetMap = productService.getTodayTargetMap();

        // 제품별 주문량 Map
        Map<ProductName, Integer> orderedCountMap = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        for (ProductName productName : dailyTargetMap.keySet()) {
            Integer orderedCount = orderListRepository.sumQuantityByProductNameAndDateBetween(productName, startOfDay, startOfNextDay);
            if (orderedCount == null) orderedCount = 0;
            orderedCountMap.put(productName, orderedCount);
        }



        model.addAttribute("orderedCountMap", orderedCountMap);
        model.addAttribute("dailyTargetMap", dailyTargetMap);
        model.addAttribute("productOrderForm", new ProductOrderForm());
        return "product/orderNew";
    }

    @PostMapping("/create")
    public String productOrderPost(@ModelAttribute("loginMember")Members loginMember, Model model,
                                   @Valid @ModelAttribute("productOrderForm")ProductOrderForm productOrderForm, BindingResult bindingResult){
        if(bindingResult.hasErrors()){

            log.info("{}", productOrderForm.getProductName());
            return "product/orderNew";
        }

        int remainingQty = productService.getRemainingOrderQuantity(productOrderForm.getProductName());

        if (productOrderForm.getQuantity() > remainingQty) {
            bindingResult.reject("exceedTarget", "목표 생산량을 초과하는 주문입니다. 주문 가능 수량: " + remainingQty);
            return getCreatePageModel(model, productOrderForm);
        }

        // 재고 체크
        if (!orderService.isEnoughInventory(productOrderForm.getProductName(), productOrderForm.getQuantity())) {
            bindingResult.reject("notEnoughMaterial", "재고가 부족합니다.");

            // 재고 부족 시에도 오늘 생산 목표 및 실제 생산량 보여주기
            LocalDate today = LocalDate.now();
            List<BikeProduction> productions = bikeProductionRepository.findByProductionDate(today);
            Map<String, Integer> dailyTargetMap = productions.stream()
                    .collect(Collectors.toMap(
                            bp -> bp.getProductName().name(),
                            BikeProduction::getTargetCount
                    ));

            model.addAttribute("dailyTargetMap", dailyTargetMap);


            return "product/orderNew";
        }

        Map<PartName, Long> requiredInventoryStock = orderService.getRequiredInventoryStock(productOrderForm.getProductName());
        log.info("productName에 따른 수량 : {}", requiredInventoryStock);


        OrderList orderList = OrderList.builder()
                .productName(productOrderForm.getProductName())
                .quantity(productOrderForm.getQuantity())
                .orderDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .members(loginMember)
                .processStatus(ProcessStatus.PENDING)
                .build();
        productService.saveOrderList(orderList);
        return "redirect:/product/list";
    }

    private String getCreatePageModel(Model model, ProductOrderForm productOrderForm) {
        List<BikeProduction> productions = productService.getTodayProductions();

        Map<String, Integer> dailyTargetMap = productions.stream()
                .collect(Collectors.toMap(bp -> bp.getProductName().name(), BikeProduction::getTargetCount));

        Map<String, Integer> dailyActualMap = productions.stream()
                .collect(Collectors.toMap(bp -> bp.getProductName().name(), BikeProduction::getActualCount));

        model.addAttribute("dailyTargetMap", dailyTargetMap);
        model.addAttribute("dailyActualMap", dailyActualMap);
        model.addAttribute("productOrderForm", productOrderForm);

        return "product/orderNew";
    }
    //    //    주문리스트
    @GetMapping("/list")
    public String orderList(Model model, HttpSession session){
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember == null){
            return "redirect:/login";
        }
        List<OrderList> orderLists = productService.allFindOrderList();
        model.addAttribute("orderLists", orderLists);
        return "product/orderList";
    }

    @GetMapping("/delete/{id}")
    public String deleteOrderList(@PathVariable("id")Long id){
        OrderList orderList = productService.oneFindOrderList(id).orElseThrow(()->new IllegalArgumentException("제품을 찾을 수 없습니다."));

        productService.deleteOrderList(orderList.getId());
        return "redirect:/product/list";
    }

}
