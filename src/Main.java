import javax.swing.*;

/**
 * Application entry point.
 * Sets a modern Look & Feel, then shows the login screen.
 */
public class Main {
    public static void main(String[] args) {
        // Use the system L&F as base, then we paint everything ourselves
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global table/scroll bar colours
        UIManager.put("ScrollBar.thumb",       new javax.swing.plaf.ColorUIResource(60, 75, 110));
        UIManager.put("ScrollBar.track",       new javax.swing.plaf.ColorUIResource(18, 24, 40));
        UIManager.put("ScrollBar.thumbHighlight", new javax.swing.plaf.ColorUIResource(80, 100, 160));

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
