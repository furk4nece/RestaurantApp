import server.ServerApp;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ServerApp app = new ServerApp();
            app.setVisible(true);
        });
    }
}
