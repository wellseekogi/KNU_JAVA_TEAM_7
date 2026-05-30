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

        MatchContext context = new MatchContext(people);
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
        results.sort(Comparator.comparingInt(result -> Math.min(result.personA, result.personB)));
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

    private static class DpChoice {
        final double cost;
        final int partner;

        DpChoice(double cost, int partner) {
            this.cost = cost;
            this.partner = partner;
        }
    }

    private class MatchContext {
        final List<Person> people;
        final boolean[][] compatible;
        final double[][] penalty;
        final List<List<Integer>> preferences;

        MatchContext(List<Person> people) {
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

    private static class BlossomMatcher {
        private final boolean[][] compatible;
        private final double[][] penalty;
        private final int n;
        private final List<List<Integer>> graph;

        private int[] match;
        private int[] parent;
        private int[] base;
        private boolean[] used;
        private boolean[] blossom;
        private Queue<Integer> queue;

        BlossomMatcher(boolean[][] compatible, double[][] penalty) {
            this.compatible = compatible;
            this.penalty = penalty;
            this.n = compatible.length;
            this.graph = buildGraph();
        }

        int[] findPerfectMatching() {
            match = new int[n];
            Arrays.fill(match, -1);
            seedGreedyMatching();

            for (int i = 0; i < n; i++) {
                if (match[i] == -1) {
                    int endpoint = findAugmentingPath(i);
                    if (endpoint != -1) {
                        augment(endpoint);
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                if (match[i] == -1) {
                    return null;
                }
            }
            return match;
        }

        private List<List<Integer>> buildGraph() {
            List<List<Integer>> result = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                List<Integer> neighbors = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if (compatible[i][j]) {
                        neighbors.add(j);
                    }
                }
                final int person = i;
                neighbors.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer left, Integer right) {
                        int byPenalty = Double.compare(penalty[person][left], penalty[person][right]);
                        if (byPenalty != 0) {
                            return byPenalty;
                        }
                        return Integer.compare(left, right);
                    }
                });
                result.add(neighbors);
            }
            return result;
        }

        private void seedGreedyMatching() {
            List<int[]> edges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (compatible[i][j]) {
                        edges.add(new int[]{i, j});
                    }
                }
            }
            edges.sort(new Comparator<int[]>() {
                @Override
                public int compare(int[] left, int[] right) {
                    int byPenalty = Double.compare(penalty[left[0]][left[1]], penalty[right[0]][right[1]]);
                    if (byPenalty != 0) {
                        return byPenalty;
                    }
                    if (left[0] != right[0]) {
                        return Integer.compare(left[0], right[0]);
                    }
                    return Integer.compare(left[1], right[1]);
                }
            });

            for (int[] edge : edges) {
                if (match[edge[0]] == -1 && match[edge[1]] == -1) {
                    match[edge[0]] = edge[1];
                    match[edge[1]] = edge[0];
                }
            }
        }

        private int findAugmentingPath(int root) {
            used = new boolean[n];
            parent = new int[n];
            base = new int[n];
            queue = new ArrayDeque<>();
            Arrays.fill(parent, -1);
            for (int i = 0; i < n; i++) {
                base[i] = i;
            }

            used[root] = true;
            queue.add(root);

            while (!queue.isEmpty()) {
                int v = queue.remove();
                for (int u : graph.get(v)) {
                    if (base[v] == base[u] || match[v] == u) {
                        continue;
                    }
                    if (u == root || (match[u] != -1 && parent[match[u]] != -1)) {
                        int currentBase = lca(v, u);
                        blossom = new boolean[n];
                        markPath(v, currentBase, u);
                        markPath(u, currentBase, v);
                        for (int i = 0; i < n; i++) {
                            if (blossom[base[i]]) {
                                base[i] = currentBase;
                                if (!used[i]) {
                                    used[i] = true;
                                    queue.add(i);
                                }
                            }
                        }
                    } else if (parent[u] == -1) {
                        parent[u] = v;
                        if (match[u] == -1) {
                            return u;
                        }
                        int matched = match[u];
                        used[matched] = true;
                        queue.add(matched);
                    }
                }
            }
            return -1;
        }

        private int lca(int a, int b) {
            boolean[] usedPath = new boolean[n];
            while (true) {
                a = base[a];
                usedPath[a] = true;
                if (match[a] == -1) {
                    break;
                }
                a = parent[match[a]];
            }
            while (true) {
                b = base[b];
                if (usedPath[b]) {
                    return b;
                }
                b = parent[match[b]];
            }
        }

        private void markPath(int vertex, int currentBase, int child) {
            while (base[vertex] != currentBase) {
                blossom[base[vertex]] = true;
                blossom[base[match[vertex]]] = true;
                parent[vertex] = child;
                child = match[vertex];
                vertex = parent[match[vertex]];
            }
        }

        private void augment(int vertex) {
            while (vertex != -1) {
                int parentVertex = parent[vertex];
                int nextVertex = parentVertex == -1 ? -1 : match[parentVertex];
                match[vertex] = parentVertex;
                if (parentVertex != -1) {
                    match[parentVertex] = vertex;
                }
                vertex = nextVertex;
            }
        }
    }
}
