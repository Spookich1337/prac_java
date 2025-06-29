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

        // Верхняя панель с кнопками
        JPanel topPanel = new JPanel();
        JButton startButton = new JButton("Старт Алгоритма");
        JButton loadButton = new JButton("Загрузка");

        startButton.addActionListener(e -> runAlgorithm());
        loadButton.addActionListener(e -> showLoadOptions());

        topPanel.add(startButton);
        topPanel.add(loadButton);

        // Панель управления шагами
        JPanel bottomPanel = new JPanel();
        stepBackButton = new JButton("<-");
        stepForwardButton = new JButton("->");
        stepBackButton.addActionListener(e -> stepBack());
        stepForwardButton.addActionListener(e -> stepForward());

        bottomPanel.add(stepBackButton);
        bottomPanel.add(stepForwardButton);

        // Правое текстовое поле
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(300, getHeight()));

        // Основной холст графа
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
        int choice = JOptionPane.showOptionDialog(this, "Загрузить граф:", "Загрузка",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    }

    private void runAlgorithm() {
        graphPanel.runAlgorithm(); // Заменить на реальный алгоритм
    }

    private void stepBack() {
        graphPanel.step(-1);
    }

    private void stepForward() {
        graphPanel.step(1);
    }
}
