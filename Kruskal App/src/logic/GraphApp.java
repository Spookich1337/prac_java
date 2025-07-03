package src.logic;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import src.gui.GraphPanel;

public class GraphApp extends JFrame {
    private GraphPanel graphPanel;
    private JTextArea logArea;
    private JButton stepBackButton, stepForwardButton;

    public GraphApp() {
        setTitle("Алгоритм Краскала");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setResizable(true);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JButton startButton = new JButton("Старт Алгоритма");
        JButton loadButton = new JButton("Загрузка");
        JButton resultButton = new JButton("Результат");

        resultButton.addActionListener(e -> runAlgorithmResult());
        startButton.addActionListener(e -> runAlgorithm());
        loadButton.addActionListener(e -> showLoadOptions());

        topPanel.add(resultButton);
        topPanel.add(startButton);
        topPanel.add(loadButton);

        JPanel bottomPanel = new JPanel();
        stepBackButton = new JButton("<--");
        stepForwardButton = new JButton("-->");
        stepBackButton.addActionListener(e -> stepBack());
        stepForwardButton.addActionListener(e -> stepForward());

        bottomPanel.add(stepBackButton);
        bottomPanel.add(stepForwardButton);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(300, getHeight()));

        graphPanel = new GraphPanel(logArea);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
        add(logScroll, BorderLayout.EAST);
        add(graphPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showLoadOptions() {
        String[] options = {"Из файла", "Случайная матрица"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Загрузить граф:",
                "Загрузка",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                graphPanel.loadFromFile(file);
            }
        } else if (choice == 1) {
            // Собираем панель с двумя полями ввода
            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            inputPanel.add(new JLabel("Количество вершин:"));
            JTextField vertField = new JTextField();
            inputPanel.add(vertField);
            inputPanel.add(new JLabel("Количество рёбер:"));
            JTextField edgeField = new JTextField();
            inputPanel.add(edgeField);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    inputPanel,
                    "Параметры генерации",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                try {
                    int numVert = Integer.parseInt(vertField.getText().trim());
                    int numEdges = Integer.parseInt(edgeField.getText().trim());

                    if (numVert < 1) {
                        throw new NumberFormatException("Необходимо numVert ≥ 1");
                    }
                    // Максимальное число рёбер в простом неориентированном графе: n*(n-1)/2
                    int maxEdges = numVert * (numVert - 1) / 2;
                    if (numEdges < numVert - 1 || numEdges > maxEdges) {
                        JOptionPane.showMessageDialog(
                                this,
                                String.format("Число рёбер должно быть в диапазоне [%d .. %d]", numVert - 1, maxEdges),
                                "Недопустимое значение",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    graphPanel.generateRandomGraph(numVert, numEdges);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Пожалуйста, введите корректные целые числа.",
                            "Ошибка ввода",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    private void runAlgorithmResult() {
        graphPanel.runAlgorithmResult();
    }

    private void runAlgorithm() {
        graphPanel.runAlgorithm();
    }

    private void stepBack() {
        graphPanel.step(-1);
        // Сброс флага при возврате к началу
        if (graphPanel.getCurrentStep() == 0) {
            graphPanel.setAlgorithmRunning(false);
        }
    }

    private void stepForward() {
        graphPanel.step(1);
    }
}