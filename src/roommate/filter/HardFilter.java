package roommate.filter;

import roommate.model.Person;

public interface HardFilter {
    boolean isCompatible(Person a, Person b);
}
