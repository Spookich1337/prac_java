package src.logic;

import java.util.*;
import src.gui.GraphPanel;

public record State( List<GraphPanel.Edge> includedEdges, List<GraphPanel.Edge> excludedEdges,  int totalWeight, 
    GraphPanel.Edge currentEdge, boolean isIncluded, List<GraphPanel.Edge> cycleEdges) {
    public State(
            List<GraphPanel.Edge> includedEdges,
            List<GraphPanel.Edge> excludedEdges,
            int totalWeight,
            GraphPanel.Edge currentEdge,
            boolean isIncluded,
            List<GraphPanel.Edge> cycleEdges
    ) {
        this.includedEdges = new ArrayList<>(includedEdges);
        this.excludedEdges = new ArrayList<>(excludedEdges);
        this.totalWeight = totalWeight;
        this.currentEdge = currentEdge;
        this.isIncluded = isIncluded;
        this.cycleEdges = new ArrayList<>(cycleEdges);
    }

    @Override
    public String toString() {
        String finalString = "";

        finalString += "Текущее ребро: " + (currentEdge != null ? currentEdge.v1.label + " - " + currentEdge.v2.label : "None") + "\n";
        finalString += "Включеные ребра: ";
        if (includedEdges.isEmpty()) {
            finalString += "------------\n";
        } else {
            for (GraphPanel.Edge edge : includedEdges) {
                finalString += edge.v1.label + " - " + edge.v2.label + "; ";
            } 
            finalString += "\n";
        }
        finalString += "Суммарный вес: " + totalWeight + "\n";
        if (isIncluded) {
            finalString += "Ребро включено.\n";
        } else {
            finalString += "Ребро не включено.\n";
            finalString += "Ребра цикла: ";
            for (GraphPanel.Edge edge : cycleEdges) {
                finalString += edge.v1.label + " - " + edge.v2.label + "; ";
            }
        }

        return finalString;
    }

    public List<GraphPanel.Edge> getIncludedEdges() {
        return Collections.unmodifiableList(includedEdges);
    }

    public List<GraphPanel.Edge> getExcludedEdges() {
        return Collections.unmodifiableList(excludedEdges);
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public GraphPanel.Edge getCurrentEdge() {
        return currentEdge;
    }

    public boolean isIncluded() {
        return isIncluded;
    }

    public List<GraphPanel.Edge> getCycleEdges() {
        return Collections.unmodifiableList(cycleEdges);
    }
}
