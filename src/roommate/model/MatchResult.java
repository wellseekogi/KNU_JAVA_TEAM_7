package roommate.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class MatchResult {
    public int personA;
    public int personB;
    public double totalPenalty;
    public Map<String, Double> penaltyByItem;

    public MatchResult(int personA, int personB, double totalPenalty) {
        this.personA = personA;
        this.personB = personB;
        this.totalPenalty = totalPenalty;
    }

    public MatchResult(int personA, int personB, double totalPenalty, Map<String, Double> penaltyByItem) {
        this(personA, personB, totalPenalty);
        this.penaltyByItem = penaltyByItem == null ? null : new LinkedHashMap<>(penaltyByItem);
    }
}
