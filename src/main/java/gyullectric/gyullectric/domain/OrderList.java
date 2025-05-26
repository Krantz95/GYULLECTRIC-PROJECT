package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_list")
public class OrderList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_name", nullable = false)
    private ProductName productName;

    @Column(nullable = false)
    private Integer quantity;
//주문자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Members members;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Setter
    @Column(name = "process_status", nullable = false)
    private ProcessStatus processStatus;

    @Builder.Default
    @OneToMany(mappedBy = "orderList", cascade = CascadeType.ALL, orphanRemoval = true)
//    부모가 자식을 버림. 부모가 삭제될때 연관관계가 끊긴 특정자식만.
    private List<OrderListInventory> orderListInventoryLists = new ArrayList<>();

    public void addOrderListInvetory(OrderListInventory jm){
        orderListInventoryLists.add(jm); //자식 리스트에 추가 : jobOrder객체가 가지고 있는 자식 리스트에 jm(자식객체를 추가)
//        이걸로 부모 => 자식 방향이 연결
        jm.setOrderList(this); //자식의 부모 설정 : 자식 객체인 JobOrderMaterial에 부모 JobOrder를 설정한다
//        이걸로 자식 => 부모 방향이 연결

    }

}
