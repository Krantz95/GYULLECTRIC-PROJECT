package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.OrderSummaryDto;
import gyullectric.gyullectric.dto.ProductOrderForm;
import gyullectric.gyullectric.service.*;
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
    private final MonitoringService monitoringService;
    private final MonitoringDataService monitoringDataService;



    @GetMapping("/create")
    public String productOrderNew(Model model, HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }
        Map<PartName, Long> inventoryQuantities = orderService.getInventoryQuantity();
        log.info("총 재료 : {}", inventoryQuantities);

        // 오늘의 생산 목표 Map<ProductName, Integer>
        Map<ProductName, Integer> dailyTargetMap = productService.getTodayTargetMap();

        List<OrderList> productOrderList = productService.allFindOrderList();
        List<ProcessLog> processLogs = monitoringService.allFindProcesses();

        Map<String, Object> stats = monitoringDataService.calculateProductAchievementAndCounts(productOrderList, processLogs);
        Map<String, Long> completedMapRaw = (Map<String, Long>) stats.get("totalCompleteByProduct");

        Map<ProductName, Integer> completedCountMap = new HashMap<>();
        for (ProductName product : dailyTargetMap.keySet()) {
            int completed = completedMapRaw.getOrDefault(product.name(), 0L).intValue();
            completedCountMap.put(product, completed);
        }

        log.info("오늘의 목표량: {}, 생산량 : {}", dailyTargetMap, completedMapRaw);
        model.addAttribute("orderedCountMap", completedCountMap);
        model.addAttribute("dailyTargetMap", dailyTargetMap);
        model.addAttribute("productOrderForm", new ProductOrderForm());
        return "product/orderNew";
    }

    @PostMapping("/create")
    public String productOrderPost(@ModelAttribute("loginMember") Members loginMember, Model model,
                                   @Valid @ModelAttribute("productOrderForm") ProductOrderForm productOrderForm, BindingResult bindingResult) {

        Map<ProductName, Integer> dailyTargetMap = productService.getTodayTargetMap();
        Map<ProductName, Integer> completedCountMap = new HashMap<>();
        Map<ProductName, Integer> remainingCountMap = new HashMap<>();

        //  완제품 수량 계산
        List<OrderList> productOrderList = productService.allFindOrderList();
        List<ProcessLog> processLogs = monitoringService.allFindProcesses(); // 주입 필요

        Map<String, Object> stats = monitoringDataService.calculateProductAchievementAndCounts(productOrderList, processLogs);
        Map<String, Long> completedMapRaw = (Map<String, Long>) stats.get("totalCompleteByProduct");

        for (ProductName productName : dailyTargetMap.keySet()) {
            int completed = completedMapRaw.getOrDefault(productName.name(), 0L).intValue();
            int target = dailyTargetMap.get(productName);
            int remaining = Math.max(0, target - completed);

            completedCountMap.put(productName, completed);
            remainingCountMap.put(productName, remaining);
        }

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            model.addAttribute("productOrderForm", productOrderForm);
            model.addAttribute("dailyTargetMap", dailyTargetMap);
            model.addAttribute("orderedCountMap", completedCountMap);
            model.addAttribute("remainingCountMap", remainingCountMap);
            return "product/orderNew";
        }

        ProductName productName = productOrderForm.getProductName();
        int remainingQty = remainingCountMap.getOrDefault(productName, 0);

        if (productOrderForm.getQuantity() > remainingQty) {
            bindingResult.reject("exceedTarget", productName + "의 목표 생산량을 초과하는 주문입니다. <br> 주문 가능 수량: " + remainingQty + "대");

            model.addAttribute("productOrderForm", productOrderForm);
            model.addAttribute("dailyTargetMap", dailyTargetMap);
            model.addAttribute("orderedCountMap", completedCountMap);
            model.addAttribute("remainingCountMap", remainingCountMap);
            return "product/orderNew";
        }

        // 주문 저장
        Map<PartName, Long> requiredInventoryStock = orderService.getRequiredInventoryStock(productName);
        log.info("productName에 따른 수량 : {}", requiredInventoryStock);

        try {
            OrderList orderList = OrderList.builder()
                    .productName(productOrderForm.getProductName())
                    .quantity(productOrderForm.getQuantity())
                    .orderDate(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .members(loginMember)
                    .processStatus(ProcessStatus.PENDING)
                    .build();

            productService.saveOrderList(orderList);

        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("productOrderForm", productOrderForm);
            model.addAttribute("dailyTargetMap", dailyTargetMap);
            model.addAttribute("orderedCountMap", completedCountMap);
            model.addAttribute("remainingCountMap", remainingCountMap);
            return "product/orderNew";
        }

        return "redirect:/product/list";
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
