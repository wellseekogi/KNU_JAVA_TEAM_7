package roommate.match;

import roommate.distance.DistanceFunction;
import roommate.distance.DistanceRegistry;
import roommate.model.Person;

/**
 * 합의서 3.5 및 3.7항에 따른 페널티 계산기 구현체
 */
public class PenaltyCalculatorImpl implements PenaltyCalculator {
    private final DistanceRegistry registry;

    // 멤버 2가 생성한 DistanceRegistry를 주입받아 사용합니다 [1]
    public PenaltyCalculatorImpl(DistanceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public double calculate(Person a, Person b) {
        double totalPenalty = 0.0;

        // 합의서에서 정의한 7가지 점수 계산 대상 항목 (Hard 항목인 성별, 흡연, 알레르기 제외) [1, 3]
        String[] calculationKeys = {
            "sleep", "wake", "cleaning", "temp", "noise", "study", "mbti"
        };

        for (String key : calculationKeys) {
            // 1. 레지스트리에서 해당 항목에 맞는 거리 함수(시그모이드, 제곱 등)를 가져옴 [2, 4]
            DistanceFunction df = registry.get(key);
            if (df == null) continue;

            // 2. Person 객체에서 해당 항목의 실제 응답값 추출
            double valA = getValueByKey(a, key);
            double valB = getValueByKey(b, key);

            // 3. 거리 함수를 통한 0~1 사이의 정규화된 거리 계산 [2, 5]
            double distance = df.calculate(valA, valB);

            // 4. 합의서 공식 적용: (A의 중요도 + B의 중요도) * 거리 결과 [1]
            // 중요도 키는 합의서 2.1항의 규정("sleep", "wake" 등)을 따름 [6]
            int weightA = a.getImportance(key);
            int weightB = b.getImportance(key);
            
            totalPenalty += (weightA + weightB) * distance;
        }

        return totalPenalty;
    }

    /**
     * Person 객체의 필드와 합의된 키 문자열을 매핑하는 헬퍼 메서드 [7, 8]
     */
    private double getValueByKey(Person p, String key) {
        return switch (key) {
            case "sleep" -> (double) p.sleepTime;      // 0~28
            case "wake" -> (double) p.wakeTime;        // 5~12
            case "cleaning" -> (double) p.cleaning;    // 0~7
            case "temp" -> (double) p.temperature;     // 18~28
            case "noise" -> (double) p.noiseTolerance; // 1~5
            // 카테고리형 데이터(문자열/문자)는 비교를 위해 수치화하여 전달
            case "study" -> mapStudyPlace(p.studyPlace);
            case "mbti" -> (p.mbtiEI == 'E') ? 1.0 : 0.0;
            default -> 0.0;
        };
    }

    /**
     * studyPlace 문자열을 CategoricalDistance가 처리할 수 있는 double로 매핑 [9]
     */
    private double mapStudyPlace(String place) {
        if (place == null) return -1.0;
        return switch (place) {
            case "room" -> 0.0;
            case "library" -> 1.0;
            case "mixed" -> 2.0;
            default -> -1.0;
        };
    }
}