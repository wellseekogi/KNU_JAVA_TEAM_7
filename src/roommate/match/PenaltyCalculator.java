package roommate.match;

import roommate.model.Person;

public interface PenaltyCalculator {
    double calculate(Person a, Person b);
}
