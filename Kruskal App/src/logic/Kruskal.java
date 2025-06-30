package src.logic;

import java.util.*;
import src.gui.GraphPanel;

// Класс для поиска пути и детекции цикла
class CycleDetector {
    /**
     * Ищет простой путь между start и target.
     * Возвращает список ребер пути или пустой список если пути нет.
     */
    public static List<GraphPanel.Edge> findPath(
            Collection<GraphPanel.Edge> edges,
            int start,
            int target
    ) {
        // Строим список смежности
        Map<Integer, List<GraphPanel.Edge>> adj = new HashMap<>();
        for (GraphPanel.Edge e : edges) {
            adj.computeIfAbsent(e.v1.label, k -> new ArrayList<>()).add(e);
            adj.computeIfAbsent(e.v2.label, k -> new ArrayList<>()).add(e);
        }

        // DFS для поиска пути
        Deque<Integer> stack = new ArrayDeque<>();
        Map<Integer, GraphPanel.Edge> edgeTo = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        stack.push(start);
        visited.add(start);

        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (u == target) break;
            for (GraphPanel.Edge e : adj.getOrDefault(u, Collections.emptyList())) {
                int w = (e.v1.label == u ? e.v2.label : e.v1.label);
                if (!visited.contains(w)) {
                    visited.add(w);
                    edgeTo.put(w, e);
                    stack.push(w);
                }
            }
        }

        // Сбор пути
        List<GraphPanel.Edge> path = new ArrayList<>();
        if (!visited.contains(target)) {
            return path; // пути нет
        }
        Integer cur = target;
        while (cur != null && cur != start) {
            GraphPanel.Edge e = edgeTo.get(cur);
            path.add(e);
            cur = (e.v1.label == cur ? e.v2.label : e.v1.label);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Возвращает ребра цикла: найденный путь + новое ребро.
     */
    public static List<GraphPanel.Edge> findCycleEdges(
            Collection<GraphPanel.Edge> includedEdges,
            GraphPanel.Edge newEdge
    ) {
        List<GraphPanel.Edge> path = findPath(includedEdges,
                newEdge.v1.label, newEdge.v2.label);
        if (path.isEmpty()) {
            return Collections.emptyList();
        }
        List<GraphPanel.Edge> cycle = new ArrayList<>(path);
        cycle.add(newEdge);
        return cycle;
    }
}

// Состояние алгоритма с документированием цикла
record State( List<GraphPanel.Edge> includedEdges, int totalWeight, GraphPanel.Edge currentEdge, boolean isIncluded, List<GraphPanel.Edge> cycleEdges) {
    public State(
            List<GraphPanel.Edge> includedEdges,
            int totalWeight,
            GraphPanel.Edge currentEdge,
            boolean isIncluded,
            List<GraphPanel.Edge> cycleEdges
    ) {
        this.includedEdges = new ArrayList<>(includedEdges);
        this.totalWeight = totalWeight;
        this.currentEdge = currentEdge;
        this.isIncluded = isIncluded;
        this.cycleEdges = new ArrayList<>(cycleEdges);
    }

    @Override
    public String toString() {
        return String.format(
                "Current: %s included? %b | Total weight: %d | Included edges: %s | Cycle edges: %s",
                currentEdge, isIncluded, totalWeight, includedEdges, cycleEdges
        );
    }
}

// Реализация Kruskal
public class Kruskal {
    private final int numVertices;
    private final List<GraphPanel.Edge> edges;
    private final List<State> states;

    public Kruskal(List<GraphPanel.Edge> inputEdges, int numVertices) {
        this.numVertices = numVertices;
        this.edges = new ArrayList<>(inputEdges);
        this.states = new ArrayList<>();
    }

    public void addEdge(GraphPanel.Edge edge) {
        edges.add(edge);
    }

    public ArrayList<GraphPanel.Edge> computeMST() {
        Collections.sort(edges);
        ArrayList<GraphPanel.Edge> mst = new ArrayList<>();
        int totalWeight = 0;

        for (GraphPanel.Edge edge : edges) {
            // Проверяем: есть ли путь между концами ребра в текущем MST
            List<GraphPanel.Edge> path = CycleDetector.findPath(mst, edge.v1.label, edge.v2.label);
            boolean added = path.isEmpty();
            List<GraphPanel.Edge> cycle = Collections.emptyList();

            if (added) {
                mst.add(edge);
                totalWeight += edge.weight;
            } else {
                // Документируем цикл
                cycle = CycleDetector.findCycleEdges(mst, edge);
            }

            // Сохраняем состояние после обработки ребра
            states.add(new State(mst, totalWeight, edge, added, cycle));
        }
        return mst;
    }

    public List<State> getStates() {
        return states;
    }
}