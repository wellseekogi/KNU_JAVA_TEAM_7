package roommate.filter;

import roommate.model.Person;

public class HardFilterImpl implements HardFilter {

    @Override
    public boolean isCompatible(Person a, Person b) {
        if (a == null || b == null) {
            return false;
        }

        // 1) 성별 필터링: 성별이 다르면 매칭 불가
        if (a.gender != b.gender) {
            return false;
        }

        // 2) 흡연 필터링: 비흡연 룸메이트를 원하는데(N) 상대가 흡연자(Y)면 불가
        if (rejectsSmoker(a) && isSmoker(b)) {
            return false;
        }
        if (rejectsSmoker(b) && isSmoker(a)) {
            return false;
        }

        // 3) 알레르기 필터링: 한쪽의 알레르기 성향이 상대방과 충돌하면 불가
        if (allergyConflicts(a, b) || allergyConflicts(b, a)) {
            return false;
        }

        // 모든 조건 통과
        return true;
    }

    /** 본인이 흡연자인지 여부 */
    private boolean isSmoker(Person p) {
        return p.smoking == 'Y';
    }

    /** 흡연 룸메이트를 거부하는지 여부 */
    private boolean rejectsSmoker(Person p) {
        return p.smokingPref == 'N';
    }

    /**
     * 두 사람의 알레르기 성향이 충돌하는지 검사한다.
     * - 한쪽이라도 "none" 이면 충돌하지 않는다.
     */
    private boolean allergyConflicts(Person sufferer, Person trigger) {
        String allergyA = normalize(sufferer.allergy);
        String allergyB = normalize(trigger.allergy);

        if (allergyA.equals("none") || allergyB.equals("none")) {
            return false;
        }
        
        // 서로 다른 알레르기 유발 환경을 가졌을 때 충돌로 판정 (합의서 필드에 맞게 수정)
        return !allergyA.equals(allergyB);
    }

    /** null/대소문자/공백에 안전하게 정규화 */
    private String normalize(String value) {
        return (value == null) ? "none" : value.trim().toLowerCase();
    }
}