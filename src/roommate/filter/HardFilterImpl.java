package roommate.filter;

import roommate.model.Person;

public class HardFilterImpl implements HardFilter {
    @Override
    public boolean isCompatible(Person a, Person b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.gender != b.gender) {
            return false;
        }
        if (rejectsSmoker(a) && isSmoker(b)) {
            return false;
        }
        if (rejectsSmoker(b) && isSmoker(a)) {
            return false;
        }
        return true;
    }

    private boolean isSmoker(Person person) {
        return person.smoking == 'Y';
    }

    private boolean rejectsSmoker(Person person) {
        return person.smokingPref == 'N';
    }

    // allergy는 합의서상 Hard 항목이지만, 현재 CSV에는 상대방의 알레르기 유발 요인 정보가 없다.
    // 따라서 MVP에서는 성별/흡연만 차단하고, allergy 충돌은 추후 필드가 추가될 때 확장한다.
}
