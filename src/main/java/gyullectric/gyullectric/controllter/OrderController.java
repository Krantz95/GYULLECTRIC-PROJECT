package gyullectric.gyullectric.controllter;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/order")
public class OrderController {

    @GetMapping("/inventory")
    public String getInventory () {
        return "inventory/inventory";
    }

    @GetMapping("/history")
    public String getOrderHistory () {
        return "inventory/inventoryOrder";
    }

    @GetMapping("/list")
    public String getOrderList (Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        // 컨트롤러 서비스 연결 필요 250523
        // model.addAttribute("orderList", orderList);
        return "product/orderList";
    }

}
