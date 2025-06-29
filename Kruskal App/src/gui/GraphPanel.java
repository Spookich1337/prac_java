package src.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import src.logic.Kruskal;

public class GraphPanel extends JPanel {
    private Vertex draggedVertex = null;
    private Point offset = new Point();

    public static class Vertex {
        public int x, y, radius = 10;
        public int label;
        Vertex(int x, int y, int label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
        boolean contains(int mx, int my) {
            return (mx - x)*(mx - x) + (my - y)*(my - y) <= radius * radius;
        }
    }

    public static class Edge implements Comparable<Edge> {
        public Vertex v1, v2;
        public int weight;

        Edge(Vertex v1, Vertex v2, int weight) {
            this.v1 = v1;
            this.v2 = v2;
            this.weight = weight;
        }

        boolean connects(Vertex v) {
            return v1 == v || v2 == v;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }
    }

    private java.util.ArrayList<Vertex> vertices = new ArrayList<>();
    private java.util.ArrayList<Edge> edges = new ArrayList<>();
    private Vertex selectedVertex = null;
    private int vertexCounter = 0;
    private JTextArea logArea;

    private java.util.List<String> algorithmSteps = new ArrayList<>();
    private int currentStep = -1;

    private TreeSet<Integer> freeLabels = new TreeSet<>();
    private int labelCounter = 1;


    public GraphPanel(JTextArea logArea) {
        this.logArea = logArea;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Vertex clicked = getVertexAt(e.getX(), e.getY());

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (clicked == null) {
                        int label = getNextLabel();
                        vertices.add(new Vertex(e.getX(), e.getY(), label));
                    } else {
                        if (selectedVertex == null) {
                            selectedVertex = clicked;
                        } else {
                            if (selectedVertex != clicked) {
                                String input = JOptionPane.showInputDialog("Введите вес ребра:");
                                try {
                                    int weight = Integer.parseInt(input);
                                    edges.add(new Edge(selectedVertex, clicked, weight));
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(null, "Некорректный ввод веса");
                                }
                            }
                            selectedVertex = null;
                        }
                        draggedVertex = clicked;
                        if (draggedVertex != null) {
                            offset.x = e.getX() - draggedVertex.x;
                            offset.y = e.getY() - draggedVertex.y;
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (clicked != null) {
                        edges.removeIf(edge -> edge.connects(clicked));
                        vertices.remove(clicked);
                        freeLabels.add(clicked.label); // освободить метку
                        selectedVertex = null;
                    }
                }
                repaint();
            }
            public void mouseReleased(MouseEvent e) {
                draggedVertex = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (draggedVertex != null) {
                    draggedVertex.x = e.getX() - offset.x;
                    draggedVertex.y = e.getY() - offset.y;
                    repaint();
                }
            }
        });
    }

    private Vertex getVertexAt(int x, int y) {
        for (Vertex v : vertices) {
            if (v.contains(x, y)) return v;
        }
        return null;
    }

    private int getNextLabel() {
    if (!freeLabels.isEmpty()) {
        return freeLabels.pollFirst();  // берём наименьший доступный номер
    }
    return labelCounter++;
}

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Edge edge : edges) {
            g.setColor(Color.BLACK);
            g.drawLine(edge.v1.x, edge.v1.y, edge.v2.x, edge.v2.y);
            int midX = (edge.v1.x + edge.v2.x) / 2;
            int midY = (edge.v1.y + edge.v2.y) / 2;
            g.drawString(Integer.toString(edge.weight), midX, midY);
        }
        for (Vertex v : vertices) {
            g.setColor(Color.WHITE);
            g.fillOval(v.x - v.radius, v.y - v.radius, v.radius * 2, v.radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(v.x - v.radius, v.y - v.radius, v.radius * 2, v.radius * 2);
            g.drawString(Integer.toString(v.label), v.x - 7, v.y + 5);
        }
    }

    public void alg_paint(){}

    public void runAlgorithm() {
        algorithmSteps.clear();
        currentStep = -1;

        Kruskal kruskal = new Kruskal(edges, vertices.size());
        ArrayList<Edge> mst = kruskal.computeMST();
        int totalWeight = 0;

        String result_string = "Алгоритм Краскала:\n";
        for (Edge edge : mst) {
            totalWeight += edge.weight;
            result_string += String.format("Добавлено ребро %d-%d с весом %d\n", edge.v1.label, edge.v2.label, edge.weight);
        }

        result_string += String.format("Общий вес остовного дерева: %d", totalWeight);
        algorithmSteps.add(result_string);

        step(1);
    }

    public void step(int delta) {
        currentStep += delta;
        if (currentStep < 0) currentStep = 0;
        if (currentStep >= algorithmSteps.size()) currentStep = algorithmSteps.size() - 1;
        if (!algorithmSteps.isEmpty()) {
            logArea.setText(algorithmSteps.get(currentStep));
        }
    }
}