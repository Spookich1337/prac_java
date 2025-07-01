package src.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import src.logic.Kruskal;
import src.logic.State;

public class GraphPanel extends JPanel {
    private Vertex draggedVertex = null;
    private Point offset = new Point();
    private boolean vertexMoved = false;

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

    private java.util.List<State> algorithmSteps = new ArrayList<>();
    private int currentStep = -1;
    private java.util.List<Edge> shownEdges = new ArrayList<>();

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
                        shownEdges.clear();
                        int label = getNextLabel();
                        vertices.add(new Vertex(e.getX(), e.getY(), label));
                        selectedVertex = null;
                    } else {
                        if (selectedVertex == null) {
                            selectedVertex = clicked;
                        } else {
                            if (selectedVertex != clicked) {
                                if (!vertexMoved) {
                                    shownEdges.clear();
                                    String input = JOptionPane.showInputDialog("Введите вес ребра:");
                                    if (input != null && !input.trim().isEmpty()) {
                                        try {
                                            int weight = Integer.parseInt(input.trim());
                                            edges.add(new Edge(selectedVertex, clicked, weight));
                                        } catch (NumberFormatException ex) {
                                            JOptionPane.showMessageDialog(null, "Вес должен быть числом");
                                        }
                                    }
                                }
                                // Если vertexMoved == true, просто сбрасываем выбор, ребро не создаём
                            }
                            selectedVertex = null;
                        }

                        // Подготовка к возможному перемещению
                        draggedVertex = clicked;
                        if (draggedVertex != null) {
                            offset.x = e.getX() - draggedVertex.x;
                            offset.y = e.getY() - draggedVertex.y;
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (clicked != null) {
                        shownEdges.clear();
                        edges.removeIf(edge -> edge.connects(clicked));
                        vertices.remove(clicked);
                        freeLabels.add(clicked.label);
                        selectedVertex = null;
                    }
                }

                vertexMoved = false; // <-- сбрасываем флаг только после всех действий
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
                    vertexMoved = true;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Edge edge : edges) {
            g.setColor(Color.BLACK);
            g.drawLine(edge.v1.x, edge.v1.y, edge.v2.x, edge.v2.y);
            int midX = (edge.v1.x + edge.v2.x) / 2;
            int midY = (edge.v1.y + edge.v2.y) / 2;
            g.drawString(Integer.toString(edge.weight), midX, midY);
        }
        
        // Рисуем только рёбра из shownEdges (МОД)
        if (!shownEdges.isEmpty()) {
            for (Edge edge : shownEdges) {
                g.setColor(Color.GREEN);
                g.drawLine(edge.v1.x, edge.v1.y, edge.v2.x, edge.v2.y);
                int midX = (edge.v1.x + edge.v2.x) / 2;
                int midY = (edge.v1.y + edge.v2.y) / 2;
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(edge.weight), midX, midY);
            }
        }
        
        // Рисуем вершины
        for (Vertex v : vertices) {
            g.setColor(Color.WHITE);
            g.fillOval(v.x - v.radius, v.y - v.radius, v.radius * 2, v.radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(v.x - v.radius, v.y - v.radius, v.radius * 2, v.radius * 2);
            g.drawString(Integer.toString(v.label), v.x - 7, v.y + 5);
        }
    }

    public void runAlgorithmResult() {
        algorithmSteps.clear();
        currentStep = -1; 

        runAlgorithm();
        step(algorithmSteps.size());
    }

    public void runAlgorithm() {
        algorithmSteps.clear();
        currentStep = -1;

        Kruskal kruskal = new Kruskal(edges, vertices.size());
        kruskal.computeMST();
        
        algorithmSteps.add(new State(
            Collections.emptyList(), // includedEdges
            0,                       // totalWeight
            null,                    // currentEdge
            false,                   // isIncluded
            Collections.emptyList()  // cycleEdges
        ));
        algorithmSteps.addAll(kruskal.getStates());

        step(1);
    }

    public void step(int delta) {
        currentStep += delta;
        if (currentStep < 0) currentStep = 0;
        if (currentStep >= algorithmSteps.size()) currentStep = algorithmSteps.size() - 1;
        if (!algorithmSteps.isEmpty()) {
            if (algorithmSteps.get(currentStep).getCurrentEdge() == null) {
                shownEdges.clear();
                logArea.setText("Начало алгоритма\n");
            } else { 
                shownEdges = new ArrayList<>(algorithmSteps.get(currentStep).getIncludedEdges());
                logArea.setText(algorithmSteps.get(currentStep).toString());
            }
            repaint();
        }
    }

     public void generateRandomGraph(int n) {
        shownEdges.clear();
        vertexCounter = 0;
        labelCounter = 1;
        freeLabels.clear();

        vertices.clear();
        edges.clear();
        vertexCounter = 0;
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            int x = 100 + rand.nextInt(600);
            int y = 100 + rand.nextInt(400);
            vertices.add(new Vertex(x, y, getNextLabel()));
        }
        for (int i = 0; i < n - 1; i++) {
            int weight = 1 + rand.nextInt(20);
            edges.add(new Edge(vertices.get(i), vertices.get(i + 1), weight));
        }
        repaint();
    }

    public void loadFromFile(File file) {
        shownEdges.clear();
        vertexCounter = 0;
        labelCounter = 1;
        freeLabels.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            vertices.clear();
            edges.clear();
            vertexCounter = 0;
            ArrayList<String[]> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim().split("\\s+"));
            }
            int n = lines.size();

            // Расставляем вершины по окружности
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(getWidth(), getHeight()) / 3;
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                int x = centerX + (int)(radius * Math.cos(angle));
                int y = centerY + (int)(radius * Math.sin(angle));
                vertices.add(new Vertex(x, y, getNextLabel()));
            }

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    String val = lines.get(i)[j];
                    if (!val.equals("0")) {
                        try {
                            int weight = Integer.parseInt(val);
                            edges.add(new Edge(vertices.get(i), vertices.get(j), weight));
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(this, "Неверный формат веса: " + val);
                        }
                    }
                }
            }
            repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка чтения файла");
        }
    }
}