package roommate.model;

import java.util.HashMap;
import java.util.Map;

public class Person {
    public int id;

    public char gender;
    public char smoking;
    public char smokingPref;
    public String allergy;

    public int sleepTime;
    public int wakeTime;
    public int cleaning;
    public int temperature;
    public int noiseTolerance;

    public String studyPlace;
    public char mbtiEI;

    public Map<String, Integer> importance = new HashMap<>();

    public int getImportance(String key) {
        if (importance == null) {
            return 5;
        }
        return importance.getOrDefault(key, 5);
    }
}
