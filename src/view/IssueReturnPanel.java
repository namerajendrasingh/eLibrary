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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

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
    
    
 // ✅ NEW FILTER & PAGINATION FIELDS
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton prevBtn, nextBtn;
    private JLabel pageInfoLabel;
    
    // ✅ PAGINATION STATE
    private int currentPage = 0;
    private int pageSize = 25;
    private int totalRecords = 0;

    public IssueReturnPanel() {
        this.bookDAO = new BookDAO();
        this.issueDAO = new BookIssueDAO();
        this.userDAO = new UserDAO();
        initUI();
        loadPage(0);  // ✅ Start with pagination
        //loadActiveIssues();
    }
    
    private JPanel createIssuePanel() {  // ✅ EXTRACTED METHOD (called once)
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

        return issuePanel;
    }


   private void initUI() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // ✅ 1. ISSUE PANEL (WEST - unchanged)
    add(createIssuePanel(), BorderLayout.WEST);

    // ✅ 2. MAIN CENTER PANEL (Filters + Table + Pagination)
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    
    // TOP ROW: Filters + Pagination
    JPanel topRowPanel = new JPanel(new BorderLayout(10, 0));
    topRowPanel.add(createFilterPanel(), BorderLayout.WEST);      // Left: Search + Status
    topRowPanel.add(createPaginationPanel(), BorderLayout.EAST);  // Right: Pagination
    
    mainPanel.add(topRowPanel, BorderLayout.NORTH);
    
    // CENTER: Table
    setupActiveIssuesTable();
    JScrollPane scrollPane = new JScrollPane(activeIssuesTable);
    TitledBorder border = BorderFactory.createTitledBorder("Active Issued Books");
    border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
    scrollPane.setBorder(border);
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    
    add(mainPanel, BorderLayout.CENTER);

    // ✅ 3. ACTION BUTTONS (SOUTH - Return/Refresh only)
    JPanel buttonPanel = createButtonPanel();
    add(buttonPanel, BorderLayout.SOUTH);
}

    /**
     * ✅ NEW: Filter Panel (Search + Status)
     */
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Global search
        JLabel searchLabel = new JLabel("🔍 Search: ");
        searchField = new JTextField(20);
        searchField.addActionListener(e -> applyFilters());

        // Debounce search (300ms)
        Timer searchTimer = new Timer(300, e -> applyFilters());
        searchTimer.setRepeats(false);
        searchField.addActionListener(e -> {
            searchTimer.stop();
            searchTimer.start();
        });

        // Status filter
        JLabel statusLabel = new JLabel("Status: ");
        String[] statuses = {"All", "ISSUED", "OVERDUE"};
        statusFilter = new JComboBox<>(statuses);
        statusFilter.addActionListener(e -> applyFilters());

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);

        return filterPanel;
    }
    
    
    /**
     * ✅ NEW: Table setup with sorting
     */
    private void setupActiveIssuesTable() {
        activeIssuesModel = new DefaultTableModel(
                new Object[]{"Issue ID", "User ID", "Book ID", "Issue Date", "Due Date", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        activeIssuesTable = new JTable(activeIssuesModel);
        
        // ✅ SORTING
        sorter = new TableRowSorter<>(activeIssuesModel);
        activeIssuesTable.setRowSorter(sorter);
        setupColumnComparators();
        sorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING)));
        
        activeIssuesTable.setRowHeight(28);
        activeIssuesTable.setGridColor(new java.awt.Color(230, 230, 230));
        activeIssuesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        activeIssuesTable.setShowGrid(true);
        activeIssuesTable.setIntercellSpacing(new Dimension(0, 1));
        activeIssuesTable.getTableHeader().setReorderingAllowed(true);  // Enable sorting

        // Column alignment & widths (unchanged)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        int[] widths = {80, 80, 80, 130, 130, 100};
        for (int i = 0; i < activeIssuesTable.getColumnCount(); i++) {
            activeIssuesTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (i == 5) { // Status → LEFT
                activeIssuesTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            } else {
                activeIssuesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Header styling
        activeIssuesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        activeIssuesTable.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        activeIssuesTable.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));
    }

    /**
     * ✅ NEW: Column comparators for sorting
     */
    private void setupColumnComparators() {
        for (int i = 0; i < activeIssuesModel.getColumnCount(); i++) {
            sorter.setComparator(i, createColumnComparator(i));
        }
    }

    private Comparator<Object> createColumnComparator(int columnIndex) {
        return (o1, o2) -> {
            return switch (columnIndex) {
                case 0, 1, 2 -> { // IDs - Numeric
                    int n1 = o1 instanceof Number ? ((Number) o1).intValue() : 0;
                    int n2 = o2 instanceof Number ? ((Number) o2).intValue() : 0;
                    yield Integer.compare(n1, n2);
                }
                case 3, 4 -> { // Dates - String (YYYY-MM-DD)
                    String s1 = o1 != null ? o1.toString() : "";
                    String s2 = o2 != null ? o2.toString() : "";
                    yield s1.compareTo(s2);
                }
                case 5 -> { // Status
                    String s1 = o1 != null ? o1.toString() : "";
                    String s2 = o2 != null ? o2.toString() : "";
                    yield s1.compareToIgnoreCase(s2);
                }
                default -> 0;
            };
        };
    }
    
    /**
     * ✅ NEW: Pagination panel
     */
    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        prevBtn = new JButton("⬅️ Previous");
        nextBtn = new JButton("➡️ Next");
        pageInfoLabel = new JLabel("Page 1 (1-20 of 0)");
        
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        prevBtn.addActionListener(e -> previousPage());
        nextBtn.addActionListener(e -> nextPage());
        
        paginationPanel.add(prevBtn);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(nextBtn);
        return paginationPanel;
    }
    
    
    /**
     * ✅ NEW: Apply filters (Search + Status)
     */
    private void applyFilters() {
        RowFilter<DefaultTableModel, Object> filter = null;
        List<RowFilter<Object,Object>> filters = new ArrayList<>();

        // Global search
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }

        // Status filter (column 5)
        String status = (String) statusFilter.getSelectedItem();
        if (!"All".equals(status)) {
            filters.add(RowFilter.regexFilter("(?i)" + status, 5));
        }

        if (!filters.isEmpty()) {
            filter = RowFilter.andFilter(filters);
        }
        sorter.setRowFilter(filter);
    }
    
    /**
     * ✅ FIXED: Async pagination loading
     */
    private void loadPage(int page) {
        currentPage = page;
        
        new Thread(() -> {
            try {
                List<BookIssue> issues = issueDAO.getActiveIssuesWithPagination(page * pageSize, pageSize);
                totalRecords = issueDAO.getActiveIssuesCount();
                
                SwingUtilities.invokeLater(() -> {
                    activeIssuesModel.setRowCount(0);
                    for (BookIssue bi : issues) {
                        activeIssuesModel.addRow(new Object[]{
                                bi.getId(),
                                bi.getUserId(),
                                bi.getBookId(),
                                formatDate(bi.getIssueDate()),
                                formatDate(bi.getDueDate()),
                                bi.getStatus() != null ? bi.getStatus() : "—"
                        });
                    }
                    updatePaginationControls();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Load failed: " + e.getMessage())
                );
            }
        }).start();
    }

    private String formatDate(Timestamp ts) {
        return ts != null ? ts.toString().split(" ")[0] : "—";
    }

    private void previousPage() {
        if (currentPage > 0) loadPage(currentPage - 1);
    }

    private void nextPage() {
        if ((currentPage + 1) * pageSize < totalRecords) loadPage(currentPage + 1);
    }
    
    private void updatePaginationControls() {
        int start = currentPage * pageSize + 1;
        int end = Math.min(start + pageSize - 1, totalRecords);
        pageInfoLabel.setText(String.format("Page %d (%d-%d of %d)", 
            currentPage + 1, start, end, totalRecords));
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled((currentPage + 1) * pageSize < totalRecords);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        refreshBtn = new JButton("🔄 Refresh");
        returnBtn = new JButton("↩️ Return Selected");
        
        refreshBtn.addActionListener(e -> loadCurrentPage());
        returnBtn.addActionListener(e -> returnSelectedIssue());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(returnBtn);
        return buttonPanel;
    }

    private void loadCurrentPage() {
        searchField.setText("");
        if (statusFilter != null && statusFilter.getItemCount() > 0) {
            statusFilter.setSelectedIndex(0);
        }
        applyFilters();
        loadPage(currentPage);
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
