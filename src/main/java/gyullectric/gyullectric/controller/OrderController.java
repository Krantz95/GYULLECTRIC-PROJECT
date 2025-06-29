package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.InventoryForm;
import gyullectric.gyullectric.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 재고 목록 조회 (검색 + 페이징)
     */
    @GetMapping("/inventory")
    public String getInventory(HttpSession session, Model model,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "kw", defaultValue = "") String kw,
                               @RequestParam(value = "partName", defaultValue = "") String partName,
                               @RequestParam(value = "supplier", defaultValue = "") String supplier) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        Page<Inventory> paging = orderService.orderGetList(page, kw, partName, supplier);
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("partName", partName);
        model.addAttribute("supplier", supplier);
        model.addAttribute("partNames", PartName.values());
        model.addAttribute("suppliers", Supplier.values());
        model.addAttribute("inventoryForm", new InventoryForm());

        return "inventory/inventory";
    }

    /**
     * 재고 등록
     */
    @PostMapping("/inventory")
    public String postInventory(@Valid @ModelAttribute("inventoryForm") InventoryForm inventoryForm,
                                BindingResult bindingResult, HttpSession session, Model model,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "kw", defaultValue = "") String kw,
                                @RequestParam(value = "partName", defaultValue = "") String partName,
                                @RequestParam(value = "supplier", defaultValue = "") String supplier) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (inventoryForm.getQuantity() == null || inventoryForm.getQuantity() <= 0) {
            bindingResult.rejectValue("quantity", "Invalid.quantity", "수량은 1 이상의 숫자여야 합니다.");
        }

        if (bindingResult.hasErrors()) {
            Page<Inventory> paging = orderService.orderGetList(page, kw, partName, supplier);
            model.addAttribute("loginMember", loginMember);
            model.addAttribute("paging", paging);
            model.addAttribute("kw", kw);
            model.addAttribute("partName", partName);
            model.addAttribute("supplier", supplier);
            model.addAttribute("partNames", PartName.values());
            model.addAttribute("suppliers", Supplier.values());
            return "inventory/inventory";
        }

        Inventory inventory = Inventory.builder()
                .id(inventoryForm.getId())
                .partName(inventoryForm.getPartName())
                .quantity(inventoryForm.getQuantity())
                .supplier(inventoryForm.getSupplier())
                .orderAt(LocalDateTime.now())
                .members(loginMember)
                .build();

        orderService.saveInventory(inventory);
        return "redirect:/order/inventory";
    }

    /**
     * 발주 이력 페이지 진입
     */
    @GetMapping("/history")
    public String getOrderHistory(Model model,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  HttpSession session) {
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember == null){
            return "redirect:/login";
        }
        Page<Inventory> paging = orderService.orderHistoryGetList(page);
        List<OrderHistory> historyList = orderService.lastPageCancelOrder(page);


        model.addAttribute("historyList", historyList);
        model.addAttribute("paging", paging);
        return "inventory/inventoryOrder";
    }


    // 재고 주문 삭제
    @GetMapping("/{id}/delete")
    public String deleteHistory(@PathVariable("id") Long id) {
        Inventory inventory = orderService.findOneInventory(id).orElseThrow(() -> new IllegalArgumentException("해당 재고를 찾을 수 없습니다"));

        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setPartName(inventory.getPartName());
        orderHistory.setSupplier(inventory.getSupplier());
        orderHistory.setOrderedAt(inventory.getOrderAt());
        orderHistory.setQuantity(inventory.getQuantity());
        orderService.saveOrderHistory(orderHistory);

        orderService.deleteInventory(inventory.getId());
        return "redirect:/order/history";
    }

}