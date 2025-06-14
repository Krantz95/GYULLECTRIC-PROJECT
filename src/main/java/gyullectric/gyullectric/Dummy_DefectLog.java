package gyullectric.gyullectric;

import gyullectric.gyullectric.dto.DefectLogDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

// 불량예측 데이터 시뮬레이션용 더미
// 정상 -> 위험 순으로 더미 재생성 필요
//@Component
public class Dummy_DefectLog {

    // ---------------------------차트 1-----------------------------------
    private final Random random = new Random();

    // 차트1 - 랜덤생성
    public DefectLogDto getDummySensorData2() {
        DefectLogDto dto = new DefectLogDto();
        //  [1] 센서 정상 범위 기본값 지정
        dto.setPressure(random.nextInt(280, 350));        // 압력: 280~349
        dto.setUpperTemp(random.nextInt(85, 120));        // 상단 금형 온도: 85~119
        dto.setLowerTemp(random.nextInt(180, 230));       // 하단 금형 온도: 180~229
        // [2] 강제로 "에러 조건"을 유도하여 테스트에 활용 (조건문 확률 조정 가능)
        // 현재는 각각 약 10% 확률로 에러 발생
        if (random.nextInt(2) == 0) {  // 50% 확률로 에러 압력 생성
            dto.setPressure(random.nextInt(180, 290));    // 기준치(299) 미달 압력
        }
        if (random.nextInt(2) == 0) {  // 50% 확률로 에러 온도 생성
            dto.setUpperTemp(random.nextInt(70, 85));     // 기준치(94) 미달 상단 금형 온도
        }
        if (random.nextInt(2) == 0) {  // 50% 확률로 에러 온도 생성
            dto.setLowerTemp(random.nextInt(150, 180));   // 기준치(193) 미달 하단 금형 온도
        }
        return dto;
    }

    // ---------------------------차트 2-----------------------------------

    // 차트 2 - 최근 20초간의 용접 출력 시퀀스 (랜덤생성)
    public List<Double> getDummyWeldingSequence() {
        List<Double> sequence = new ArrayList<>();
        Random random = new Random();

        // 🎯 0~9 사이 숫자 뽑아서 모드 결정
        int mode = random.nextInt(10); // 0~9
        double center;
        String modeLabel;

        if (mode < 2) {
            // 🔴 위험 (20%) → 출력 평균 낮음
            center = 1000;
            modeLabel = "🔴 위험";
        } else if (mode < 5) {
            // 🟡 주의 (30%) → 출력 평균 중간
            center = 1450;
            modeLabel = "🟡 주의";
        } else {
            // 🟢 정상 (50%) → 출력 평균 높음
            center = 1800;
            modeLabel = "🟢 정상";
        }

        double offset = 100; // ±100 범위 내 퍼짐

        for (int i = 0; i < 20; i++) {
            double value = center + (Math.random() * 2 - 1) * offset;
            sequence.add(Math.round(value * 10) / 10.0);
        }

        System.out.println("📡 생성된 용접 시퀀스 (" + modeLabel + " 모드): " + sequence);
        return sequence;
    }
}
