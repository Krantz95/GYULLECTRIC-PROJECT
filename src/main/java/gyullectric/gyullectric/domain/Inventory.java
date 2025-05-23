package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_name", length = 50, nullable = false)
    private PartName partName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private int quantity = 0;

    @Column(name = "ordered_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime orderedAt;

    public Inventory() {}

    // getters, setters 생략
}
