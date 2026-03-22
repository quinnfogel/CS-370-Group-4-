import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SCODashboard dashboard = new SCODashboard();
            dashboard.setVisible(true);
        });
    }
}