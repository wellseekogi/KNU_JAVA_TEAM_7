package roommate.match;

import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.List;

public interface Matcher {
    List<MatchResult> match(List<Person> people);
}
