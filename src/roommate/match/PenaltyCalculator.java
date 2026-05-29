package roommate.match;
 
import roommate.model.Person;
 
public interface PenaltyCalculator {
    /**
     * 두 사람의 총 페널티를 계산한다.
     * 공식: Σ (wA + wB) × distance(vA, vB)
     * @return 총 페널티 (낮을수록 좋은 매칭)
     */
    double calculate(Person a, Person b);
}
