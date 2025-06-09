package gyullectric.gyullectric;

import gyullectric.gyullectric.dto.DefectLogDto;
import org.springframework.stereotype.Component;

import java.util.Random;

// 불량예측 데이터 시뮬레이션용 더미 데이터 생성 유틸 클래스
@Component
public class Dummy_DefectLog {

    private final Random random = new Random();

    // 랜덤으로 센서 데이터를 생성하여 DefectLogDto 형태로 반환
    public DefectLogDto getDummySensorData() {
        DefectLogDto dto = new DefectLogDto();

        // 정상 범위 내 값 설정
        dto.setPressure(random.nextInt(280, 350));         // 압력: 280~350
        dto.setUpperTemp(random.nextInt(85, 120));         // 상단 금형 온도: 85~120
        dto.setLowerTemp(random.nextInt(180, 230));        // 하단 금형 온도: 180~230
        dto.setWeldingOutput(random.nextInt(80, 100));     // 용접 출력: 80~100

        // 10% 확률로 각각의 항목에 대해 에러 상황을 일부러 발생시킴

        if (random.nextInt(10) == 0) {
            // 압력을 기준 이하로 낮게 설정 (에러 유도)
            dto.setPressure(random.nextInt(200, 280));
        }
        if (random.nextInt(10) == 0) {
            // 상단 금형 온도를 기준 미달로 설정
            dto.setUpperTemp(random.nextInt(70, 85));
        }
        if (random.nextInt(10) == 0) {
            // 하단 금형 온도를 기준 미달로 설정
            dto.setLowerTemp(random.nextInt(150, 180));
        }
        if (random.nextInt(10) == 0) {
            // 용접 출력을 기준 미달로 설정
            dto.setWeldingOutput(random.nextInt(50, 80));
        }

        return dto;
    }
}
