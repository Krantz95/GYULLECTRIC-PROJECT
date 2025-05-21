package gyullectric.gyullectric.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "password")
public class Members {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;
    @Setter
    private String loginId;
    @Setter
    private String name;
    @Setter
    private String password;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "position_name")
    private PositionName positionName;
    @Setter
    private String phone;
    @Setter
    private LocalDateTime createDate;



}
