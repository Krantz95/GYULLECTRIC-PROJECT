package gyullectric.gyullectric.controller;

import gyullectric.gyullectric.domain.Members;
import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.PartName;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.dto.ProductOrderForm;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final OrderService orderService;


    @GetMapping("/create")
    public String productOrderNew(Model model, HttpSession session){
        Members loginMember = (Members) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember == null){
            return "redirect:/login";
        }
        Map<PartName, Long> inventoryQuantities = orderService.getInventoryQuantity();
        log.info("총 재료 : {}", inventoryQuantities);
        model.addAttribute("productOrderForm", new ProductOrderForm());
        return "product/orderNew";
    }

    @PostMapping("/create")
    public String productOrderPost(@ModelAttribute("loginMember")Members loginMember,
                                   @Valid @ModelAttribute("productOrderForm")ProductOrderForm productOrderForm, BindingResult bindingResult){
       if(bindingResult.hasErrors()){
           log.info("{}", productOrderForm.getProductName());
           return "product/orderNew";
       }

        Map<PartName, Long> requiredInventoryStock = orderService.getRequiredInventoryStock(productOrderForm.getProductName());
        log.info("productName에 따른 수량 : {}", requiredInventoryStock);

        if(!orderService.isEnoughInventory(productOrderForm.getProductName(),
                productOrderForm.getQuantity())){
            bindingResult.reject("notEnoughMaterial", "재고가 부족합니다.");
            return "product/orderNew";
        }
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
