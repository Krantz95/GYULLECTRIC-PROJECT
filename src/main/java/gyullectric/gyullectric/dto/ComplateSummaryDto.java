package gyullectric.gyullectric.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplateSummaryDto {

    private String productName;   // 제품명
    private int totalOrderQty;    // 해당 제품 주문 총 수량 (공정 개수로 나눈 값)
    private long finishedCount;   // 완제품(모든 공정 OK) 수량
    private double achievementRate;  // 달성률 (완제품 / 주문수 * 100)
}
