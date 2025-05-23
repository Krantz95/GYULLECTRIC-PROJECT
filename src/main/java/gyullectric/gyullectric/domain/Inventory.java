package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    @Setter
    private PartName partName;
    @Enumerated(EnumType.STRING)
    @Setter
    private Supplier supplier;
    @Setter
    private Integer quantity;
    @Setter
    private LocalDateTime orderAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Members members;

//        @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
//        private List<OrderList> orderLists = new ArrayList<>();



}
