package roommate.service;

import roommate.model.Person;
import java.util.HashMap;
import java.util.Map;

public class RoommateMatcher {

    // [1단계] Hard 필터링
    // 두 사람이 룸메이트가 될 수 있는 최소한의 조건을 충족하는지 검사합니다.
    // 한 항목이라도 통과하지 못하면 false를 반환합니다.
    public boolean isCompatible(Person a, Person b) {
        // 1. 성별 체크 (동성만 매칭)
        if (a.gender != b.gender) {
            return false;
        }

        // 2. 흡연 조건 체크
        // A가 흡연자인데 B가 흡연자를 거부(N)하거나, B가 흡연자인데 A가 흡연자를 거부(N)하는 경우 실패
        if (a.smoking == 'Y' && b.smokingPref == 'N') return false;
        if (b.smoking == 'Y' && a.smokingPref == 'N') return false;

        // 3. 알레르기 체크
        // 서로가 가진 알레르기 요인이 상대방의 특성과 충돌하는지 검사 (예: 한쪽이 pet인데 상대가 반려동물을 키우는 경우 등)
        // 여기서는 기본적으로 서로의 알레르기 유발 물질이 다르거나 none인 경우를 통과시키는 예시입니다.
        if (!a.allergy.equals("none") && !b.allergy.equals("none")) {
            if (!a.allergy.equals(b.allergy)) {
                // 팀 규칙에 따라 세부 조건 조절 가능
            }
        }

        return true; // 모든 하드 조건 통과
    }

    // [2단계] 거리 계산 (페널티 점수화)
    // 두 사람의 성향 차이를 구하고, 각자가 생각하는 중요도(Importance)를 가중치로 곱합니다.
    // 점수가 높을수록 '안 맞는 사이(페널티가 높은 상태)'를 의미합니다.
    // penaltyMapOut: 항목별 상세 페널티를 담아갈 빈 Map (멤버 4의 리포트 출력을 위해 채워줌)
    public double calculatePenalty(Person a, Person b, Map<String, Double> penaltyMapOut) {
        double totalPenalty = 0.0;

        // 1. 수면 시간 차이 (sleepTime: 0~28)
        int sleepDiff = Math.abs(a.sleepTime - b.sleepTime);
        double sleepPenalty = sleepDiff * (a.getImportance("sleep") + b.getImportance("sleep"));
        totalPenalty += sleepPenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("sleep", sleepPenalty);

        // 2. 기상 시간 차이 (wakeTime: 5~12)
        int wakeDiff = Math.abs(a.wakeTime - b.wakeTime);
        double wakePenalty = wakeDiff * (a.getImportance("wake") + b.getImportance("wake"));
        totalPenalty += wakePenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("wake", wakePenalty);

        // 3. 청소 주기 차이 (cleaning: 0~7)
        int cleaningDiff = Math.abs(a.cleaning - b.cleaning);
        double cleaningPenalty = cleaningDiff * (a.getImportance("cleaning") + b.getImportance("cleaning"));
        totalPenalty += cleaningPenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("cleaning", cleaningPenalty);

        // 4. 희망 온도 차이 (temperature: 18~28)
        int tempDiff = Math.abs(a.temperature - b.temperature);
        double tempPenalty = tempDiff * (a.getImportance("temp") + b.getImportance("temp"));
        totalPenalty += tempPenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("temp", tempPenalty);

        // 5. 소음 민감도 차이 (noiseTolerance: 1~5)
        int noiseDiff = Math.abs(a.noiseTolerance - b.noiseTolerance);
        double noisePenalty = noiseDiff * (a.getImportance("noise") + b.getImportance("noise"));
        totalPenalty += noisePenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("noise", noisePenalty);

        // 6. 공부 장소 선호도 (studyPlace: "room", "library", "mixed")
        // 두 사람의 선호도가 다르면 페널티를 부여 (중요도 반영)
        double studyPenalty = 0.0;
        if (!a.studyPlace.equals(b.studyPlace)) {
            studyPenalty = 2.0 * (a.getImportance("study") + b.getImportance("study"));
        }
        totalPenalty += studyPenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("study", studyPenalty);

        // 7. MBTI E/I 성향 (mbtiEI: 'E', 'I')
        // 성향이 다를 때 페널티를 부여한다고 가정
        double mbtiPenalty = 0.0;
        if (a.mbtiEI != b.mbtiEI) {
            mbtiPenalty = 1.5 * (a.getImportance("mbti") + b.getImportance("mbti"));
        }
        totalPenalty += mbtiPenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("mbti", mbtiPenalty);

        // 8. 흡연 항목 중요도 페널티 (둘 다 흡연자 조건은 통과했으나 선호도 미세 차이 반영)
        double smokingPenalty = 0.0;
        if (a.smoking != b.smoking) {
            smokingPenalty = 2.0 * (a.getImportance("smoking") + b.getImportance("smoking"));
        }
        totalPenalty += smokingPenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("smoking", smokingPenalty);

        // 9. 알레르기 항목 중요도 페널티
        double allergyPenalty = 0.0;
        if (!a.allergy.equals(b.allergy)) {
            allergyPenalty = 2.0 * (a.getImportance("allergy") + b.getImportance("allergy"));
        }
        totalPenalty += allergyPenalty;
        if (penaltyMapOut != null) penaltyMapOut.put("allergy", allergyPenalty);

        return totalPenalty;
    }
}