import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window displayed after successful login.
 * Contains a tab bar for Student Management, Attendance, and Reports.
 */
public class MainFrame extends JFrame {

    private static final Color BG_DARK    = new Color(18, 24, 40);
    private static final Color ACCENT     = new Color(99, 120, 255);
    private static final Color TEXT_LIGHT = new Color(230, 235, 255);
    private static final Color TEXT_MUTED = new Color(140, 150, 180);

    private String loggedInUser;

    public MainFrame(String username) {
        this.loggedInUser = username;
        setTitle("Smart Attendance System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        // ── Top Header Bar ─────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(12, 16, 30));
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleGroup.setOpaque(false);

        JLabel iconLbl = new JLabel("🎓 ");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel appTitle = new JLabel("Smart Attendance System");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(TEXT_LIGHT);

        titleGroup.add(iconLbl);
        titleGroup.add(appTitle);
        header.add(titleGroup, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        JLabel userLbl = new JLabel("👤 " + loggedInUser);
        userLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLbl.setForeground(TEXT_MUTED);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setForeground(new Color(255, 100, 100));
        btnLogout.setBackground(new Color(50, 20, 20));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DBConnection.closeConnection();
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        userPanel.add(userLbl);
        userPanel.add(btnLogout);
        header.add(userPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Tabbed Pane ─────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_LIGHT);

        UIManager.put("TabbedPane.selected", new Color(28, 36, 58));
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabsOverlapBorder", true);

        tabs.addTab("  👥  Students  ", new StudentPanel());
        tabs.addTab("  ✅  Attendance  ", new AttendancePanel());
        tabs.addTab("  📊  Reports  ", new ReportPanel());

        tabs.setTabComponentAt(0, buildTabLabel("👥  Students"));
        tabs.setTabComponentAt(1, buildTabLabel("✅  Attendance"));
        tabs.setTabComponentAt(2, buildTabLabel("📊  Reports"));

        add(tabs, BorderLayout.CENTER);

        // ── Status Bar ─────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        statusBar.setBackground(new Color(12, 16, 30));
        JLabel statusLbl = new JLabel("Connected to smart_attendance database");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLbl.setForeground(new Color(80, 200, 120));
        statusBar.add(statusLbl);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JLabel buildTabLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        lbl.setForeground(TEXT_LIGHT);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        return lbl;
    }
}
