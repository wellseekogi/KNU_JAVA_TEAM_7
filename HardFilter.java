package roommate.filter;

import roommate.model.Person;

public interface HardFilter {
    /**
     * 두 사람이 매칭 가능한지 판정한다.
     * Hard 조건(성별, 흡연 선호, 알레르기 등)을 모두 통과해야 true.
     */
    boolean isCompatible(Person a, Person b);
}