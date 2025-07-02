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
    private boolean algorithmRunning = false; // Флаг выполнения алгоритма

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

        // Новый метод: проверка нахождения точки на ребре
        boolean containsPoint(int x, int y, int threshold) {
            // Проверяем расстояние от точки до линии ребра
            int dx = v2.x - v1.x;
            int dy = v2.y - v1.y;
            int lengthSquared = dx*dx + dy*dy;

            // Если длина ребра 0 (вершины совпадают)
            if (lengthSquared == 0) {
                return (v1.x - x)*(v1.x - x) + (v1.y - y)*(v1.y - y) <= threshold*threshold;
            }

            // Вычисляем проекцию точки на линию ребра
            double t = ((double)((x - v1.x)*dx + (y - v1.y)*dy)) / lengthSquared;
            t = Math.max(0, Math.min(1, t)); // Ограничиваем проекцию отрезком

            // Ближайшая точка на ребре
            int projX = (int)(v1.x + t*dx);
            int projY = (int)(v1.y + t*dy);

            // Квадрат расстояния до проекции
            int distSquared = (projX - x)*(projX - x) + (projY - y)*(projY - y);
            return distSquared <= threshold*threshold;
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
    private java.util.List<Edge> excludedEdges = new ArrayList<>();
    private java.util.List<Edge> cycleEdges = new ArrayList<>();

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
                        // Добавление новой вершины
                        if (algorithmRunning && !confirmAlgorithmInterruption()) return;
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
                                    // Добавление нового ребра
                                    if (algorithmRunning && !confirmAlgorithmInterruption()) {
                                        selectedVertex = null;
                                        return;
                                    }
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
                        // Удаление вершины
                        if (algorithmRunning && !confirmAlgorithmInterruption()) return;
                        shownEdges.clear();
                        edges.removeIf(edge -> edge.connects(clicked));
                        vertices.remove(clicked);
                        freeLabels.add(clicked.label);
                        selectedVertex = null;
                    } else {
                        // Проверяем клик на ребре
                        Edge edge = getEdgeAt(e.getX(), e.getY());
                        if (edge != null) {
                            // Удаление ребра
                            if (algorithmRunning && !confirmAlgorithmInterruption()) return;
                            shownEdges.clear();
                            edges.remove(edge);
                            selectedVertex = null;
                        }
                    }
                }

                vertexMoved = false;
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

    // Подтверждение прерывания алгоритма
    private boolean confirmAlgorithmInterruption() {
        Object[] options = {"Да", "Нет"};
        int response = JOptionPane.showOptionDialog(
            this,
            "Алгоритм выполняется. Прервать и продолжить редактирование?",
            "Подтверждение прерывания",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,  // Массив с текстом кнопок
            options[0] // Первая кнопка по умолчанию
        );
        if (response == JOptionPane.YES_OPTION) {
            algorithmRunning = false;
            return true;
        }
        return false;
    }

    // Новый метод: поиск ребра по координатам
    private Edge getEdgeAt(int x, int y) {
        for (Edge edge : edges) {
            if (edge.containsPoint(x, y, 5)) { // Порог 5 пикселей
                return edge;
            }
        }
        return null;
    }

    public void showRandomGraphDialog() {
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Количество вершин:"));
        JTextField verticesField = new JTextField("5");
        inputPanel.add(verticesField);
        inputPanel.add(new JLabel("Количество рёбер:"));
        JTextField edgesField = new JTextField("7");
        inputPanel.add(edgesField);

        int result = JOptionPane.showConfirmDialog(
            this,
            inputPanel,
            "Параметры случайного графа",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int vertices = Integer.parseInt(verticesField.getText());
                int edges = Integer.parseInt(edgesField.getText());
                
                // Проверка минимальных/максимальных значений
                vertices = Math.max(2, Math.min(vertices, 20)); // от 2 до 20 вершин
                int maxPossibleEdges = vertices * (vertices - 1) / 2;
                edges = Math.max(vertices - 1, Math.min(edges, maxPossibleEdges)); // минимум n-1, максимум все возможные
                
                generateRandomGraph(vertices, edges);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, введите корректные числа", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void generateRandomGraph(int numVert, int numEdges) {
        if (algorithmRunning && !confirmAlgorithmInterruption()) return;
        
        shownEdges.clear();
        freeLabels.clear();
        vertices.clear();
        edges.clear();
        excludedEdges.clear();
        cycleEdges.clear();
        vertexCounter = 0;
        labelCounter = 1;

        Random rand = new Random();

        // 1. Создаём вершины в случайных позициях
        for (int i = 0; i < numVert; i++) {
            int x = 100 + rand.nextInt(600);
            int y = 100 + rand.nextInt(400);
            vertices.add(new Vertex(x, y, getNextLabel()));
        }

        // 2. Строим связное остовное дерево
        ArrayList<Integer> order = new ArrayList<>();
        for (int i = 0; i < numVert; i++) order.add(i);
        Collections.shuffle(order, rand);

        Set<String> edgeSet = new HashSet<>();

        for (int i = 1; i < numVert; i++) {
            int v1 = order.get(i);
            int v2 = order.get(rand.nextInt(i));
            int weight = 1 + rand.nextInt(20);
            Vertex a = vertices.get(v1);
            Vertex b = vertices.get(v2);
            edges.add(new Edge(a, b, weight));
            edgeSet.add(Math.min(v1, v2) + "-" + Math.max(v1, v2));
        }

        // 3. Добавляем случайные рёбра до нужного числа
        int attempts = 0;
        while (edges.size() < numEdges && attempts < numEdges * 10) {
            int i = rand.nextInt(numVert);
            int j = rand.nextInt(numVert);
            if (i == j) {
                attempts++;
                continue;
            }
            String key = Math.min(i, j) + "-" + Math.max(i, j);
            if (edgeSet.contains(key)) {
                attempts++;
                continue;
            }
            int weight = 1 + rand.nextInt(20);
            edges.add(new Edge(vertices.get(i), vertices.get(j), weight));
            edgeSet.add(key);
        }

        repaint();
    }

    // Остальные методы остаются без изменений
    private Vertex getVertexAt(int x, int y) {
        for (Vertex v : vertices) {
            if (v.contains(x, y)) return v;
        }
        return null;
    }

    private int getNextLabel() {
        if (!freeLabels.isEmpty()) {
            return freeLabels.pollFirst();
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

        if (!excludedEdges.isEmpty()){
            for (Edge edge : excludedEdges) {
                g.setColor(Color.RED);
                g.drawLine(edge.v1.x, edge.v1.y, edge.v2.x, edge.v2.y);
                int midX = (edge.v1.x + edge.v2.x) / 2;
                int midY = (edge.v1.y + edge.v2.y) / 2;
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(edge.weight), midX, midY);
            }
        }

        if (!cycleEdges.isEmpty()) {
            for (Edge edge : cycleEdges) {
                g.setColor(Color.PINK);
                g.drawLine(edge.v1.x, edge.v1.y, edge.v2.x, edge.v2.y);
                int midX = (edge.v1.x + edge.v2.x) / 2;
                int midY = (edge.v1.y + edge.v2.y) / 2;
                g.setColor(Color.BLACK);
                g.drawString(Integer.toString(edge.weight), midX, midY);
            }
        }
        
        for (Vertex v : vertices) {
            g.setColor(Color.WHITE);
            g.fillOval(v.x - v.radius, v.y - v.radius, v.radius * 2, v.radius * 2);
            g.setColor(Color.BLACK);
            g.drawOval(v.x - v.radius, v.y - v.radius, v.radius * 2, v.radius * 2);
            g.drawString(Integer.toString(v.label), v.x - 7, v.y + 5);
        }
    }

    public void runAlgorithmResult() {
        algorithmRunning = true; 
        algorithmSteps.clear();
        currentStep = -1; 
        runAlgorithm();
        step(algorithmSteps.size());
        if (!algorithmSteps.isEmpty()) {
            State lastState = algorithmSteps.get(algorithmSteps.size() - 1);
            shownEdges = new ArrayList<>(lastState.getIncludedEdges());
            excludedEdges = new ArrayList<>(lastState.getExcludedEdges()); 
            cycleEdges.clear();
            logArea.setText("Минимальное остовное дерево построено.\n");
            repaint();
        }
    }   

    public void runAlgorithm() {
        algorithmRunning = true; 
        algorithmSteps.clear();
        currentStep = -1;

        Kruskal kruskal = new Kruskal(edges, vertices.size());
        kruskal.computeMST();
        
        algorithmSteps.add(new State(
            Collections.emptyList(),
            Collections.emptyList(),
            0,
            null,
            false,
            Collections.emptyList()
        ));
        algorithmSteps.addAll(kruskal.getStates());

        step(1);
    }

    public void step(int delta) {
        int previousStep = currentStep;
        
        currentStep += delta;
        
        if (currentStep < 0) {
            currentStep = 0;
        } else if (currentStep > algorithmSteps.size()) {
            currentStep = algorithmSteps.size();
        }
        
        if (currentStep == previousStep) {
            return;
        }
        
        if (currentStep == algorithmSteps.size()) {
            if (!algorithmSteps.isEmpty()) {
                State lastState = algorithmSteps.get(algorithmSteps.size() - 1);
                shownEdges = new ArrayList<>(lastState.getIncludedEdges());
                excludedEdges = new ArrayList<>(lastState.getExcludedEdges());
            }
            cycleEdges.clear();
            logArea.setText("Минимальное остовное дерево построено.\n");
            repaint();
            return;
        }
        
        State currentState = algorithmSteps.get(currentStep);
        if (currentState.getCurrentEdge() == null) {
            shownEdges.clear();
            excludedEdges.clear();
            cycleEdges.clear();
            logArea.setText("Начало алгоритма\n");
        } else {
            shownEdges = new ArrayList<>(currentState.getIncludedEdges());
            excludedEdges = new ArrayList<>(currentState.getExcludedEdges());
            cycleEdges = new ArrayList<>(currentState.getCycleEdges());
            logArea.setText(currentState.toString());
        }
        repaint();
    }

    public void loadFromFile(File file) {
        if (algorithmRunning && !confirmAlgorithmInterruption()) return;
        
        shownEdges.clear();
        excludedEdges.clear();
        cycleEdges.clear();
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

    // Геттер для текущего шага
    public int getCurrentStep() {
        return currentStep;
    }

    // Сеттер для флага выполнения алгоритма
    public void setAlgorithmRunning(boolean algorithmRunning) {
        this.algorithmRunning = algorithmRunning;
    }
}