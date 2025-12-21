package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.BookDAO;
import model.BookIssue;
import model.BookIssueDAO;
import model.User;
import model.UserDAO;

public class IssueReturnPanel extends JPanel {

    private JTextField userIdField;
    private JTextField bookIdField;
    private JSpinner dueDateDaysSpinner;
    private JButton issueBtn;

    private JTable activeIssuesTable;
    private DefaultTableModel activeIssuesModel;
    private JButton returnBtn, refreshBtn;

    private final BookDAO bookDAO;
    private final BookIssueDAO issueDAO;
    private final UserDAO userDAO;

    public IssueReturnPanel() {
        this.bookDAO = new BookDAO();
        this.issueDAO = new BookIssueDAO();
        this.userDAO = new UserDAO();
        initUI();
        loadActiveIssues();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: Issue Form
        
        JPanel issuePanel = new JPanel(new GridBagLayout());
        issuePanel.setBorder(BorderFactory.createTitledBorder("Issue New Book"));
        ((TitledBorder)issuePanel.getBorder()).setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        issuePanel.setPreferredSize(new Dimension(400, 175));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        issuePanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        userIdField = new JTextField(10);
        userIdField.setPreferredSize(new Dimension(120, 28));
        issuePanel.add(userIdField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        issuePanel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1;
        bookIdField = new JTextField(10);
        bookIdField.setPreferredSize(new Dimension(120, 28));
        issuePanel.add(bookIdField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        issuePanel.add(new JLabel("Due in (days):"), gbc);
        gbc.gridx = 1;
        dueDateDaysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 365, 1));
        ((JSpinner.DefaultEditor) dueDateDaysSpinner.getEditor()).getTextField().setColumns(8);
        issuePanel.add(dueDateDaysSpinner, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        issueBtn = new JButton("Issue Book");
        issueBtn.addActionListener(e -> issueBook());
        issuePanel.add(issueBtn, gbc);

        add(issuePanel, BorderLayout.NORTH);

        // Center: Active Issues Table (for return)
        activeIssuesModel = new DefaultTableModel(
                new Object[]{"Issue ID", "User ID", "Book ID", "Issue Date", "Due Date", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        activeIssuesTable = new JTable(activeIssuesModel);
        activeIssuesTable.setRowHeight(28);
        activeIssuesTable.setGridColor(new java.awt.Color(230, 230, 230));
        activeIssuesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        activeIssuesTable.setShowGrid(true);
        activeIssuesTable.setIntercellSpacing(new Dimension(0, 1));

        // ✅ CENTER ALIGN COLUMNS (professional selective alignment)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        for (int i = 0; i < activeIssuesTable.getColumnCount(); i++) {
            switch (i) {
                case 5: // Status → LEFT
                    activeIssuesTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
                    break;
                default: // IDs, Dates → CENTER
                    activeIssuesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // ✅ Professional header styling
        activeIssuesTable.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        activeIssuesTable.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        activeIssuesTable.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));
        activeIssuesTable.getTableHeader().setReorderingAllowed(false);

        // Optimal column widths
        activeIssuesTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Issue ID
        activeIssuesTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // User ID
        activeIssuesTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Book ID
        activeIssuesTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Issue Date
        activeIssuesTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Due Date
        activeIssuesTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Status

        JScrollPane scrollPane = new JScrollPane(activeIssuesTable);
        TitledBorder border = BorderFactory.createTitledBorder("Active Issued Books");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        scrollPane.setBorder(border);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);

        

        // Bottom: Return / Refresh
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        refreshBtn = new JButton("🔄 Refresh");
        returnBtn = new JButton("↩️ Return Selected");

        refreshBtn.addActionListener(e -> loadActiveIssues());
        returnBtn.addActionListener(e -> returnSelectedIssue());

        bottomPanel.add(refreshBtn);
        bottomPanel.add(returnBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void issueBook() {
        String userIdText = userIdField.getText().trim();
        String bookIdText = bookIdField.getText().trim();

        if (userIdText.isEmpty() || bookIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "User ID and Book ID are required.", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId, bookId;
        try {
            userId = Integer.parseInt(userIdText);
            bookId = Integer.parseInt(bookIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "User ID and Book ID must be integers.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found with ID: " + userId, "User Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean stockOk = bookDAO.decreaseAvailableCopies(bookId);
        if (!stockOk) {
            JOptionPane.showMessageDialog(this, "Book not available or invalid book ID.", "No Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int days = (int) dueDateDaysSpinner.getValue();
        LocalDate today = LocalDate.now();
        LocalDate dueDateLocal = today.plusDays(days);

        BookIssue issue = new BookIssue();
        issue.setUserId(userId);
        issue.setBookId(bookId);
        issue.setIssueDate(Timestamp.valueOf(today.atStartOfDay()));
        issue.setDueDate(Timestamp.valueOf(dueDateLocal.atStartOfDay()));
        issue.setStatus("ISSUED");

        boolean ok = issueDAO.issueBook(issue);
        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Book issued successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadActiveIssues();
            userIdField.setText("");
            bookIdField.setText("");
            dueDateDaysSpinner.setValue(14);
        } else {
            bookDAO.increaseAvailableCopies(bookId);
            JOptionPane.showMessageDialog(this, "❌ Failed to issue book. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadActiveIssues() {
        activeIssuesModel.setRowCount(0);
        List<BookIssue> list = issueDAO.findActiveIssues();
        for (BookIssue bi : list) {
            activeIssuesModel.addRow(new Object[]{
                    bi.getId(),
                    bi.getUserId(),
                    bi.getBookId(),
                    bi.getIssueDate(),
                    bi.getDueDate(),
                    bi.getStatus() != null ? bi.getStatus() : "—"
            });
        }
    }

    private void returnSelectedIssue() {
        int row = activeIssuesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an issue to return.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = activeIssuesTable.convertRowIndexToModel(row);
        int issueId = (Integer) activeIssuesModel.getValueAt(modelRow, 0);
        int bookId = (Integer) activeIssuesModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Return issue #%d?\nBook ID: %d", issueId, bookId),
                "Confirm Return",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean okIssue = issueDAO.returnBook(issueId);
            boolean okStock = bookDAO.increaseAvailableCopies(bookId);

            if (okIssue && okStock) {
                JOptionPane.showMessageDialog(this, "✅ Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadActiveIssues();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to return book. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

	public void refreshData() {
		loadActiveIssues();
	}

}
