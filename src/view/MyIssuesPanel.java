package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import model.BookIssue;
import model.BookIssueDAO;

public class MyIssuesPanel extends JPanel {

    private final int userId;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton refreshBtn, prevBtn, nextBtn;
    private JLabel pageInfoLabel;
    private JComboBox<String> statusFilter;
    private JTextField searchField;
    private BookIssueDAO issueDAO;

    // ✅ PAGINATION STATE
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalRecords = 0;

    public MyIssuesPanel(int userId) {
        this.userId = userId;
        this.issueDAO = new BookIssueDAO();
        initUI();
        loadPage(0);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("📋 My Issued Books");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        titleLabel.setForeground(new java.awt.Color(51, 51, 51));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ✅ FILTER PANEL
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Table model
        tableModel = new DefaultTableModel(
            new Object[]{"Issue ID", "Book Name", "Book ID", "Issue Date", "Due Date", "Return Date", "Status"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        
        // ✅ SORTING SETUP
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        setupColumnComparators();
        sorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING)));
        
        table.setRowHeight(30);
        table.setGridColor(new java.awt.Color(230, 230, 230));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(true);  // ✅ ENABLE SORTING

        // Column alignment
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

        // Professional header styling
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        table.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));

        // Optimal column widths
        int[] widths = {90, 90, 140, 140, 140, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        scrollPane.setPreferredSize(new Dimension(900, 400));
        add(scrollPane, BorderLayout.CENTER);

        // ✅ PAGINATION PANEL
        JPanel paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Search box
        JLabel searchLabel = new JLabel("🔍 Search: ");
        searchField = new JTextField(15);
        searchField.addActionListener(e -> applyFilters());
        
        // Debounce search (300ms delay)
        Timer searchTimer = new Timer(300, e -> applyFilters());
        searchTimer.setRepeats(false);
        searchField.addActionListener(e -> {
            searchTimer.stop();
            searchTimer.start();
        });

        // Status filter
        JLabel statusLabel = new JLabel("Status: ");
        String[] statuses = {"All", "ISSUED", "RETURNED", "OVERDUE"};
        statusFilter = new JComboBox<>(statuses);
        statusFilter.addActionListener(e -> applyFilters());

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);
        filterPanel.add(new JLabel("  ")); // Spacer

        refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadCurrentPage());
        filterPanel.add(refreshBtn);

        return filterPanel;
    }

    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new BorderLayout());
        
        // Page info + navigation
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prevBtn = new JButton("⬅️ Previous");
        nextBtn = new JButton("➡️ Next");
        pageInfoLabel = new JLabel("Page 1 (1-50 of 0)");
        
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        prevBtn.addActionListener(e -> previousPage());
        nextBtn.addActionListener(e -> nextPage());
        
        navPanel.add(prevBtn);
        navPanel.add(new JLabel("  "));
        navPanel.add(pageInfoLabel);
        navPanel.add(new JLabel("  "));
        navPanel.add(nextBtn);
        
        paginationPanel.add(navPanel, BorderLayout.WEST);
        return paginationPanel;
    }

    private void setupColumnComparators() {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            sorter.setComparator(i, createColumnComparator(i));
        }
    }

    private Comparator<Object> createColumnComparator(int columnIndex) {
        return (o1, o2) -> {
            return switch (columnIndex) {
                case 0, 1 -> { // IDs - Numeric
                    int n1 = o1 instanceof Number ? ((Number) o1).intValue() : 0;
                    int n2 = o2 instanceof Number ? ((Number) o2).intValue() : 0;
                    yield Integer.compare(n1, n2);
                }
                case 2, 3, 4 -> { // Dates - String (format: YYYY-MM-DD)
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

    private void loadPage(int page) {
        currentPage = page;
        tableModel.setRowCount(0);
        
        List<BookIssue> issues = issueDAO.findByUserWithPagination(userId, page * pageSize, pageSize);
        totalRecords = issueDAO.getUserIssueCount(userId);
        
        for (BookIssue bi : issues) {
            tableModel.addRow(new Object[]{
                    bi.getId(),
                    bi.getBookName(), 
                    bi.getBookId(),
                    bi.getIssueDate(),
                    bi.getDueDate(),
                    bi.getReturnDate(),
                    bi.getStatus() != null ? bi.getStatus() : "—"
            });
        }
        
        updatePaginationControls();
    }

    private void loadCurrentPage() {
        loadPage(currentPage);
    }

    private void previousPage() {
        if (currentPage > 0) {
            loadPage(currentPage - 1);
        }
    }

    private void nextPage() {
        if (hasMoreRecords()) {
            loadPage(currentPage + 1);
        }
    }

    private boolean hasMoreRecords() {
        return (currentPage + 1) * pageSize < totalRecords;
    }

    private void updatePaginationControls() {
        int startRecord = currentPage * pageSize + 1;
        int endRecord = Math.min(startRecord + pageSize - 1, totalRecords);
        
        pageInfoLabel.setText(String.format("Page %d (%d-%d of %d)", 
            currentPage + 1, startRecord, endRecord, totalRecords));
        
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(hasMoreRecords());
    }

    private void applyFilters() {
        RowFilter<DefaultTableModel, Object> filter = null;
        List<RowFilter<Object,Object>> filters = new ArrayList<>();

        // Search filter (all columns)
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }

        // Status filter
        String status = (String) statusFilter.getSelectedItem();
        if (!"All".equals(status)) {
            filters.add(RowFilter.regexFilter("(?i)" + status, 5));
        }

        if (!filters.isEmpty()) {
            filter = RowFilter.andFilter(filters);
        }

        sorter.setRowFilter(filter);
    }

    public void refreshData() {
        loadCurrentPage();
    }
}
