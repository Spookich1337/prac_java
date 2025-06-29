package src.logic;

import java.util.*;
import src.gui.GraphPanel;

class UnionFind {
    private int[] parent;
    private int[] rank;

    public UnionFind(int n) {
        parent = new int[n + 1];
        rank = new int[n + 1];
        for (int i = 0; i < n + 1; i++) parent[i] = i;
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

record State(ArrayList<GraphPanel.Edge> includedEdges, int totalWeight, GraphPanel.Edge currentEdge, boolean isIncluded) {
    State(ArrayList<GraphPanel.Edge> includedEdges, int totalWeight, GraphPanel.Edge currentEdge, boolean isIncluded) {
        // Deep copy the list to freeze the state
        this.includedEdges = new ArrayList<>(includedEdges);
        this.totalWeight = totalWeight;
        this.currentEdge = currentEdge;
        this.isIncluded = isIncluded;
    }

    @Override
    public ArrayList<GraphPanel.Edge> includedEdges() {
        return includedEdges;
    }

    @Override
    public String toString() {
        return String.format(
                "Current: %s included? %b | Total weight: %d | Included edges: %s",
                currentEdge, isIncluded, totalWeight, includedEdges);
    }
}

public class Kruskal {
    private int numVertices;
    private ArrayList<GraphPanel.Edge> edges;
    private ArrayList<State> states;

    public Kruskal(ArrayList<GraphPanel.Edge> input_edges, int numVertices) {
        this.numVertices = numVertices;
        this.edges = new ArrayList<>();
        this.states = new ArrayList<>();
        this.edges.addAll(input_edges);
    }

    public Kruskal(int numVertices) {
        this.numVertices = numVertices;
        this.edges = new ArrayList<>();
        this.states = new ArrayList<>();
    }

    public void addEdge(GraphPanel.Edge edge) {
        edges.add(edge);
    }

    public ArrayList<GraphPanel.Edge> computeMST() {
        // Sort edges by weight
        Collections.sort(edges);
        UnionFind uf = new UnionFind(numVertices);
        ArrayList<GraphPanel.Edge> mst = new ArrayList<>();
        int totalWeight = 0;

        for (GraphPanel.Edge edge : edges) {
            boolean added = false;
            // If adding this edge doesn't form a cycle
            if (uf.union(edge.v1.label, edge.v2.label)) {
                mst.add(edge);
                totalWeight += edge.weight;
                added = true;
            }
            // Record the state after considering this edge
            states.add(new State(mst, totalWeight, edge, added));
        }
        return mst;
    }

    public ArrayList<State> getStates() {
        return states;
    }
}
