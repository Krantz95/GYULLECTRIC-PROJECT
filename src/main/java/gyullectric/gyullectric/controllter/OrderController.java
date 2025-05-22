package gyullectric.gyullectric.controllter;

import gyullectric.gyullectric.domain.*;
import gyullectric.gyullectric.dto.InventoryForm;
import gyullectric.gyullectric.dto.MembersForm;
import gyullectric.gyullectric.dto.MembersUpdateForm;
import gyullectric.gyullectric.service.MemberService;
import gyullectric.gyullectric.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;


    @GetMapping("/inventory")
    public String getInventory (HttpSession session, Model model, @RequestParam(value = "page", defaultValue = "0")int page,
                                @RequestParam(value = "kw", defaultValue = "") String kw,
                                @RequestParam(value = "partName", required = false, defaultValue = "") String partName,
                                @RequestParam(value = "supplier", required = false, defaultValue = "") String supplier) {
        // 로그인 및 권한 체크
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/";
        }
        if (loginMember.getPositionName() != PositionName.ADMIN) {
            return "redirect:/";
        }

        // 검색 조건으로 페이징 처리
        Page<Inventory> paging = orderService.orderGetList(page, kw, partName, supplier);

        // 뷰에 필요한 데이터들
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("partName", partName);
        model.addAttribute("supplier", supplier);

        // 드롭다운 목록용 enum
        model.addAttribute("partNames", PartName.values());
        model.addAttribute("suppliers", Supplier.values());
        model.addAttribute("inventoryForm", new InventoryForm());

        return "inventory/inventory";
    }
    @PostMapping("/inventory")
    public String postInventory(@Valid @ModelAttribute("inventoryForm")InventoryForm inventoryForm, BindingResult bindingResult, Model model,
                                HttpSession session){
        Members loginMember = (Members) session.getAttribute("loginMember");
        if (loginMember == null) {
            // 로그인 정보가 없으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }
        if(inventoryForm.getQuantity() == null || inventoryForm.getQuantity() <= 0) {
            bindingResult.rejectValue("quantity", "Invalid.quantity", "수량은 1 이상의 숫자여야 합니다.");
            return "inventory/inventory";
        }
        if(bindingResult.hasErrors()){
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

    @GetMapping("/history")
    public String getOrderHistory (Model model) {

        List<Inventory> inventoryList = orderService.findAllInventory();
        model.addAttribute("inventoryList", inventoryList);
        return "inventory/inventoryOrder";
    }

    @GetMapping("/list")
    public String getOrderList () {
        return "product/orderList";
    }

}