import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

/**
 * Panel to show the attendance records of a specific student.
 */
public class StudentAttendancePanel extends JPanel {

    private static final Color BG      = new Color(18, 24, 40);
    private static final Color CARD    = new Color(28, 36, 58);
    private static final Color FG      = new Color(230, 235, 255);
    private static final Color MUTED   = new Color(140, 150, 180);
    private static final Color BORDER  = new Color(60, 75, 110);
    private static final Color ROW_ALT = new Color(32, 42, 65);

    private int studentId;
    private DefaultTableModel tableModel;
    private JTable attendanceTable;

    public StudentAttendancePanel(int studentId) {
        this.studentId = studentId;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadAttendance();
    }

    private void buildUI() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(BG);
        tableCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel titleLbl = new JLabel("  My Attendance History");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(FG);
        titleLbl.setBorder(new EmptyBorder(0, 0, 16, 0));

        String[] cols = {"Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        attendanceTable = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? new Color(24, 32, 52) : ROW_ALT);
                c.setForeground(FG);
                
                if (col == 1) {
                    JLabel label = (JLabel) c;
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    String val = (String) getValueAt(row, col);
                    if ("Present".equals(val)) {
                        label.setForeground(new Color(80, 220, 130));
                    } else {
                        label.setForeground(new Color(255, 110, 110));
                    }
                } else {
                    ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                if (isRowSelected(row)) {
                    c.setBackground(new Color(60, 80, 140));
                }
                return c;
            }
        };

        attendanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        attendanceTable.setRowHeight(40);
        attendanceTable.setShowGrid(false);
        attendanceTable.setIntercellSpacing(new Dimension(0, 0));
        attendanceTable.setBackground(BG);
        attendanceTable.setForeground(FG);
        attendanceTable.setSelectionBackground(new Color(60, 80, 140));
        attendanceTable.setFillsViewportHeight(true);

        JTableHeader header = attendanceTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(20, 28, 48));
        header.setForeground(MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(attendanceTable);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(new LineBorder(BORDER, 1, true));

        tableCard.add(titleLbl, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadAttendance() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement(
                "SELECT date, status FROM attendance WHERE student_id = ? ORDER BY date DESC"
            );
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getDate("date"), rs.getString("status")});
            }
            rs.close(); 
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
