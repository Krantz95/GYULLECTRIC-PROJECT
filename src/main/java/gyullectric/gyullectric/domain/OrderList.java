package gyullectric.gyullectric.domain;

import jakarta.persistence.Entity;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
public class OrderList {
    private Long id;
    private ProductName productName;
    private int quantity;
    private Members noti_author;
    private LocalDateTime orderDate;
    private LocalDateTime dueDate;
    private ProcessStatus processStatus;
}
