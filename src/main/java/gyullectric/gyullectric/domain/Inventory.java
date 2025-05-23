package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_name", length = 50, nullable = false)
    private PartName partName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Supplier supplier;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "ordered_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime orderedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Members members;

}
