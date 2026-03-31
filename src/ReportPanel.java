import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

/**
 * Panel to view attendance reports with optional filters by student or date.
 */
public class ReportPanel extends JPanel {

    private static final Color BG      = new Color(18, 24, 40);
    private static final Color CARD    = new Color(28, 36, 58);
    private static final Color ACCENT  = new Color(99, 120, 255);
    private static final Color FG      = new Color(230, 235, 255);
    private static final Color MUTED   = new Color(140, 150, 180);
    private static final Color BORDER  = new Color(60, 75, 110);
    private static final Color SUCCESS = new Color(80, 200, 120);

    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> cmbStudent;
    private JTextField txtFromDate;
    private JTextField txtToDate;
    private JLabel summaryLbl;

    public ReportPanel() {
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        // ── Filter Toolbar ──────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        toolbar.setBackground(CARD);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        cmbStudent = new JComboBox<>();
        cmbStudent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStudent.setBackground(new Color(38, 48, 72));
        cmbStudent.setForeground(FG);
        cmbStudent.setPreferredSize(new Dimension(200, 34));
        populateStudentCombo();

        txtFromDate = filterField(LocalDate.now().minusDays(30).toString());
        txtToDate   = filterField(LocalDate.now().toString());

        JButton btnLoad  = accentBtn("🔍  Load Report");
        JButton btnClear = outlineBtn("✖  Clear Filters");

        toolbar.add(makeLabel("Student:"));
        toolbar.add(cmbStudent);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(makeLabel("From:"));
        toolbar.add(txtFromDate);
        toolbar.add(makeLabel("To:"));
        toolbar.add(txtToDate);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(btnLoad);
        toolbar.add(btnClear);

        // ── Summary Strip ───────────────────────────────────────
        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        summaryBar.setBackground(new Color(22, 30, 50));
        summaryBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        summaryLbl = new JLabel("Load a report to see summary.");
        summaryLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryLbl.setForeground(MUTED);
        summaryBar.add(summaryLbl);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(toolbar, BorderLayout.NORTH);
        topSection.add(summaryBar, BorderLayout.SOUTH);

        // ── Table ───────────────────────────────────────────────
        String[] cols = {"#", "Student Name", "Roll No", "Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildStyledTable();
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        // Status column renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                c.setHorizontalAlignment(CENTER);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setOpaque(true);
                if ("Present".equals(val)) {
                    c.setForeground(new Color(80, 220, 130));
                    c.setBackground(sel ? new Color(60, 80, 140) : new Color(20, 60, 35));
                } else {
                    c.setForeground(new Color(255, 110, 110));
                    c.setBackground(sel ? new Color(60, 80, 140) : new Color(60, 20, 20));
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(new EmptyBorder(14, 20, 14, 20));

        add(topSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Actions
        btnLoad.addActionListener(e -> loadReport());
        btnClear.addActionListener(e -> {
            cmbStudent.setSelectedIndex(0);
            txtFromDate.setText(LocalDate.now().minusDays(30).toString());
            txtToDate.setText(LocalDate.now().toString());
            tableModel.setRowCount(0);
            summaryLbl.setText("Filters cleared.");
        });
    }

    private void loadReport() {
        tableModel.setRowCount(0);
        String fromDate = txtFromDate.getText().trim();
        String toDate   = txtToDate.getText().trim();
        String selectedStudent = (String) cmbStudent.getSelectedItem();

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;

            StringBuilder sql = new StringBuilder(
                "SELECT s.name, s.roll_no, a.date, a.status " +
                "FROM attendance a " +
                "JOIN students s ON a.student_id = s.id " +
                "WHERE a.date BETWEEN ? AND ? "
            );
            boolean filterStudent = selectedStudent != null && !selectedStudent.equals("All Students");
            if (filterStudent) sql.append("AND s.name = ? ");
            sql.append("ORDER BY a.date DESC, s.name");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            if (filterStudent) ps.setString(3, selectedStudent);

            ResultSet rs = ps.executeQuery();
            int row = 1, present = 0, absent = 0;
            while (rs.next()) {
                String status = rs.getString(4);
                if ("Present".equals(status)) present++; else absent++;
                tableModel.addRow(new Object[]{
                    row++, rs.getString(1), rs.getString(2),
                    rs.getString(3), status
                });
            }
            rs.close(); ps.close();

            int total = present + absent;
            double pct = total > 0 ? (present * 100.0 / total) : 0;
            summaryLbl.setText(String.format(
                "Total Records: %d  |  ✅ Present: %d  |  ❌ Absent: %d  |  Attendance Rate: %.1f%%",
                total, present, absent, pct));
            summaryLbl.setForeground(FG);
        } catch (Exception ex) {
            summaryLbl.setText("Error: " + ex.getMessage());
            summaryLbl.setForeground(new Color(255, 100, 100));
        }
    }

    private void populateStudentCombo() {
        cmbStudent.addItem("All Students");
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM students ORDER BY name");
            while (rs.next()) cmbStudent.addItem(rs.getString(1));
            rs.close(); st.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private JTable buildStyledTable() {
        JTable t = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (col != 4) {
                    c.setBackground(row % 2 == 0 ? new Color(24, 32, 52) : new Color(32, 42, 65));
                    c.setForeground(FG);
                    if (isRowSelected(row)) c.setBackground(new Color(60, 80, 140));
                }
                return c;
            }
        };
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(BG);
        t.setForeground(FG);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(20, 28, 48));
        h.setForeground(MUTED);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        h.setReorderingAllowed(false);
        return t;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(MUTED);
        return l;
    }

    private JTextField filterField(String text) {
        JTextField f = new JTextField(text, 11);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(FG);
        f.setBackground(new Color(38, 48, 72));
        f.setCaretColor(FG);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    private JButton accentBtn(String text) {
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
        btn.setPreferredSize(new Dimension(170, 34));
        return btn;
    }

    private JButton outlineBtn(String text) {
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
        btn.setPreferredSize(new Dimension(150, 34));
        return btn;
    }
}
