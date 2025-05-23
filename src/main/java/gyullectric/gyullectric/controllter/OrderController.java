package gyullectric.gyullectric.controllter;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @GetMapping("/inventory")
    public String getInventory () {
        return "inventory/inventory";
    }

    @GetMapping("/history")
    public String getOrderHistory () {
        return "inventory/inventoryOrder";
    }

    // 주문 목록 페이지
    @GetMapping("/list")
    public String getOrderList(Model model, HttpSession session) {
        Members loginMember = (Members) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<OrderList> orders = orderService.getAllOrdersOrderByOrderDateDesc();
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("orders", orders);
        return "product/orderlist";
    }

    // 공정 시작 요청 (PENDING → COMPLETED)
    @PostMapping("/process/new/{id}")
    public String startProcess(@PathVariable Long id, HttpSession session) {
        Members loginMember = (Members) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        orderService.updateOrderStatusToCompleted(id);
        return "redirect:/order/list";
    }

    // 주문 삭제
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, HttpSession session) {
        Members loginMember = (Members) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        orderService.deleteOrderById(id);
        return "redirect:/order/list";
    }
}
