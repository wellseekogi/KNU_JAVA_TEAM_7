package roommate.model;

import java.util.Map; // 여기에 빨간 줄이 간다면 1단계 Build Path 설정을 다시 해주어야 합니다.

public class Person {
    // ===== 식별자 =====
    public int id;

    // ===== Hard 항목 (필터링에 사용) =====
    public char gender;          // 'M' or 'F'
    public char smoking;         // 'Y' or 'N'
    public char smokingPref;     // 'Y', 'N', 'X' (X = 상관없음)
    public String allergy;       // "none", "dust", "pet", "food"

    // ===== Compatibility 항목 (거리 계산에 사용) =====
    public int sleepTime;        // 0~28
    public int wakeTime;         // 5~12
    public int cleaning;         // 0~7
    public int temperature;      // 18~28
    public int noiseTolerance;   // 1~5

    // ===== Complementary 항목 (보너스 점수) =====
    public String studyPlace;    // "room", "library", "mixed"
    public char mbtiEI;          // 'E' or 'I'

    // ===== 중요도 (1~10) =====
    public Map<String, Integer> importance;

    public int getImportance(String key) {
        return importance.getOrDefault(key, 5);
    }
}