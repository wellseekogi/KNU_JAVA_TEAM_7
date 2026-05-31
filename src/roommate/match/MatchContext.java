package roommate.match;

import roommate.filter.HardFilter;
import roommate.model.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class MatchContext {
    private static final double INF = 1_000_000_000.0;

    final List<Person> people;
    final boolean[][] compatible;
    final double[][] penalty;
    final List<List<Integer>> preferences;

    MatchContext(List<Person> people, HardFilter filter, PenaltyCalculator calculator) {
        this.people = people;
        int n = people.size();
        this.compatible = new boolean[n][n];
        this.penalty = new double[n][n];
        this.preferences = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Arrays.fill(penalty[i], INF);
            preferences.add(new ArrayList<Integer>());
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Person a = people.get(i);
                Person b = people.get(j);
                if (filter.isCompatible(a, b)) {
                    compatible[i][j] = true;
                    compatible[j][i] = true;
                    double value = calculator.calculate(a, b);
                    penalty[i][j] = value;
                    penalty[j][i] = value;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            List<Integer> ordered = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if (compatible[i][j]) {
                    ordered.add(j);
                }
            }
            final int person = i;
            ordered.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer left, Integer right) {
                    int byPenalty = Double.compare(penalty[person][left], penalty[person][right]);
                    if (byPenalty != 0) {
                        return byPenalty;
                    }
                    return Integer.compare(people.get(left).id, people.get(right).id);
                }
            });
            preferences.set(i, ordered);
        }
    }
}
