package roommate.match;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

class BlossomMatcher {
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
