package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_list")
public class OrderList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", length = 50, nullable = false)
    private ProductName productName;

    @Column(nullable = false)
    private int quantity = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noti_author", nullable = false)
    private Members notiAuthor;

    @Column(name = "order_date", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime orderDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", length = 20, nullable = false)
    private ProcessStatus processStatus = ProcessStatus.PENDING;

    public OrderList() {}

    // Getters and setters 생략
}
