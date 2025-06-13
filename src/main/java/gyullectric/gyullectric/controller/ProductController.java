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

import java.time.LocalDateTime;
import java.util.*;

/**
 * 🛒 ProductController – 제품 주문 & 주문 목록 관리
 *   1. 주문 생성 페이지 (GET) – 목표·완료·잔여 수량 표시
 *   2. 주문 생성 처리  (POST) – 목표 초과/완료 예외 처리
 *   3. 주문 목록 조회 & 삭제
 */
@Slf4j
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;
    private final MonitoringService monitoringService;
    private final MonitoringDataService monitoringDataService;

    /* ================= 주문 생성 ================= */

    /** 주문 생성 폼 */
    @GetMapping("/create")
    public String showOrderCreateForm(Model model, HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";

        // 1) 목표 생산량 & 완료 수량 → 잔여 수량 계산
        Map<ProductName, Integer> dailyTarget = productService.getTodayTargetMap();
        Map<ProductName, Integer> completed = getCompletedCountMap();

        model.addAttribute("productOrderForm", new ProductOrderForm());
        model.addAttribute("dailyTargetMap", dailyTarget);
        model.addAttribute("orderedCountMap", completed);
        return "product/orderNew";
    }

    /** 주문 등록 */
    @PostMapping("/create")
    public String createOrder(@Valid @ModelAttribute("productOrderForm") ProductOrderForm form,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) return "redirect:/login";

        Map<ProductName, Integer> dailyTarget = productService.getTodayTargetMap();
        Map<ProductName, Integer> completed = getCompletedCountMap();
        Map<ProductName, Integer> remaining = buildRemainingMap(dailyTarget, completed);

        /* 1) 유효성 검증 오류 */
        if (bindingResult.hasErrors()) {
            populateCreateFormModel(model, form, dailyTarget, completed, remaining);
            return "product/orderNew";
        }

        /* 2) 목표 초과 & 목표 달성 완료 체크 */
        ProductName product = form.getProductName();
        int remainingQty = remaining.getOrDefault(product, 0);
        if (remainingQty == 0) {
            reject(bindingResult, "errMessage", product + "의 금일 목표 달성 완료");
        } else if (form.getQuantity() > remainingQty) {
            reject(bindingResult, "exceedTarget", product + "의 목표 생산량을 초과하는 주문입니다. <br> 주문 가능 수량: " + remainingQty + "대");
        }
        if (bindingResult.hasErrors()) {
            populateCreateFormModel(model, form, dailyTarget, completed, remaining);
            return "product/orderNew";
        }

        /* 3) 재고 확인 & 주문 저장 */
        Map<PartName, Long> requiredStock = orderService.getRequiredInventoryStock(product);
        log.info("[{}] 예상 소모 자재: {}", product, requiredStock);

        OrderList order = OrderList.builder()
                .productName(product)
                .quantity(form.getQuantity())
                .orderDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .members(loginMember)
                .processStatus(ProcessStatus.PENDING)
                .build();
        productService.saveOrderList(order);

        return "redirect:/product/list";
    }

    /* ================= 주문 목록 ================= */

    /** 주문 리스트 */
    @GetMapping("/list")
    public String listOrders(Model model, HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";
        model.addAttribute("orderLists", productService.allFindOrderList());
        return "product/orderList";
    }

    /** 주문 삭제 */
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id) {
        productService.deleteOrderList(id);
        return "redirect:/product/list";
    }

    /* ================= Helper ================= */

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SessionConst.LOGIN_MEMBER) != null;
    }

    /** 완료 수량 계산 */
    private Map<ProductName, Integer> getCompletedCountMap() {
        List<OrderList> orders = productService.allFindOrderList();
        List<ProcessLog> logs = monitoringService.allFindProcesses();
        Map<Long, OrderSummaryDto> summary = monitoringDataService.getOrderSummaryByOrderId(logs);

        Map<ProductName, Integer> completed = new EnumMap<>(ProductName.class);
        for (OrderList o : orders) {
            int done = summary.getOrDefault(o.getId(), new OrderSummaryDto(0,0,0)).getFinishedLots();
            completed.merge(o.getProductName(), done, Integer::sum);
        }
        return completed;
    }

    /** 잔여 수량 맵 */
    private Map<ProductName, Integer> buildRemainingMap(Map<ProductName, Integer> target,
                                                        Map<ProductName, Integer> completed) {
        Map<ProductName, Integer> remaining = new EnumMap<>(ProductName.class);
        for (ProductName p : target.keySet()) {
            remaining.put(p, Math.max(0, target.get(p) - completed.getOrDefault(p, 0)));
        }
        return remaining;
    }

    /** 에러 메시지 편의 메서드 */
    private void reject(BindingResult br, String code, String msg) {
        br.reject(code, msg);
    }

    /** 주문 생성 폼에 필요한 모델 속성 주입 */
    private void populateCreateFormModel(Model model,
                                         ProductOrderForm form,
                                         Map<ProductName, Integer> target,
                                         Map<ProductName, Integer> completed,
                                         Map<ProductName, Integer> remaining) {
        model.addAttribute("productOrderForm", form);
        model.addAttribute("dailyTargetMap", target);
        model.addAttribute("orderedCountMap", completed);
        model.addAttribute("remainingCountMap", remaining);
    }
}
