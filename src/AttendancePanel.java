import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Panel for marking daily attendance (Present/Absent) for all students.
 */
public class AttendancePanel extends JPanel {

    private static final Color BG      = new Color(18, 24, 40);
    private static final Color CARD    = new Color(28, 36, 58);
    private static final Color ACCENT  = new Color(99, 120, 255);
    private static final Color FG      = new Color(230, 235, 255);
    private static final Color MUTED   = new Color(140, 150, 180);
    private static final Color BORDER  = new Color(60, 75, 110);
    private static final Color SUCCESS = new Color(80, 200, 120);
    private static final Color ERR     = new Color(255, 100, 100);

    // Table columns:  [0]=ID(hidden)  [1]=Name  [2]=Roll  [3]=Status(Present/Absent)
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel txtDate;
    private JLabel statusLbl;
    private String selectedDate;

    public AttendancePanel() {
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadTodayAttendance();
    }

    private void buildUI() {
        // ── Top toolbar ─────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        toolbar.setBackground(CARD);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel lbl = new JLabel("Date:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(MUTED);

        selectedDate = LocalDate.now().toString();
        txtDate = new JLabel(selectedDate);
        txtDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtDate.setForeground(FG);
        txtDate.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));

        JButton btnPrev = navBtn("◀");
        JButton btnNext = navBtn("▶");
        JButton btnToday = pillBtn("Today");
        JButton btnSave = accentBtn("💾  Save Attendance");

        statusLbl = new JLabel(" ");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLbl.setForeground(SUCCESS);

        toolbar.add(lbl);
        toolbar.add(btnPrev);
        toolbar.add(txtDate);
        toolbar.add(btnNext);
        toolbar.add(btnToday);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnSave);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(statusLbl);

        // ── Table ───────────────────────────────────────────────
        String[] cols = {"ID", "Student Name", "Roll No", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 3 ? String.class : Object.class;
            }
        };

        table = buildStyledTable();

        // Combo-box editor for Status column
        JComboBox<String> combo = new JComboBox<>(new String[]{"Present", "Absent"});
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(new Color(38, 48, 72));
        combo.setForeground(FG);
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(combo));
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);   // hide ID
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);

        // Custom renderer to colour Present/Absent cells
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                c.setHorizontalAlignment(CENTER);
                c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                if ("Present".equals(val)) {
                    c.setForeground(new Color(80, 220, 130));
                    c.setBackground(sel ? new Color(60, 80, 140) : new Color(20, 60, 35));
                } else {
                    c.setForeground(new Color(255, 110, 110));
                    c.setBackground(sel ? new Color(60, 80, 140) : new Color(60, 20, 20));
                }
                c.setOpaque(true);
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(new EmptyBorder(16, 20, 16, 20));

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // ── Button Logic ────────────────────────────────────────
        btnPrev.addActionListener(e -> {
            selectedDate = LocalDate.parse(selectedDate).minusDays(1).toString();
            txtDate.setText(selectedDate);
            loadTodayAttendance();
        });
        btnNext.addActionListener(e -> {
            selectedDate = LocalDate.parse(selectedDate).plusDays(1).toString();
            txtDate.setText(selectedDate);
            loadTodayAttendance();
        });
        btnToday.addActionListener(e -> {
            selectedDate = LocalDate.now().toString();
            txtDate.setText(selectedDate);
            loadTodayAttendance();
        });
        btnSave.addActionListener(e -> saveAttendance());
    }

    /** Loads students and their existing attendance for selectedDate. */
    private void loadTodayAttendance() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(
                "SELECT s.id, s.name, s.roll_no, " +
                "COALESCE(a.status, 'Absent') AS status " +
                "FROM students s " +
                "LEFT JOIN attendance a ON s.id = a.student_id AND a.date = ? " +
                "ORDER BY s.id"
            );
            ps.setString(1, selectedDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)
                });
            }
            rs.close(); ps.close();
            statusLbl.setForeground(MUTED);
            statusLbl.setText("Loaded " + tableModel.getRowCount() + " student(s) for " + selectedDate);
        } catch (SQLException ex) {
            statusLbl.setForeground(ERR);
            statusLbl.setText("Error loading: " + ex.getMessage());
        }
    }

    private void saveAttendance() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        if (tableModel.getRowCount() == 0) {
            statusLbl.setForeground(ERR);
            statusLbl.setText("No students to save.");
            return;
        }
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO attendance (student_id, date, status) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status)"
            );
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                ps.setInt(1, (Integer) tableModel.getValueAt(i, 0));
                ps.setString(2, selectedDate);
                ps.setString(3, (String) tableModel.getValueAt(i, 3));
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            statusLbl.setForeground(SUCCESS);
            statusLbl.setText("✓ Attendance saved for " + selectedDate + "!");
        } catch (SQLException ex) {
            statusLbl.setForeground(ERR);
            statusLbl.setText("Save error: " + ex.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private JTable buildStyledTable() {
        JTable t = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (col != 3) {
                    c.setBackground(row % 2 == 0 ? new Color(24, 32, 52) : new Color(32, 42, 65));
                    c.setForeground(FG);
                }
                if (isRowSelected(row) && col != 3)
                    c.setBackground(new Color(60, 80, 140));
                return c;
            }
        };
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(40);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(BG);
        t.setForeground(FG);
        t.setSelectionBackground(new Color(60, 80, 140));
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(20, 28, 48));
        h.setForeground(MUTED);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        h.setReorderingAllowed(false);
        return t;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(FG);
        b.setBackground(new Color(38, 48, 72));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton pillBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setForeground(MUTED);
        b.setBackground(CARD);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
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
        btn.setPreferredSize(new Dimension(180, 36));
        return btn;
    }
}
