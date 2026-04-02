import javax.swing.*;
import java.awt.*;

/**
 * Main application window displayed after successful student login.
 * Contains a dashboard showing only their personal attendance.
 */
public class StudentMainFrame extends JFrame {

    private static final Color BG_DARK    = new Color(18, 24, 40);
    private static final Color TEXT_LIGHT = new Color(230, 235, 255);
    private static final Color TEXT_MUTED = new Color(140, 150, 180);

    private int studentId;
    private String studentName;
    private String rollNo;

    public StudentMainFrame(int studentId, String studentName, String rollNo) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNo = rollNo;
        
        setTitle("Student Dashboard - " + studentName);
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

        JLabel appTitle = new JLabel("My Attendance Dashboard");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(TEXT_LIGHT);

        titleGroup.add(iconLbl);
        titleGroup.add(appTitle);
        header.add(titleGroup, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        JLabel userLbl = new JLabel("👤 " + studentName + " (" + rollNo + ")");
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
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        userPanel.add(userLbl);
        userPanel.add(btnLogout);
        header.add(userPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Main Content Area ──────────────────────────────────
        add(new StudentAttendancePanel(studentId), BorderLayout.CENTER);

        // ── Status Bar ─────────────────────────────────────────
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        statusBar.setBackground(new Color(12, 16, 30));
        JLabel statusLbl = new JLabel("Viewing personal attendance records");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLbl.setForeground(new Color(80, 200, 120));
        statusBar.add(statusLbl);
        add(statusBar, BorderLayout.SOUTH);
    }
}
