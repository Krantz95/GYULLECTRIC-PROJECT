package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_list")
public class OrderList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", nullable = false)
    private ProductName productName;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noti_author_id")
    private Members notiAuthor;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status", nullable = false)
    private ProcessStatus processStatus;
}
