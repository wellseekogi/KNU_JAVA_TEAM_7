package roommate.match;

import roommate.filter.HardFilter;
import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StableRoommatesMatcher implements Matcher {
    private final HardFilter filter;
    private final PenaltyCalculator calculator;

    public StableRoommatesMatcher(HardFilter filter, PenaltyCalculator calculator) {
        this.filter = filter;
        this.calculator = calculator;
    }

    @Override
    public List<MatchResult> match(List<Person> people) {
        if (people == null || people.isEmpty()) {
            return new ArrayList<>();
        }
        if (people.size() % 2 != 0) {
            throw new IllegalArgumentException("매칭 대상 인원은 반드시 짝수여야 합니다: " + people.size());
        }

        List<MatchResult> stableAttempt = tryMutualPreferenceMatching(people);
        if (stableAttempt.size() == people.size() / 2) {
            return stableAttempt;
        }

        List<MatchResult> fallback = runMinimumPenaltyFallback(people);
        if (fallback.size() != people.size() / 2) {
            throw new IllegalStateException("Hard 조건을 만족하는 완전 매칭을 만들 수 없습니다.");
        }
        return fallback;
    }

    private List<MatchResult> tryMutualPreferenceMatching(List<Person> people) {
        Map<Integer, List<Integer>> preferences = buildPreferences(people);
        Map<Integer, Integer> proposedIndex = new LinkedHashMap<>();
        Map<Integer, Integer> heldByReceiver = new LinkedHashMap<>();
        Set<Integer> active = new HashSet<>();
        for (Person person : people) {
            proposedIndex.put(person.id, 0);
            active.add(person.id);
        }

        int guard = people.size() * people.size() * 2;
        while (!active.isEmpty() && guard-- > 0) {
            int proposer = active.iterator().next();
            active.remove(proposer);

            List<Integer> list = preferences.get(proposer);
            int index = proposedIndex.get(proposer);
            if (list == null || index >= list.size()) {
                continue;
            }

            int receiver = list.get(index);
            proposedIndex.put(proposer, index + 1);
            Integer current = heldByReceiver.get(receiver);
            if (current == null || prefers(preferences.get(receiver), proposer, current)) {
                heldByReceiver.put(receiver, proposer);
                if (current != null) {
                    active.add(current);
                }
            } else {
                active.add(proposer);
            }
        }

        List<MatchResult> results = new ArrayList<>();
        Set<Integer> matched = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : heldByReceiver.entrySet()) {
            int receiver = entry.getKey();
            int proposer = entry.getValue();
            if (matched.contains(receiver) || matched.contains(proposer)) {
                continue;
            }
            Person a = findPerson(people, proposer);
            Person b = findPerson(people, receiver);
            if (a != null && b != null && filter.isCompatible(a, b)) {
                results.add(createResult(a, b));
                matched.add(a.id);
                matched.add(b.id);
            }
        }
        return results;
    }

    private Map<Integer, List<Integer>> buildPreferences(List<Person> people) {
        Map<Integer, List<Integer>> preferences = new LinkedHashMap<>();
        for (Person a : people) {
            List<ScoredPerson> scores = new ArrayList<>();
            for (Person b : people) {
                if (a.id == b.id || !filter.isCompatible(a, b)) {
                    continue;
                }
                scores.add(new ScoredPerson(b.id, calculator.calculate(a, b)));
            }
            scores.sort(Comparator.comparingDouble(item -> item.penalty));
            List<Integer> ids = new ArrayList<>();
            for (ScoredPerson score : scores) {
                ids.add(score.id);
            }
            preferences.put(a.id, ids);
        }
        return preferences;
    }

    private boolean prefers(List<Integer> preferences, int candidate, int current) {
        if (preferences == null) {
            return false;
        }
        int candidateIndex = preferences.indexOf(candidate);
        int currentIndex = preferences.indexOf(current);
        if (candidateIndex < 0) {
            return false;
        }
        if (currentIndex < 0) {
            return true;
        }
        return candidateIndex < currentIndex;
    }

    private List<MatchResult> runMinimumPenaltyFallback(List<Person> people) {
        List<PairCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < people.size(); i++) {
            for (int j = i + 1; j < people.size(); j++) {
                Person a = people.get(i);
                Person b = people.get(j);
                if (filter.isCompatible(a, b)) {
                    candidates.add(new PairCandidate(a, b, calculator.calculate(a, b)));
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(candidate -> candidate.penalty));

        List<MatchResult> results = new ArrayList<>();
        Set<Integer> matched = new HashSet<>();
        for (PairCandidate candidate : candidates) {
            if (matched.contains(candidate.a.id) || matched.contains(candidate.b.id)) {
                continue;
            }
            results.add(createResult(candidate.a, candidate.b));
            matched.add(candidate.a.id);
            matched.add(candidate.b.id);
        }
        return results;
    }

    private MatchResult createResult(Person a, Person b) {
        double total = calculator.calculate(a, b);
        Map<String, Double> byItem = null;
        if (calculator instanceof PenaltyCalculatorImpl) {
            byItem = ((PenaltyCalculatorImpl) calculator).calculateByItem(a, b);
        }
        return new MatchResult(a.id, b.id, total, byItem);
    }

    private Person findPerson(List<Person> people, int id) {
        for (Person person : people) {
            if (person.id == id) {
                return person;
            }
        }
        return null;
    }

    private static class ScoredPerson {
        final int id;
        final double penalty;

        ScoredPerson(int id, double penalty) {
            this.id = id;
            this.penalty = penalty;
        }
    }

    private static class PairCandidate {
        final Person a;
        final Person b;
        final double penalty;

        PairCandidate(Person a, Person b, double penalty) {
            this.a = a;
            this.b = b;
            this.penalty = penalty;
        }
    }
}
