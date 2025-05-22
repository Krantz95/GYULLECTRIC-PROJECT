package gyullectric.gyullectric.domain;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

public class OrderList {
    private Long id;
    private ProductName productName;
    private int quantity;
    private Members noti_author;
    private LocalDateTime orderDate;
    private LocalDateTime dueDate;
    private ProcessStatus processStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="inventory_id")
    private Inventory inventory;
}
