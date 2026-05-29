package roommate.match;

import roommate.filter.HardFilter;
import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.*;

/**
 * 멤버 3 담당: Irving's Algorithm을 이용한 안정 매칭 구현 및 백업 알고리즘 제공
 */
public class StableRoommatesMatcher implements Matcher {
    private final HardFilter filter;
    private final PenaltyCalculator calculator;

    public StableRoommatesMatcher(HardFilter filter, PenaltyCalculator calculator) {
        this.filter = filter;
        this.calculator = calculator;
    }

    @Override
    public List<MatchResult> match(List<Person> people) {
        // 1. 인원수 체크 (합의서 2.6항: 반드시 짝수여야 함)
        if (people.size() % 2 != 0) {
            throw new IllegalArgumentException("매칭 인원은 반드시 짝수여야 합니다. 현재 인원: " + people.size());
        }

        // 2. 선호 리스트 생성 (기획서 2.4항 1단계)
        // 각 사람이 다른 모든 사람에 대해 페널티가 낮은 순으로 정렬된 리스트를 가짐
        Map<Integer, LinkedList<Integer>> prefLists = buildPreferenceLists(people);

        try {
            // 3. Irving's Algorithm 수행
            return runIrvingAlgorithm(people, prefLists);
        } catch (Exception e) {
            // 4. 안정 매칭 실패 시 Fallback (기획서 2.4항: 헝가리안 알고리즘 기반 최소 페널티 매칭)
            return runFallbackMatching(people);
        }
    }

    /**
     * 모든 참여자의 선호 리스트를 구축합니다.
     */
    private Map<Integer, LinkedList<Integer>> buildPreferenceLists(List<Person> people) {
        Map<Integer, LinkedList<Integer>> prefLists = new HashMap<>();
        
        for (Person a : people) {
            List<ScoredParticipant> scores = new ArrayList<>();
            for (Person b : people) {
                if (a.id == b.id) continue;

                // HardFilter 적용 (합의서 3.2항): 타협 불가 항목 위반 시 거리를 최대로 설정
                double penalty;
                if (filter.isCompatible(a, b)) {
                    penalty = calculator.calculate(a, b);
                } else {
                    penalty = Double.MAX_VALUE; // 매칭 순위에서 뒤로 밀려나게 함
                }
                scores.add(new ScoredParticipant(b.id, penalty));
            }
            
            // 페널티 낮은 순(선호도 높은 순) 정렬
            scores.sort(Comparator.comparingDouble(s -> s.penalty));
            
            LinkedList<Integer> sortedIds = new LinkedList<>();
            for (ScoredParticipant sp : scores) {
                sortedIds.add(sp.id);
            }
            prefLists.put(a.id, sortedIds);
        }
        return prefLists;
    }

    /**
     * Irving's Algorithm 실구현 (Phase 1, 2, 3)
     */
    private List<MatchResult> runIrvingAlgorithm(List<Person> people, Map<Integer, LinkedList<Integer>> prefs) {
        Map<Integer, Integer> holds = new HashMap<>(); // 현재 제안을 유지(Hold)하고 있는 상태
        Queue<Integer> freePeople = new LinkedList<>(prefs.keySet());

        // Phase 1: Proposals
        while (!freePeople.isEmpty()) {
            int proposerId = freePeople.poll();
            LinkedList<Integer> proposerPrefs = prefs.get(proposerId);

            if (proposerPrefs.isEmpty()) throw new RuntimeException("Stable matching impossible");

            int receiverId = proposerPrefs.getFirst();
            LinkedList<Integer> receiverPrefs = prefs.get(receiverId);

            // 받는 사람이 proposer를 수락 가능한지(자신에게 제안한 다른 사람보다 선호하는지) 확인
            int currentHoldId = holds.getOrDefault(receiverId, -1);
            if (currentHoldId == -1 || isBetterPreference(receiverPrefs, proposerId, currentHoldId)) {
                holds.put(receiverId, proposerId);
                if (currentHoldId != -1) freePeople.add(currentHoldId); // 기존 홀더는 다시 자유 상태가 됨
                
                // Phase 2: 대칭적 리스트 축소
                reducePreferenceLists(prefs, receiverId, proposerId);
            } else {
                // 제안 거절 시 다음 선호도로 이동
                proposerPrefs.removeFirst();
                freePeople.add(proposerId);
            }
        }

        // Phase 3: Rotation Elimination (순환 제거)
        while (hasMultiplePreferences(prefs)) {
            List<Integer> rotation = findRotation(prefs);
            eliminateRotation(prefs, rotation);
        }

        return convertToMatchResults(prefs, people);
    }

    private void reducePreferenceLists(Map<Integer, LinkedList<Integer>> prefs, int receiver, int proposer) {
        LinkedList<Integer> receiverPrefs = prefs.get(receiver);
        // receiver의 리스트에서 proposer보다 뒤에 있는 사람들은 모두 제거 (대칭적으로 상대방 리스트에서도 receiver 제거)
        int index = receiverPrefs.indexOf(proposer);
        List<Integer> toRemove = new ArrayList<>(receiverPrefs.subList(index + 1, receiverPrefs.size()));
        
        for (int rejectedId : toRemove) {
            receiverPrefs.remove((Integer) rejectedId);
            prefs.get(rejectedId).remove((Integer) receiver);
        }
    }

    private boolean isBetterPreference(List<Integer> pref, int newId, int currentId) {
        return pref.indexOf(newId) < pref.indexOf(currentId);
    }

    private boolean hasMultiplePreferences(Map<Integer, LinkedList<Integer>> prefs) {
        return prefs.values().stream().anyMatch(list -> list.size() > 1);
    }

    private List<Integer> findRotation(Map<Integer, LinkedList<Integer>> prefs) {
        List<Integer> p = new ArrayList<>();
        List<Integer> q = new ArrayList<>();
        int curr = prefs.keySet().stream().filter(id -> prefs.get(id).size() > 1).findFirst().get();
        
        while (!p.contains(curr)) {
            p.add(curr);
            q.add(prefs.get(curr).get(1)); // 두 번째 선호인
            curr = prefs.get(q.get(q.size()-1)).getLast(); // 마지막 선호인
        }
        return p.subList(p.indexOf(curr), p.size());
    }

    private void eliminateRotation(Map<Integer, LinkedList<Integer>> prefs, List<Integer> rotation) {
        for (int i = 0; i < rotation.size(); i++) {
            int a = rotation.get(i);
            int b = prefs.get(a).get(1);
            // a가 b를 거절하게 함으로써 순환 제거
            prefs.get(a).remove(Integer.valueOf(b));
            prefs.get(b).remove(Integer.valueOf(a));
        }
    }

    /**
     * Irving 실패 시 실행되는 백업 매칭 (그리디 페널티 최소화 전략)
     */
    private List<MatchResult> runFallbackMatching(List<Person> people) {
        List<MatchResult> results = new ArrayList<>();
        Set<Integer> matched = new HashSet<>();
        
        for (Person a : people) {
            if (matched.contains(a.id)) continue;
            
            Person bestPartner = null;
            double minPenalty = Double.MAX_VALUE;

            for (Person b : people) {
                if (a.id == b.id || matched.contains(b.id)) continue;
                if (!filter.isCompatible(a, b)) continue;

                double penalty = calculator.calculate(a, b);
                if (penalty < minPenalty) {
                    minPenalty = penalty;
                    bestPartner = b;
                }
            }

            if (bestPartner != null) {
                matched.add(a.id);
                matched.add(bestPartner.id);
                results.add(new MatchResult(a.id, bestPartner.id, minPenalty));
            }
        }
        return results;
    }

    private List<MatchResult> convertToMatchResults(Map<Integer, LinkedList<Integer>> prefs, List<Person> people) {
        List<MatchResult> results = new ArrayList<>();
        Set<Integer> processed = new HashSet<>();
        
        for (Map.Entry<Integer, LinkedList<Integer>> entry : prefs.entrySet()) {
            int aId = entry.getKey();
            if (processed.contains(aId)) continue;
            
            int bId = entry.getValue().getFirst();
            Person a = findPerson(people, aId);
            Person b = findPerson(people, bId);
            
            results.add(new MatchResult(aId, bId, calculator.calculate(a, b)));
            processed.add(aId);
            processed.add(bId);
        }
        return results;
    }

    private Person findPerson(List<Person> people, int id) {
        return people.stream().filter(p -> p.id == id).findFirst().orElse(null);
    }

    private static class ScoredParticipant {
        int id;
        double penalty;
        ScoredParticipant(int id, double penalty) { this.id = id; this.penalty = penalty; }
    }
}