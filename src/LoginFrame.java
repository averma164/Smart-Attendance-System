import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Login screen for the Smart Attendance Management System.
 * Validates credentials against the 'users' table in MySQL.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnClear;
    private JButton btnSignUp;

    // ── Colour Palette ─────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18, 24, 40);
    private static final Color BG_CARD      = new Color(28, 36, 58);
    private static final Color ACCENT       = new Color(99, 120, 255);
    private static final Color ACCENT_HOVER = new Color(130, 148, 255);
    private static final Color TEXT_PRIMARY = new Color(230, 235, 255);
    private static final Color TEXT_MUTED   = new Color(140, 150, 180);
    private static final Color FIELD_BG     = new Color(38, 48, 72);
    private static final Color BORDER_COLOR = new Color(60, 75, 110);
    private static final Font  FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font  FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_FIELD   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font  FONT_BTN     = new Font("Segoe UI", Font.BOLD, 14);

    public LoginFrame() {
        setTitle("Smart Attendance System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        // Root panel with gradient background
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, BG_DARK, 0, getHeight(), new Color(10, 14, 28)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setLayout(new GridBagLayout());

        // Card panel
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(40, 45, 40, 45)
        ));
        card.setOpaque(true);
        card.setPreferredSize(new Dimension(360, 430));

        // Icon placeholder (emoji as label)
        JLabel iconLabel = new JLabel("🎓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel titleLabel = new JLabel("Smart Attendance", SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitleLabel.setFont(FONT_LABEL);
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(FONT_LABEL);
        lblUser.setForeground(TEXT_MUTED);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = createTextField();
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(FONT_LABEL);
        lblPass.setForeground(TEXT_MUTED);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        styleField(txtPassword);
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons
        btnLogin = createAccentButton("Login", ACCENT, ACCENT_HOVER);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnClear = createOutlineButton("Clear");
        btnClear.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components to card
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(28));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(16));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(24));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(btnClear);

        // ── Sign Up link below the card ─────────────────────────
        JPanel signUpRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        signUpRow.setOpaque(false);
        JLabel noAccountLbl = new JLabel("Don't have an account?");
        noAccountLbl.setFont(FONT_LABEL);
        noAccountLbl.setForeground(TEXT_MUTED);

        btnSignUp = new JButton("Sign Up");
        btnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSignUp.setForeground(ACCENT);
        btnSignUp.setContentAreaFilled(false);
        btnSignUp.setBorderPainted(false);
        btnSignUp.setFocusPainted(false);
        btnSignUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        signUpRow.add(noAccountLbl);
        signUpRow.add(btnSignUp);

        // Outer wrapper to stack card + sign-up row
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.add(card);
        wrapper.add(Box.createVerticalStrut(14));
        wrapper.add(signUpRow);

        root.add(wrapper);
        setContentPane(root);

        // Actions
        btnLogin.addActionListener(e -> doLogin());
        btnSignUp.addActionListener(e -> showRegisterDialog());
        btnClear.addActionListener(e -> {
            txtUsername.setText("");
            txtPassword.setText("");
            txtUsername.requestFocus();
        });

        // Enter key triggers login
        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                showError("Cannot connect to database.\nCheck DBConnection.java and MySQL server.");
                return;
            }
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM users WHERE username = ? AND password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                dispose();
                SwingUtilities.invokeLater(() -> new MainFrame(username).setVisible(true));
            } else {
                showError("Invalid username or password.");
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            showError("Database error:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }

    // ── Sign Up Dialog ─────────────────────────────────────────

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Create Account", true);
        dialog.setSize(420, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setUndecorated(false);

        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, BG_DARK, 0, getHeight(), new Color(10, 14, 28)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(30, 36, 30, 36)
        ));
        card.setPreferredSize(new Dimension(360, 440));

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Register a new user");
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblU = new JLabel("Username");
        lblU.setFont(FONT_LABEL); lblU.setForeground(TEXT_MUTED); lblU.setAlignmentX(LEFT_ALIGNMENT);
        JTextField regUser = createTextField();
        regUser.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblP = new JLabel("Password");
        lblP.setFont(FONT_LABEL); lblP.setForeground(TEXT_MUTED); lblP.setAlignmentX(LEFT_ALIGNMENT);
        JPasswordField regPass = new JPasswordField();
        styleField(regPass);
        regPass.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblP2 = new JLabel("Confirm Password");
        lblP2.setFont(FONT_LABEL); lblP2.setForeground(TEXT_MUTED); lblP2.setAlignmentX(LEFT_ALIGNMENT);
        JPasswordField regPass2 = new JPasswordField();
        styleField(regPass2);
        regPass2.setAlignmentX(LEFT_ALIGNMENT);

        JLabel statusLbl = new JLabel(" ");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLbl.setForeground(new Color(255, 100, 100));
        statusLbl.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnCreate = createAccentButton("Create Account", ACCENT, ACCENT_HOVER);
        btnCreate.setAlignmentX(LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));
        card.add(lblU);
        card.add(Box.createVerticalStrut(5));
        card.add(regUser);
        card.add(Box.createVerticalStrut(14));
        card.add(lblP);
        card.add(Box.createVerticalStrut(5));
        card.add(regPass);
        card.add(Box.createVerticalStrut(14));
        card.add(lblP2);
        card.add(Box.createVerticalStrut(5));
        card.add(regPass2);
        card.add(Box.createVerticalStrut(14));
        card.add(statusLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(btnCreate);

        root.add(card);
        dialog.setContentPane(root);

        btnCreate.addActionListener(e -> {
            String u  = regUser.getText().trim();
            String p  = new String(regPass.getPassword()).trim();
            String p2 = new String(regPass2.getPassword()).trim();

            if (u.isEmpty() || p.isEmpty()) {
                statusLbl.setText("All fields are required.");
                return;
            }
            if (!p.equals(p2)) {
                statusLbl.setText("Passwords do not match.");
                return;
            }
            if (p.length() < 6) {
                statusLbl.setText("Password must be at least 6 characters.");
                return;
            }
            try {
                Connection conn = DBConnection.getConnection();
                if (conn == null) {
                    statusLbl.setText("Database not connected.");
                    return;
                }
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)");
                ps.setString(1, u);
                ps.setString(2, p);
                ps.executeUpdate();
                ps.close();
                JOptionPane.showMessageDialog(dialog,
                    "Account created! You can now log in as \"" + u + "\".",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                txtUsername.setText(u);
                txtPassword.setText("");
                txtPassword.requestFocus();
            } catch (SQLIntegrityConstraintViolationException ex) {
                statusLbl.setText("Username already taken. Choose another.");
            } catch (SQLException ex) {
                statusLbl.setText("DB error: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    // ── Helpers ────────────────────────────────────────────────

    private JTextField createTextField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setFont(FONT_FIELD);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(FIELD_BG);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setOpaque(true);
    }

    private JButton createAccentButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setOpaque(false);
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(50, 60, 88) : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setOpaque(false);
        return btn;
    }
}
