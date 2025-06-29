import java.util.*;

/**
 * Represents an undirected edge with a weight.
 */
class AlgEdge implements Comparable<AlgEdge> {
    int u, v, weight;

    public AlgEdge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    @Override
    public int compareTo(AlgEdge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return String.format("(%d--%d, w=%d)", u, v, weight);
    }
}

/**
 * Union-Find (Disjoint Set) data structure for cycle detection.
 */
class UnionFind {
    private int[] parent;
    private int[] rank;

    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return false;
        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }
        return true;
    }
}

/**
 * Stores a snapshot of the algorithm at each step.
 */
record State(List<AlgEdge> includedEdges, int totalWeight, AlgEdge currentEdge, boolean isIncluded) {
    State(List<AlgEdge> includedEdges, int totalWeight, AlgEdge currentEdge, boolean isIncluded) {
        // Deep copy the list to freeze the state
        this.includedEdges = new ArrayList<>(includedEdges);
        this.totalWeight = totalWeight;
        this.currentEdge = currentEdge;
        this.isIncluded = isIncluded;
    }

    @Override
    public List<AlgEdge> includedEdges() {
        return Collections.unmodifiableList(includedEdges);
    }

    @Override
    public String toString() {
        return String.format(
                "Current: %s included? %b | Total weight: %d | Included edges: %s",
                currentEdge, isIncluded, totalWeight, includedEdges);
    }
}

/**
 * Main class implementing Kruskal's algorithm with state tracking.
 */
public class Kruskal {
    private int numVertices;
    private List<AlgEdge> edges;
    private List<State> states;

    public Kruskal(int numVertices) {
        this.numVertices = numVertices;
        this.edges = new ArrayList<>();
        this.states = new ArrayList<>();
    }

    public void addEdge(int u, int v, int weight) {
        edges.add(new AlgEdge(u, v, weight));
    }

    /**
     * Executes Kruskal's algorithm and records states.
     */
    public List<AlgEdge> computeMST() {
        // Sort edges by weight
        Collections.sort(edges);
        UnionFind uf = new UnionFind(numVertices);
        List<AlgEdge> mst = new ArrayList<>();
        int totalWeight = 0;

        for (AlgEdge edge : edges) {
            boolean added = false;
            // If adding this edge doesn't form a cycle
            if (uf.union(edge.u, edge.v)) {
                mst.add(edge);
                totalWeight += edge.weight;
                added = true;
            }
            // Record the state after considering this edge
            states.add(new State(mst, totalWeight, edge, added));
        }
        return mst;
    }

    /**
     * Returns all recorded states.
     */
    public List<State> getStates() {
        return Collections.unmodifiableList(states);
    }
    public static void main(String[] args) {
        Kruskal algo = new Kruskal(6);
        algo.addEdge(0, 1, 4);
        algo.addEdge(0, 2, 4);
        algo.addEdge(1, 2, 2);
        algo.addEdge(1, 3, 5);
        algo.addEdge(2, 3, 5);
        algo.addEdge(2, 4, 3);
        algo.addEdge(3, 4, 1);
        algo.addEdge(3, 5, 6);
        algo.addEdge(4, 5, 7);

        algo.computeMST();
        for (State s : algo.getStates()) {
            System.out.println(s);
        }
    }
}

