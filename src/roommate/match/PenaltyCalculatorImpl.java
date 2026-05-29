package roommate.match;

import roommate.distance.DistanceFunction;
import roommate.distance.DistanceRegistry;
import roommate.model.Person;

import java.util.LinkedHashMap;
import java.util.Map;

public class PenaltyCalculatorImpl implements PenaltyCalculator {
    private static final String[] KEYS = {
            "sleep", "wake", "cleaning", "temp", "noise", "study", "mbti"
    };

    private final DistanceRegistry registry;

    public PenaltyCalculatorImpl(DistanceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public double calculate(Person a, Person b) {
        double total = 0.0;
        for (double penalty : calculateByItem(a, b).values()) {
            total += penalty;
        }
        return total;
    }

    public Map<String, Double> calculateByItem(Person a, Person b) {
        Map<String, Double> penalties = new LinkedHashMap<>();

        for (String key : KEYS) {
            DistanceFunction function = registry.get(key);
            if (function == null) {
                continue;
            }
            double distance = function.calculate(valueOf(a, key), valueOf(b, key));
            double penalty = (a.getImportance(key) + b.getImportance(key)) * distance;
            penalties.put(key, penalty);
        }

        return penalties;
    }

    private double valueOf(Person person, String key) {
        if ("sleep".equals(key)) {
            return person.sleepTime;
        }
        if ("wake".equals(key)) {
            return person.wakeTime;
        }
        if ("cleaning".equals(key)) {
            return person.cleaning;
        }
        if ("temp".equals(key)) {
            return person.temperature;
        }
        if ("noise".equals(key)) {
            return person.noiseTolerance;
        }
        if ("study".equals(key)) {
            return studyValue(person.studyPlace);
        }
        if ("mbti".equals(key)) {
            return person.mbtiEI == 'E' ? 1.0 : 0.0;
        }
        return 0.0;
    }

    private double studyValue(String value) {
        if ("room".equals(value)) {
            return 0.0;
        }
        if ("library".equals(value)) {
            return 1.0;
        }
        if ("mixed".equals(value)) {
            return 2.0;
        }
        return -1.0;
    }
}
