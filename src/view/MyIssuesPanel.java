package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.BookIssue;
import model.BookIssueDAO;

public class MyIssuesPanel extends JPanel {

    private final int userId;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton refreshBtn;
    private BookIssueDAO issueDAO;

    public MyIssuesPanel(int userId) {
        this.userId = userId;
        this.issueDAO = new BookIssueDAO();
        initUI();
        loadIssues();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("My Issued Books");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        titleLabel.setForeground(new java.awt.Color(51, 51, 51));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"Issue ID", "Book ID", "Issue Date", "Due Date", "Return Date", "Status"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setGridColor(new java.awt.Color(230, 230, 230));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        // ✅ CENTER ALIGN COLUMNS (professional selective alignment)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(javax.swing.JLabel.LEFT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            switch (i) {
                case 5: // Status → LEFT
                    table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
                    break;
                default: // IDs, Dates → CENTER
                    table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // ✅ Professional header styling
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        table.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));
        table.getTableHeader().setReorderingAllowed(false);

        // Optimal column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Issue ID
        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // Book ID
        table.getColumnModel().getColumn(2).setPreferredWidth(140);  // Issue Date
        table.getColumnModel().getColumn(3).setPreferredWidth(140);  // Due Date
        table.getColumnModel().getColumn(4).setPreferredWidth(140);  // Return Date
        table.getColumnModel().getColumn(5).setPreferredWidth(110);  // Status

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        scrollPane.setPreferredSize(new Dimension(900, 450));
        add(scrollPane, BorderLayout.CENTER);

        // Professional button panel
        refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadIssues());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadIssues() {
        tableModel.setRowCount(0);
        List<BookIssue> issues = issueDAO.findByUser(userId);
        for (BookIssue bi : issues) {
            tableModel.addRow(new Object[]{
                    bi.getId(),
                    bi.getBookId(),
                    bi.getIssueDate(),
                    bi.getDueDate(),
                    bi.getReturnDate(),
                    bi.getStatus() != null ? bi.getStatus() : "—"
            });
        }
    }

    public void refreshData() {
        loadIssues();
    }

}
