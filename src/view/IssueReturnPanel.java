package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import model.Book;
import model.BookDAO;
import model.BookIssue;
import model.BookIssueDAO;
import model.User;
import model.UserDAO;
import util.CommonMethods;

public class IssueReturnPanel extends JPanel {
    
    // Constants
    private static final int PAGE_SIZE = 25;
    private static final int DEBOUNCE_MS = 500;
    private static final String[] STATUSES = {"All", "ISSUED", "OVERDUE"};
    private static final String[] TABLE_COLS = {"Issue ID", "Username", "Book Title", "Issue Date", "Due Date", "Status"};
    
    // State
    private int selectedUserId = -1, selectedBookId = -1, currentPage = 0, totalRecords = 0;
    private final BookDAO bookDAO = new BookDAO();
    private final BookIssueDAO issueDAO = new BookIssueDAO();
    private final UserDAO userDAO = new UserDAO();
    
    // UI Components
    private final JTextField userSearchField = new JTextField(14);
    private final JTextField bookSearchField = new JTextField(14);
    private final JSpinner dueDateDaysSpinner = createSpinner();
    private final JLabel selectedUserLabel = new JLabel("👤 No user selected");
    private final JLabel selectedBookLabel = new JLabel("📖 No book selected");
    private final JTable activeIssuesTable = new JTable();
    private final DefaultTableModel activeIssuesModel = new DefaultTableModel(TABLE_COLS, 0);
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Filter/Pagination
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JButton prevBtn, nextBtn, refreshBtn, returnBtn;
    private JLabel pageInfoLabel;
    private boolean suppressSearch = false;

    public IssueReturnPanel() {
        activeIssuesModel.setColumnIdentifiers(TABLE_COLS);
        activeIssuesModel.setNumRows(0);
        initUI();
        loadPage(0);
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        add(createIssuePanel(), BorderLayout.WEST);
        add(createIssuesPanel(), BorderLayout.CENTER);
    }

    private JPanel createIssuePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("Issue New Book"));
        panel.setPreferredSize(new Dimension(360, 350));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // User card
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(createSelectionCard("👤 Select User", userSearchField, selectedUserLabel, this::searchUsers), gbc);
        
        // Book card
        gbc.gridy = row++;
        panel.add(createSelectionCard("📚 Select Book", bookSearchField, selectedBookLabel, this::searchBooks), gbc);
        
        // Due days
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("📅 Due (days):"), gbc);
        gbc.gridx = 1;
        panel.add(dueDateDaysSpinner, gbc);
        
        // Issue button
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(createIssueButton(), gbc);
        
        return wrapCenter(panel);
    }

    
    private JPanel createSelectionCard(String title, JTextField field, JLabel label, Runnable onSearch) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(55, 71, 79));
        card.add(titleLabel, BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        field.setPreferredSize(new Dimension(155, 36));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setCaretColor(Color.BLUE);
        
        setupDebounce(field, DEBOUNCE_MS, onSearch);
        
        inputPanel.add(field);
        inputPanel.add(createSearchButton("🔍", onSearch));
        card.add(inputPanel, BorderLayout.CENTER);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(new Color(117, 117, 117));
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        card.add(label, BorderLayout.SOUTH);
        
        return card;
    }

    private JButton createSearchButton(String icon, Runnable onClick) {
        return new JButton(icon) {{
            setPreferredSize(new Dimension(85, 36));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addActionListener(e -> onClick.run());
        }};
    }

    private JButton createIssueButton() {
        return new JButton("✅ Issue Book") {{
            setPreferredSize(new Dimension(150, 42));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setBackground(new Color(46, 125, 50));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(new Color(67, 160, 71)); }
                public void mouseExited(MouseEvent e) { setBackground(new Color(46, 125, 50)); }
            });
            addActionListener(e -> IssueReturnPanel.this.issueBook());
        }};
    }

    private void setupDebounce(JTextField field, int delay, Runnable action) {
        Timer timer = new Timer(delay, e -> {
            if (!suppressSearch) action.run();
            ((Timer)e.getSource()).stop();
        });
        timer.setRepeats(false);
        
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { if (!suppressSearch) timer.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { if (!suppressSearch) timer.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { if (!suppressSearch) timer.restart(); }
        });
    }

    private <T> T showSelectionDialog(String title, List<T> items, int idCol, Function<T, Integer> idGetter, Function<T, String> nameGetter) {
        final T[] selected = (T[]) new Object[1];
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(title.contains("User") ? 450 : 500, 300);
        dialog.setLocationRelativeTo(this);
        
        String[] cols = title.contains("User") ? 
            new String[]{"Username", "ID", "Email"} : 
            new String[]{"Title", "Author", "ID", "Available"};
            
        DefaultTableModel model = new DefaultTableModel(cols, 0) {{
            setNumRows(0);
            for (T item : items) {
                if (item instanceof User u) {
                    addRow(new Object[]{u.getUsername(), u.getId(), u.getEmail()});
                } else if (item instanceof Book b) {
                    addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getId(), b.getAvailableCopies()});
                }
            }
        }};
        
        JTable table = new JTable(model) {{
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getTableHeader().setReorderingAllowed(false);
            setRowHeight(28);
        }};
        
        Runnable selectAction = () -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                int id = (Integer) model.getValueAt(modelRow, idCol);
                selected[0] = items.stream().filter(i -> idGetter.apply((T) i).equals(id)).findFirst().orElse(null);
                dialog.dispose();
            }
        };
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) selectAction.run(); }
        });
        table.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) selectAction.run(); }
        });
        
        JButton selectBtn = new JButton("✅ Select");
        selectBtn.addActionListener(e -> selectAction.run());
        
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(selectBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
        
        return selected[0];
    }

    private void searchUsers() {
        String text = userSearchField.getText().trim();
        if (text.isEmpty()) return;
        
        List<User> users = userDAO.searchByName(text);
        if (users.isEmpty()) {
            CommonMethods.showWarning(this, "No users found");
            return;
        }
        
        User user = showSelectionDialog("Select User", users, 1, User::getId, User::getUsername);
        if (user != null) {
            suppressSearch = true;
            selectedUserId = user.getId();
            userSearchField.setText(user.getUsername());
            selectedUserLabel.setText("✓ " + user.getUsername());
            selectedUserLabel.setForeground(Color.GREEN);
            SwingUtilities.invokeLater(() -> suppressSearch = false);
        }
    }
    
    private void searchBooks() {
        String text = bookSearchField.getText().trim();
        if (text.isEmpty()) return;
        
        List<Book> books = bookDAO.searchByTitle(text);
        if (books.isEmpty()) {
            CommonMethods.showWarning(this, "No books found");
            return;
        }
        
        Book book = showSelectionDialog("Select Book", books, 2, Book::getId, Book::getTitle);
        if (book != null) {
            suppressSearch = true;
            selectedBookId = book.getId();
            bookSearchField.setText(book.getTitle());
            selectedBookLabel.setText("✓ " + book.getTitle());
            selectedBookLabel.setForeground(Color.GREEN);
            SwingUtilities.invokeLater(() -> suppressSearch = false);
        }
    }

    private void issueBook() {
        if (selectedUserId == -1 || selectedBookId == -1) {
            CommonMethods.showWarning(this, "Please select both User and Book");
            return;
        }
        
        User user = userDAO.findById(selectedUserId);
        if (user == null || !bookDAO.decreaseAvailableCopies(selectedBookId)) {
            CommonMethods.showWarning(this, "Invalid selection or no stock");
            return;
        }
        
        int days = (int) dueDateDaysSpinner.getValue();
        LocalDate today = LocalDate.now();
        
        BookIssue issue = new BookIssue();
        issue.setUserId(selectedUserId);
        issue.setBookId(selectedBookId);
        issue.setIssueDate(Timestamp.valueOf(today.atStartOfDay()));
        issue.setDueDate(Timestamp.valueOf(today.plusDays(days).atStartOfDay()));
        issue.setStatus("ISSUED");
            
        if (issueDAO.issueBook(issue)) {
            CommonMethods.showMessage(this, "✅ Book issued to " + user.getUsername());
            resetForm();
            loadPage(currentPage);
        } else {
            bookDAO.increaseAvailableCopies(selectedBookId);
            CommonMethods.showError(this, "Failed to issue book");
        }
    }
    
    private void resetForm() {
        suppressSearch = true;
        selectedUserId = selectedBookId = -1;
        userSearchField.setText(""); 
        bookSearchField.setText("");
        selectedUserLabel.setText("👤 No user selected"); 
        selectedUserLabel.setForeground(new Color(117, 117, 117));
        selectedBookLabel.setText("📖 No book selected"); 
        selectedBookLabel.setForeground(new Color(117, 117, 117));
        dueDateDaysSpinner.setValue(14);
        SwingUtilities.invokeLater(() -> suppressSearch = false);
    }

    // Issues table methods
    private JPanel createIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(createFilterPanel(), BorderLayout.NORTH);
        
        setupTable();
        JScrollPane scrollPane = new JScrollPane(activeIssuesTable);
        scrollPane.setBorder(createTitledBorder("Active Issued Books"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(createBottomPanel(), BorderLayout.SOUTH);
        return panel;
    }
    
    private void setupTable() {
    activeIssuesTable.setModel(activeIssuesModel);
    sorter = new TableRowSorter<>(activeIssuesModel);
    activeIssuesTable.setRowSorter(sorter);
    
    // Setup renderers
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
    leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
    
    int[] widths = {70, 140, 200, 120, 120, 90};
    for (int i = 0; i < TABLE_COLS.length; i++) {
        TableColumn col = activeIssuesTable.getColumnModel().getColumn(i);
        col.setPreferredWidth(widths[i]);
        col.setCellRenderer(i == 1 || i == 2 || i == 5 ? leftRenderer : centerRenderer);
        sorter.setComparator(i, createColumnComparator(i));  // ✅ Fixed comparator
    }
    
    activeIssuesTable.setRowHeight(28);
    activeIssuesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    activeIssuesTable.setShowGrid(true);
    activeIssuesTable.setGridColor(new Color(230, 230, 230));
    sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
    
    // Header styling
    activeIssuesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    activeIssuesTable.getTableHeader().setBackground(new Color(248, 249, 250));
    activeIssuesTable.getTableHeader().setForeground(new Color(33, 33, 33));
    activeIssuesTable.getTableHeader().setReorderingAllowed(true);
}

    private Comparator<Object> createColumnComparator(int col) {
        return switch (col) {
            case 0 -> (o1, o2) -> {
                int n1 = o1 instanceof Number ? ((Number) o1).intValue() : 0;
                int n2 = o2 instanceof Number ? ((Number) o2).intValue() : 0;
                return Integer.compare(n1, n2);
            };
            case 1, 2, 5 -> (o1, o2) -> {
                String s1 = o1 != null ? o1.toString() : "";
                String s2 = o2 != null ? o2.toString() : "";
                return s1.compareToIgnoreCase(s2);
            };
            case 3, 4 -> (o1, o2) -> {
                String s1 = o1 != null ? o1.toString() : "";
                String s2 = o2 != null ? o2.toString() : "";
                return s1.compareTo(s2);
            };
            default -> (o1, o2) -> 0;
        };
    }


    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        searchField = new JTextField(20);
        statusFilter = new JComboBox<>(STATUSES);
        
        setupDebounce(searchField, 300, this::applyFilters);
        statusFilter.addActionListener(e -> applyFilters());
        
        panel.add(new JLabel("🔍 Search: "));
        panel.add(searchField);
        panel.add(new JLabel("Status: "));
        panel.add(statusFilter);
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(createPaginationPanel(), BorderLayout.WEST);
        bottom.add(createButtonPanel(), BorderLayout.EAST);
        return bottom;
    }
    
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        prevBtn = new JButton("⬅️ Previous");
        nextBtn = new JButton("➡️ Next");
        pageInfoLabel = new JLabel("Page 1 (1-25 of 0)");
        
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        prevBtn.addActionListener(e -> previousPage());
        nextBtn.addActionListener(e -> nextPage());
        
        panel.add(prevBtn);
        panel.add(pageInfoLabel);
        panel.add(nextBtn);
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        refreshBtn = new JButton("🔄 Refresh");
        returnBtn = new JButton("↩️ Return Selected");
        
        refreshBtn.addActionListener(e -> loadCurrentPage());
        returnBtn.addActionListener(e -> returnSelectedIssue());
        
        panel.add(refreshBtn);
        panel.add(returnBtn);
        return panel;
    }
    
    private void applyFilters() {
        List<RowFilter<Object,Object>> filters = new ArrayList<>();
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }
        String status = (String) statusFilter.getSelectedItem();
        if (!"All".equals(status)) {
            filters.add(RowFilter.regexFilter("(?i)" + status, 5));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
    
    private void loadPage(int page) {
        currentPage = page;
        new SwingWorker<List<BookIssue>, Void>() {
            @Override protected List<BookIssue> doInBackground() {
                return issueDAO.getActiveIssuesWithPagination(page * PAGE_SIZE, PAGE_SIZE);
            }
            @Override protected void done() {
                try {
                    totalRecords = issueDAO.getActiveIssuesCount();
                    activeIssuesModel.setRowCount(0);
                    get().forEach(this::addTableRow);
                    updatePagination();  // ✅ Now defined
                } catch (Exception e) {
                    CommonMethods.showError(IssueReturnPanel.this, "Load failed: " + e.getMessage());
                }
            }
            private void addTableRow(BookIssue issue) {
                User user = userDAO.findById(issue.getUserId());
                Book book = bookDAO.findById(issue.getBookId());
                activeIssuesModel.addRow(new Object[]{
                    issue.getId(),
                    user != null ? user.getUsername() : "Unknown",
                    book != null ? book.getTitle() : "Unknown",
                    formatDate(issue.getIssueDate()),
                    formatDate(issue.getDueDate()),
                    issue.getStatus() != null ? issue.getStatus() : "—"
                });
            }
        }.execute();
    }
    
    // ✅ MISSING METHOD - Now defined!
    private void updatePagination() {
        int start = currentPage * PAGE_SIZE + 1;
        int end = Math.min(start + PAGE_SIZE - 1, totalRecords);
        pageInfoLabel.setText(String.format("Page %d (%d-%d of %d)", 
            currentPage + 1, start, end, totalRecords));
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled((currentPage + 1) * PAGE_SIZE < totalRecords);
    }
    
    private void previousPage() {
        if (currentPage > 0) loadPage(currentPage - 1);
    }
    
    private void nextPage() {
        if ((currentPage + 1) * PAGE_SIZE < totalRecords) loadPage(currentPage + 1);
    }
    
    private void loadCurrentPage() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        applyFilters();
        loadPage(currentPage);
    }
    
    private void returnSelectedIssue() {
        int row = activeIssuesTable.getSelectedRow();
        if (row < 0) {
            CommonMethods.showWarning(this, "Please select an issue to return.");
            return;
        }

        int modelRow = activeIssuesTable.convertRowIndexToModel(row);
        int issueId = (Integer) activeIssuesModel.getValueAt(modelRow, 0);
        BookIssue issue = issueDAO.findById(issueId);
        
        if (issue == null) {
            CommonMethods.showError(this, "Issue not found.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Return issue #%d?\nBook: %s", issueId, activeIssuesModel.getValueAt(modelRow, 2)),
            "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean okIssue = issueDAO.returnBook(issueId);
            boolean okStock = bookDAO.increaseAvailableCopies(issue.getBookId());
            if (okIssue && okStock) {
                CommonMethods.showMessage(this, "✅ Book returned successfully!");
                loadPage(currentPage);
            } else {
                CommonMethods.showError(this, "❌ Failed to return book.");
            }
        }
    }

    // Utility methods
    private static JSpinner createSpinner() {
        return new JSpinner(new SpinnerNumberModel(14, 1, 365, 1)) {{
            ((JSpinner.DefaultEditor) getEditor()).getTextField().setColumns(8);
            setPreferredSize(new Dimension(140, 34));
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }};
    }
    
    private static TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        return border;
    }
    
    private static JPanel wrapCenter(JPanel panel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }
    
    private String formatDate(Timestamp ts) {
        return ts != null ? ts.toString().split(" ")[0] : "—";
    }
    
    public void refreshData() {
        loadPage(currentPage);
    }
}
