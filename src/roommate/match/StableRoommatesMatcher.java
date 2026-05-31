package roommate.match;

import roommate.filter.HardFilter;
import roommate.model.MatchResult;
import roommate.model.Person;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class StableRoommatesMatcher implements Matcher {
    private static final double INF = 1_000_000_000.0;
    private static final double EPS = 1e-9;
    private static final int EXACT_DP_LIMIT = 20;

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

        MatchContext context = new MatchContext(people, filter, calculator);
        List<int[]> stablePairs = tryIrvingStableRoommates(context);
        if (stablePairs != null && isStable(context, stablePairs)) {
            return toResults(context, stablePairs);
        }

        List<int[]> fallbackPairs = runFallbackMatching(context);
        if (fallbackPairs == null || fallbackPairs.size() != people.size() / 2) {
            throw new IllegalStateException("Hard 조건을 만족하는 완전 매칭을 만들 수 없습니다.");
        }
        return toResults(context, fallbackPairs);
    }

    private List<int[]> tryIrvingStableRoommates(MatchContext context) {
        List<List<Integer>> table = copyPreferences(context.preferences);
        if (!runPhaseOne(table)) {
            return null;
        }

        while (true) {
            if (hasEmptyList(table)) {
                return null;
            }
            if (allListsHaveOneChoice(table)) {
                return tableToPairs(table);
            }

            List<int[]> rotation = findRotation(table);
            if (rotation == null || rotation.isEmpty()) {
                return null;
            }
            for (int[] pair : rotation) {
                deletePair(table, pair[0], pair[1]);
            }
        }
    }

    private boolean runPhaseOne(List<List<Integer>> table) {
        int n = table.size();
        int[] heldByReceiver = new int[n];
        boolean[] inQueue = new boolean[n];
        Arrays.fill(heldByReceiver, -1);

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            queue.add(i);
            inQueue[i] = true;
        }

        while (!queue.isEmpty()) {
            int proposer = queue.remove();
            inQueue[proposer] = false;

            if (table.get(proposer).isEmpty()) {
                return false;
            }

            int receiver = table.get(proposer).get(0);
            int current = heldByReceiver[receiver];
            if (current == -1 || prefers(table, receiver, proposer, current)) {
                if (current != -1) {
                    deletePair(table, current, receiver);
                    enqueueIfPossible(table, queue, inQueue, current);
                }
                heldByReceiver[receiver] = proposer;
                if (!trimSuccessors(table, queue, inQueue, receiver, proposer)) {
                    return false;
                }
            } else {
                deletePair(table, proposer, receiver);
                enqueueIfPossible(table, queue, inQueue, proposer);
            }
        }

        return !hasEmptyList(table);
    }

    private boolean trimSuccessors(List<List<Integer>> table, Queue<Integer> queue,
                                   boolean[] inQueue, int receiver, int acceptedProposer) {
        List<Integer> receiverList = table.get(receiver);
        int acceptedIndex = receiverList.indexOf(acceptedProposer);
        if (acceptedIndex < 0) {
            return false;
        }

        List<Integer> rejected = new ArrayList<>(receiverList.subList(acceptedIndex + 1, receiverList.size()));
        for (int rejectedPerson : rejected) {
            deletePair(table, rejectedPerson, receiver);
            enqueueIfPossible(table, queue, inQueue, rejectedPerson);
        }
        return !hasEmptyList(table);
    }

    private void enqueueIfPossible(List<List<Integer>> table, Queue<Integer> queue,
                                   boolean[] inQueue, int person) {
        if (!table.get(person).isEmpty() && !inQueue[person]) {
            queue.add(person);
            inQueue[person] = true;
        }
    }

    private List<int[]> findRotation(List<List<Integer>> table) {
        int start = -1;
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).size() > 1) {
                start = i;
                break;
            }
        }
        if (start == -1) {
            return new ArrayList<>();
        }

        Map<Integer, Integer> seen = new HashMap<>();
        List<Integer> pList = new ArrayList<>();
        List<Integer> qList = new ArrayList<>();
        int p = start;

        while (!seen.containsKey(p)) {
            if (table.get(p).size() < 2) {
                return null;
            }
            seen.put(p, pList.size());
            int q = table.get(p).get(1);
            if (table.get(q).isEmpty()) {
                return null;
            }
            pList.add(p);
            qList.add(q);
            p = table.get(q).get(table.get(q).size() - 1);
        }

        int rotationStart = seen.get(p);
        List<int[]> rotation = new ArrayList<>();
        for (int i = rotationStart; i < pList.size(); i++) {
            rotation.add(new int[]{pList.get(i), qList.get(i)});
        }
        return rotation;
    }

    private boolean prefers(List<List<Integer>> table, int person, int candidate, int current) {
        List<Integer> list = table.get(person);
        int candidateIndex = list.indexOf(candidate);
        int currentIndex = list.indexOf(current);
        if (candidateIndex < 0) {
            return false;
        }
        if (currentIndex < 0) {
            return true;
        }
        return candidateIndex < currentIndex;
    }

    private void deletePair(List<List<Integer>> table, int a, int b) {
        table.get(a).remove(Integer.valueOf(b));
        table.get(b).remove(Integer.valueOf(a));
    }

    private boolean hasEmptyList(List<List<Integer>> table) {
        for (List<Integer> list : table) {
            if (list.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean allListsHaveOneChoice(List<List<Integer>> table) {
        for (List<Integer> list : table) {
            if (list.size() != 1) {
                return false;
            }
        }
        return true;
    }

    private List<int[]> tableToPairs(List<List<Integer>> table) {
        List<int[]> pairs = new ArrayList<>();
        boolean[] matched = new boolean[table.size()];
        for (int i = 0; i < table.size(); i++) {
            if (matched[i]) {
                continue;
            }
            int partner = table.get(i).get(0);
            if (partner < 0 || partner >= table.size() || table.get(partner).isEmpty()
                    || table.get(partner).get(0) != i) {
                return null;
            }
            pairs.add(new int[]{i, partner});
            matched[i] = true;
            matched[partner] = true;
        }
        return pairs;
    }

    private List<int[]> runFallbackMatching(MatchContext context) {
        if (context.people.size() <= EXACT_DP_LIMIT) {
            return runExactMinimumPenaltyMatching(context);
        }

        int[] matching = new BlossomMatcher(context.compatible, context.penalty).findPerfectMatching();
        if (matching == null) {
            return null;
        }

        List<int[]> pairs = matchingToPairs(matching);
        improveByPairSwaps(context, pairs);
        return pairs;
    }

    private List<int[]> runExactMinimumPenaltyMatching(MatchContext context) {
        long fullMask = (1L << context.people.size()) - 1L;
        Map<Long, DpChoice> memo = new HashMap<>();
        double best = solveExact(context, fullMask, memo);
        if (best >= INF / 2) {
            return null;
        }

        List<int[]> pairs = new ArrayList<>();
        long mask = fullMask;
        while (mask != 0L) {
            int first = Long.numberOfTrailingZeros(mask);
            DpChoice choice = memo.get(mask);
            if (choice == null || choice.partner < 0) {
                return null;
            }
            pairs.add(new int[]{first, choice.partner});
            mask &= ~(1L << first);
            mask &= ~(1L << choice.partner);
        }
        return pairs;
    }

    private double solveExact(MatchContext context, long mask, Map<Long, DpChoice> memo) {
        if (mask == 0L) {
            return 0.0;
        }
        DpChoice cached = memo.get(mask);
        if (cached != null) {
            return cached.cost;
        }

        int first = Long.numberOfTrailingZeros(mask);
        long rest = mask & ~(1L << first);
        double best = INF;
        int bestPartner = -1;

        for (int partner = first + 1; partner < context.people.size(); partner++) {
            long partnerBit = 1L << partner;
            if ((rest & partnerBit) == 0L || !context.compatible[first][partner]) {
                continue;
            }
            double candidate = context.penalty[first][partner]
                    + solveExact(context, rest & ~partnerBit, memo);
            if (candidate < best - EPS) {
                best = candidate;
                bestPartner = partner;
            }
        }

        DpChoice choice = new DpChoice(best, bestPartner);
        memo.put(mask, choice);
        return best;
    }

    private void improveByPairSwaps(MatchContext context, List<int[]> pairs) {
        boolean improved = true;
        int guard = context.people.size() * context.people.size();

        while (improved && guard-- > 0) {
            improved = false;
            outer:
            for (int i = 0; i < pairs.size(); i++) {
                for (int j = i + 1; j < pairs.size(); j++) {
                    int a = pairs.get(i)[0];
                    int b = pairs.get(i)[1];
                    int c = pairs.get(j)[0];
                    int d = pairs.get(j)[1];
                    double current = context.penalty[a][b] + context.penalty[c][d];

                    if (context.compatible[a][c] && context.compatible[b][d]) {
                        double alternative = context.penalty[a][c] + context.penalty[b][d];
                        if (alternative + EPS < current) {
                            pairs.set(i, new int[]{a, c});
                            pairs.set(j, new int[]{b, d});
                            improved = true;
                            break outer;
                        }
                    }

                    if (context.compatible[a][d] && context.compatible[b][c]) {
                        double alternative = context.penalty[a][d] + context.penalty[b][c];
                        if (alternative + EPS < current) {
                            pairs.set(i, new int[]{a, d});
                            pairs.set(j, new int[]{b, c});
                            improved = true;
                            break outer;
                        }
                    }
                }
            }
        }
    }

    private boolean isStable(MatchContext context, List<int[]> pairs) {
        int n = context.people.size();
        int[] partner = new int[n];
        Arrays.fill(partner, -1);
        for (int[] pair : pairs) {
            partner[pair[0]] = pair[1];
            partner[pair[1]] = pair[0];
        }

        for (int i = 0; i < n; i++) {
            if (partner[i] == -1) {
                return false;
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!context.compatible[i][j] || partner[i] == j) {
                    continue;
                }
                if (context.penalty[i][j] + EPS < context.penalty[i][partner[i]]
                        && context.penalty[j][i] + EPS < context.penalty[j][partner[j]]) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<MatchResult> toResults(MatchContext context, List<int[]> pairs) {
        List<MatchResult> results = new ArrayList<>();
        for (int[] pair : pairs) {
            Person a = context.people.get(pair[0]);
            Person b = context.people.get(pair[1]);
            results.add(createResult(a, b));
        }
        results.sort(new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult left, MatchResult right) {
                int leftId = Math.min(left.personA, left.personB);
                int rightId = Math.min(right.personA, right.personB);
                return Integer.compare(leftId, rightId);
            }
        });
        return results;
    }

    private MatchResult createResult(Person a, Person b) {
        double total = calculator.calculate(a, b);
        Map<String, Double> byItem = new LinkedHashMap<>();
        if (calculator instanceof PenaltyCalculatorImpl) {
            byItem.putAll(((PenaltyCalculatorImpl) calculator).calculateByItem(a, b));
        }
        return new MatchResult(a.id, b.id, total, byItem);
    }

    private List<List<Integer>> copyPreferences(List<List<Integer>> preferences) {
        List<List<Integer>> copy = new ArrayList<>();
        for (List<Integer> list : preferences) {
            copy.add(new ArrayList<>(list));
        }
        return copy;
    }

    private List<int[]> matchingToPairs(int[] matching) {
        List<int[]> pairs = new ArrayList<>();
        boolean[] used = new boolean[matching.length];
        for (int i = 0; i < matching.length; i++) {
            if (!used[i]) {
                int partner = matching[i];
                pairs.add(new int[]{i, partner});
                used[i] = true;
                used[partner] = true;
            }
        }
        return pairs;
    }
}
