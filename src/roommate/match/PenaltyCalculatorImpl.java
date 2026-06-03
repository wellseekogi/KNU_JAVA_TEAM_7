package roommate.match;

import roommate.distance.DistanceFunction;
import roommate.distance.DistanceRegistry;
import roommate.model.Person;

import java.util.LinkedHashMap;
import java.util.Map;

public class PenaltyCalculatorImpl implements PenaltyCalculator {
    private static final double SLEEP_FACTOR = 1.5;
    private static final double WAKE_FACTOR = 1.3;
    private static final double CLEANING_FACTOR = 1.5;
    private static final double STUDY_ROOM_CONFLICT_FACTOR = 0.1;
    private static final double MBTI_FACTOR = 0.05;

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
            double distance = distanceOf(a, b, key, function);
            double penalty = (a.getImportance(key) + b.getImportance(key)) * distance * factorOf(key);
            penalties.put(key, penalty);
        }

        return penalties;
    }

    private double distanceOf(Person a, Person b, String key, DistanceFunction function) {
        if ("study".equals(key)) {
            return studyDistance(a.studyPlace, b.studyPlace);
        }
        return function.calculate(valueOf(a, key), valueOf(b, key));
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
        if ("mbti".equals(key)) {
            return person.mbtiEI == 'E' ? 1.0 : 0.0;
        }
        return 0.0;
    }

    private double factorOf(String key) {
        if ("sleep".equals(key)) {
            return SLEEP_FACTOR;
        }
        if ("wake".equals(key)) {
            return WAKE_FACTOR;
        }
        if ("cleaning".equals(key)) {
            return CLEANING_FACTOR;
        }
        if ("study".equals(key)) {
            return STUDY_ROOM_CONFLICT_FACTOR;
        }
        if ("mbti".equals(key)) {
            return MBTI_FACTOR;
        }
        return 1.0;
    }

    private double studyDistance(String a, String b) {
        if ("room".equals(a) && "room".equals(b)) {
            return 1.0;
        }
        return 0.0;
    }
}
