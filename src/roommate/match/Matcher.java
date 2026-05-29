package roommate.match;
 
import java.util.List;
import roommate.model.Person;
import roommate.model.MatchResult;
 
public interface Matcher {
    /**
     * 주어진 사람들을 짝지어 매칭 결과를 반환한다.
     * @param people 매칭 대상 응답자들 (짝수여야 함)
     * @return 매칭된 페어 목록 (N/2 개)
     * @throws IllegalStateException 매칭 불가능한 경우
     */
    List<MatchResult> match(List<Person> people);
}
