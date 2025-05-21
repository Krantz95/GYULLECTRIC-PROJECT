package gyullectric.gyullectric.controllter;

import org.springframework.stereotype.Controller;
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
    public String getOrderList () {
        return "product/orderList";
    }

}
