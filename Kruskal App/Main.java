import javax.swing.SwingUtilities;
import src.logic.GraphApp;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GraphApp::new);
    }
}