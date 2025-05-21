package gyullectric.gyullectric.domain;

import java.time.LocalDateTime;

public class OrderHistory {
    private Long id;
    private PartName partName;
    private Supplier supplier;
    private int quantity;
    private LocalDateTime orderedAt;
    private OrderStatus status;
}
