package roommate.distance;

import java.util.HashMap;
import java.util.Map;

public class DistanceRegistry {
    private final Map<String, DistanceFunction> map = new HashMap<>();

    public DistanceRegistry() {
        map.put("sleep", new SigmoidDistance(1.5, 2.0, 12.0));
        map.put("wake", new SigmoidDistance(1.5, 1.5, 7.0));
        map.put("cleaning", new SquaredDistance(7.0));
        map.put("temp", new AbsoluteDistance(10.0));
        map.put("noise", new SquaredDistance(4.0));
        map.put("study", new CategoricalDistance());
        map.put("mbti", new CategoricalDistance());
    }

    public DistanceFunction get(String key) {
        return map.get(key);
    }
}
