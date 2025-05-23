package gyullectric.gyullectric.service;

import gyullectric.gyullectric.domain.OrderList;
import gyullectric.gyullectric.domain.ProcessStatus;
import gyullectric.gyullectric.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderListRepository;

    public List<OrderList> getAllOrdersOrderByOrderDateDesc() {
        return orderListRepository.findAllByOrderByOrderDateDesc();
    }

    public void updateOrderStatusToCompleted(Long orderId) {
        OrderList order = orderListRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. id=" + orderId));
        order.setProcessStatus(ProcessStatus.COMPLETED);
        orderListRepository.save(order);
    }

    public void deleteOrderById(Long orderId) {
        orderListRepository.deleteById(orderId);
    }
}

