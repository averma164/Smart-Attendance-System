import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Panel for adding new students and viewing the student list.
 */
public class StudentPanel extends JPanel {

    // Palette
    private static final Color BG       = new Color(18, 24, 40);
    private static final Color CARD     = new Color(28, 36, 58);
    private static final Color ACCENT   = new Color(99, 120, 255);
    private static final Color FG       = new Color(230, 235, 255);
    private static final Color MUTED    = new Color(140, 150, 180);
    private static final Color FIELD_BG = new Color(38, 48, 72);
    private static final Color BORDER   = new Color(60, 75, 110);
    private static final Color ROW_ALT  = new Color(32, 42, 65);
    private static final Color SUCCESS  = new Color(80, 200, 120);

    private JTextField txtName;
    private JTextField txtRoll;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public StudentPanel() {
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadStudents();
    }

    private void buildUI() {
        // ── Left: Add Student Form ──────────────────────────────
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(CARD);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
            new EmptyBorder(30, 28, 30, 28)
        ));
        formCard.setPreferredSize(new Dimension(300, 0));

        JLabel formTitle = new JLabel("Add New Student");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        formTitle.setForeground(FG);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel formSub = new JLabel("Fill in the details below");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(MUTED);
        formSub.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblName = makeLabel("Full Name");
        txtName = makeField("e.g. John Smith");

        JLabel lblRoll = makeLabel("Roll Number");
        txtRoll = makeField("e.g. CS004");

        JButton btnAdd = makeAccentButton("➕  Add Student");
        btnAdd.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnRefresh = makeOutlineButton("🔄  Refresh List");
        btnRefresh.setAlignmentX(LEFT_ALIGNMENT);

        JLabel statusLbl = new JLabel(" ");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLbl.setForeground(SUCCESS);
        statusLbl.setAlignmentX(LEFT_ALIGNMENT);

        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(formSub);
        formCard.add(Box.createVerticalStrut(28));
        formCard.add(lblName);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(txtName);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(lblRoll);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(txtRoll);
        formCard.add(Box.createVerticalStrut(24));
        formCard.add(btnAdd);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(btnRefresh);
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(statusLbl);

        // ── Right: Student Table ────────────────────────────────
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(BG);
        tableCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel tableTitle = new JLabel("  Student Records");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(FG);
        tableTitle.setBorder(new EmptyBorder(0, 0, 14, 0));

        String[] cols = {"ID", "Full Name", "Roll Number"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable = buildTable(tableModel);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(110);

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(new LineBorder(BORDER, 1, true));

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        add(formCard, BorderLayout.WEST);
        add(tableCard, BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            String name = txtName.getText().trim();
            String roll = txtRoll.getText().trim();
            if (name.isEmpty() || roll.isEmpty()) {
                statusLbl.setForeground(new Color(255, 100, 100));
                statusLbl.setText("⚠ Both fields are required.");
                return;
            }
            try {
                String password = name.replace(" ", "") + "123";
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO students (name, roll_no, password) VALUES (?, ?, ?)");
                ps.setString(1, name);
                ps.setString(2, roll);
                ps.setString(3, password);
                ps.executeUpdate();
                ps.close();
                statusLbl.setForeground(SUCCESS);
                statusLbl.setText("✓ Student added successfully! (Password: " + password + ")");
                txtName.setText(""); txtRoll.setText("");
                loadStudents();
            } catch (SQLIntegrityConstraintViolationException ex) {
                statusLbl.setForeground(new Color(255, 100, 100));
                statusLbl.setText("⚠ Roll number already exists.");
            } catch (SQLException ex) {
                statusLbl.setForeground(new Color(255, 100, 100));
                statusLbl.setText("DB error: " + ex.getMessage());
            }
        });

        btnRefresh.addActionListener(e -> {
            loadStudents();
            statusLbl.setForeground(MUTED);
            statusLbl.setText("List refreshed.");
        });
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name, roll_no FROM students ORDER BY id");
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
            }
            rs.close(); st.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("placeholder", placeholder);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setForeground(FG);
        f.setBackground(FIELD_BG);
        f.setCaretColor(FG);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    private JButton makeAccentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(130, 148, 255) : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return btn;
    }

    private JButton makeOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(50, 60, 88) : CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(MUTED);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return btn;
    }

    private JTable buildTable(TableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? new Color(24, 32, 52) : ROW_ALT);
                c.setForeground(FG);
                if (isRowSelected(row)) c.setBackground(new Color(60, 80, 140));
                return c;
            }
        };
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(BG);
        t.setForeground(FG);
        t.setSelectionBackground(new Color(60, 80, 140));
        t.setSelectionForeground(FG);
        t.setFillsViewportHeight(true);

        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(20, 28, 48));
        header.setForeground(MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);
        return t;
    }
}
