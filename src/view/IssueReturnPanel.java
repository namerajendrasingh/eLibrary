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
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import model.Book;
import model.BookDAO;
import model.BookIssue;
import model.BookIssueDAO;
import model.User;
import model.UserDAO;

public class IssueReturnPanel extends JPanel {

    private JTextField userSearchField;
    private JTextField bookSearchField;
    private JSpinner dueDateDaysSpinner;
    private JButton issueBtn;
    private JLabel selectedUserLabel;
    private JLabel selectedBookLabel;
    
    private int selectedUserId = -1;
    private int selectedBookId = -1;

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
    
    private boolean suppressUserSearch = false;
    private boolean suppressBookSearch = false;



    public IssueReturnPanel() {
        this.bookDAO = new BookDAO();
        this.issueDAO = new BookIssueDAO();
        this.userDAO = new UserDAO();
        initUI();
        loadPage(0);
    }
    
    private JPanel createIssuePanel() {
        JPanel issuePanel = new JPanel(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("Issue New Book"));
        ((TitledBorder)contentPanel.getBorder()).setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.setPreferredSize(new Dimension(360, 350));  // Slightly wider for better UX
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        
        // ✨ MODERN USER CARD
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        contentPanel.add(createModernUserSection(), gbc);

        // ✨ MODERN BOOK CARD  
        gbc.gridy = row++;
        contentPanel.add(createModernBookSection(), gbc);

        // Due Days (unchanged)
        gbc.gridy = row++; gbc.gridx = 0; gbc.gridwidth = 1;
        contentPanel.add(new JLabel("📅 Due (days):"), gbc);
        gbc.gridx = 1;
        dueDateDaysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 365, 1));
        ((JSpinner.DefaultEditor) dueDateDaysSpinner.getEditor()).getTextField().setColumns(8);
        dueDateDaysSpinner.setPreferredSize(new Dimension(140, 34));
        dueDateDaysSpinner.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        contentPanel.add(dueDateDaysSpinner, gbc);

        // ✨ HERO ISSUE BUTTON
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;
        issueBtn = new JButton("✅ Issue Book");
        issueBtn.setPreferredSize(new Dimension(150, 42));
        issueBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        issueBtn.setBackground(new Color(46, 125, 50));  // Green
        issueBtn.setForeground(Color.WHITE);
        issueBtn.setFocusPainted(false);
        issueBtn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        issueBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Hover effect
        issueBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                issueBtn.setBackground(new Color(67, 160, 71)); 
            }
            public void mouseExited(MouseEvent e) { 
                issueBtn.setBackground(new Color(46, 125, 50)); 
            }
        });
        issueBtn.addActionListener(e -> issueBook());
        contentPanel.add(issueBtn, gbc);

        issuePanel.add(contentPanel, BorderLayout.CENTER);
        return issuePanel;
    }
    
    // ✨ NEW: Modern User Selection Card
   private JPanel createModernUserSection() {

    JPanel userCard = new JPanel(new BorderLayout(12, 8));
    userCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
    ));
    userCard.setBackground(Color.WHITE);

    JLabel title = new JLabel("👤 Select User");
    title.setFont(new Font("Segoe UI", Font.BOLD, 13));
    title.setForeground(new Color(55, 71, 79));
    userCard.add(title, BorderLayout.NORTH);

    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    userSearchField = new JTextField(14);
    userSearchField.setPreferredSize(new Dimension(155, 36));
    userSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    userSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
    ));

    // 🔑 DEBOUNCE TIMER (FIXED)
    Timer userTimer = new Timer(500, e -> {
        if (!suppressUserSearch) {
            searchUsers();
        }
    });
    userTimer.setRepeats(false);

    userSearchField.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    if (!suppressUserSearch) userTimer.restart();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    if (!suppressUserSearch) userTimer.restart();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    if (!suppressUserSearch) userTimer.restart();
                }
            }
    );

    inputPanel.add(userSearchField);
    userCard.add(inputPanel, BorderLayout.CENTER);

    selectedUserLabel = new JLabel("👤 No user selected");
    selectedUserLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    selectedUserLabel.setForeground(new Color(117, 117, 117));
    selectedUserLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    userCard.add(selectedUserLabel, BorderLayout.SOUTH);

    return userCard;
}


   private JPanel createModernBookSection() {

    JPanel bookCard = new JPanel(new BorderLayout(12, 8));
    bookCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
    ));
    bookCard.setBackground(Color.WHITE);

    JLabel title = new JLabel("📚 Select Book");
    title.setFont(new Font("Segoe UI", Font.BOLD, 13));
    title.setForeground(new Color(55, 71, 79));
    bookCard.add(title, BorderLayout.NORTH);

    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    bookSearchField = new JTextField(14);
    bookSearchField.setPreferredSize(new Dimension(155, 36));
    bookSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    bookSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
    ));

    // 🔑 DEBOUNCE TIMER (FIXED)
    Timer bookTimer = new Timer(500, e -> {
        if (!suppressBookSearch) {
            searchBooks();
        }
    });
    bookTimer.setRepeats(false);

    bookSearchField.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    if (!suppressBookSearch) bookTimer.restart();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    if (!suppressBookSearch) bookTimer.restart();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    if (!suppressBookSearch) bookTimer.restart();
                }
            }
    );

    inputPanel.add(bookSearchField);
    bookCard.add(inputPanel, BorderLayout.CENTER);

    selectedBookLabel = new JLabel("📖 No book selected");
    selectedBookLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
    selectedBookLabel.setForeground(new Color(117, 117, 117));
    selectedBookLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    bookCard.add(selectedBookLabel, BorderLayout.SOUTH);

    return bookCard;
}

    private User showUserSelectionDialog(List<User> users) {
        final User[] selectedUser = { null };

        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Select User",
                true
        );
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Username", "ID", "Email"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        users.forEach(u ->
            model.addRow(new Object[]{u.getUsername(), u.getId(), u.getEmail()})
        );

        JTable table = new JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        Runnable selectAction = () -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                int id = (Integer) model.getValueAt(modelRow, 1);
                selectedUser[0] = users.stream()
                        .filter(u -> u.getId() == id)
                        .findFirst()
                        .orElse(null);
                dialog.dispose();
            }
        };

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) selectAction.run();
            }
        });

        table.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) selectAction.run();
            }
        });

        JButton selectBtn = new JButton("Select");
        selectBtn.addActionListener(e -> selectAction.run());

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(selectBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);

        return selectedUser[0];
    }


    private Book showBookSelectionDialog(List<Book> books) {
        final Book[] selectedBook = { null };

        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Select Book",
                true
        );
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Title", "Author", "ID", "Available"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        books.forEach(b ->
            model.addRow(new Object[]{
                    b.getTitle(),
                    b.getAuthor(),
                    b.getId(),
                    b.getAvailableCopies()
            })
        );

        JTable table = new JTable(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        Runnable selectAction = () -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                int id = (Integer) model.getValueAt(modelRow, 2);
                selectedBook[0] = books.stream()
                        .filter(b -> b.getId() == id)
                        .findFirst()
                        .orElse(null);
                dialog.dispose();
            }
        };

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) selectAction.run();
            }
        });

        table.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) selectAction.run();
            }
        });

        JButton selectBtn = new JButton("Select");
        selectBtn.addActionListener(e -> selectAction.run());

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(selectBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);

        return selectedBook[0];
    }

    private void searchUsers() {
        String text = userSearchField.getText().trim();
        if (text.isEmpty()) return;

        List<User> users = userDAO.searchByName(text);
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users found");
            return;
        }

        User u = showUserSelectionDialog(users);
        if (u != null) {
            selectedUserId = u.getId();          // ✅ ID stored
            suppressUserSearch = true;
            userSearchField.setText(u.getUsername()); // ✅ text set
            selectedUserLabel.setText("✓ " + u.getUsername());
            selectedUserLabel.setForeground(Color.GREEN);
        }
    }

    private void searchBooks() {
        String text = bookSearchField.getText().trim();
        if (text.isEmpty()) return;

        List<Book> books = bookDAO.searchByTitle(text);
        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No books found");
            return;
        }

        Book b = showBookSelectionDialog(books);
        if (b != null) {
            selectedBookId = b.getId();          // ✅ ID stored
            suppressBookSearch = true;
            bookSearchField.setText(b.getTitle()); // ✅ text set
            selectedBookLabel.setText("✓ " + b.getTitle());
            selectedBookLabel.setForeground(Color.GREEN);
        }
    }



    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createIssuePanel(), BorderLayout.WEST);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(createFilterPanel(), BorderLayout.NORTH);
        
        setupActiveIssuesTable();
        JScrollPane scrollPane = new JScrollPane(activeIssuesTable);
        TitledBorder border = BorderFactory.createTitledBorder("Active Issued Books");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        scrollPane.setBorder(border);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createPaginationPanel(), BorderLayout.WEST);
        bottomPanel.add(createButtonPanel(), BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    // ✅ UPDATED: Table shows Username & Book Title
    private void setupActiveIssuesTable() {
        activeIssuesModel = new DefaultTableModel(
                new Object[]{"Issue ID", "Username", "Book Title", "Issue Date", "Due Date", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        activeIssuesTable = new JTable(activeIssuesModel);
        
        sorter = new TableRowSorter<>(activeIssuesModel);
        activeIssuesTable.setRowSorter(sorter);
        setupColumnComparators();
        sorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING)));
        
        activeIssuesTable.setRowHeight(28);
        activeIssuesTable.setGridColor(new java.awt.Color(230, 230, 230));
        activeIssuesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        activeIssuesTable.setShowGrid(true);
        activeIssuesTable.setIntercellSpacing(new Dimension(0, 1));
        activeIssuesTable.getTableHeader().setReorderingAllowed(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        int[] widths = {70, 140, 200, 120, 120, 90};
        for (int i = 0; i < activeIssuesTable.getColumnCount(); i++) {
            activeIssuesTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (i == 1 || i == 2 || i == 5) {
                activeIssuesTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            } else {
                activeIssuesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        activeIssuesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        activeIssuesTable.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        activeIssuesTable.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));
    }

    private void setupColumnComparators() {
        for (int i = 0; i < activeIssuesModel.getColumnCount(); i++) {
            sorter.setComparator(i, createColumnComparator(i));
        }
    }

    private Comparator<Object> createColumnComparator(int columnIndex) {
        return (o1, o2) -> {
            return switch (columnIndex) {
                case 0 -> { // Issue ID
                    int n1 = o1 instanceof Number ? ((Number) o1).intValue() : 0;
                    int n2 = o2 instanceof Number ? ((Number) o2).intValue() : 0;
                    yield Integer.compare(n1, n2);
                }
                case 1, 2, 5 -> { // Text columns
                    String s1 = o1 != null ? o1.toString() : "";
                    String s2 = o2 != null ? o2.toString() : "";
                    yield s1.compareToIgnoreCase(s2);
                }
                case 3, 4 -> { // Dates
                    String s1 = o1 != null ? o1.toString() : "";
                    String s2 = o2 != null ? o2.toString() : "";
                    yield s1.compareTo(s2);
                }
                default -> 0;
            };
        };
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel searchLabel = new JLabel("🔍 Search: ");
        searchField = new JTextField(20);
        Timer searchTimer = new Timer(300, e -> applyFilters());
        searchTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
        });
        searchField.addActionListener(e -> {
            searchTimer.stop();
            searchTimer.start();
        });

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
    
    private void applyFilters() {
        RowFilter<DefaultTableModel, Object> filter = null;
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }

        String status = (String) statusFilter.getSelectedItem();
        if (!"All".equals(status)) {
            filters.add(RowFilter.regexFilter("(?i)" + status, 5));
        }

        if (!filters.isEmpty()) {
            filter = RowFilter.andFilter(filters);
        }
        sorter.setRowFilter(filter);
    }

    
    private void loadPage(int page) {
        currentPage = page;
        
        new Thread(() -> {
            try {
                List<BookIssue> issues = issueDAO.getActiveIssuesWithPagination(page * pageSize, pageSize);
                totalRecords = issueDAO.getActiveIssuesCount();
                
                SwingUtilities.invokeLater(() -> {
                    activeIssuesModel.setRowCount(0);
                    for (BookIssue bi : issues) {
                        User user = userDAO.findById(bi.getUserId());
                        Book book = bookDAO.findById(bi.getBookId());
                        activeIssuesModel.addRow(new Object[]{
                                bi.getId(),
                                user != null ? user.getUsername() : "Unknown",
                                book != null ? book.getTitle() : "Unknown",
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

    // ✅ UPDATED: Uses selected IDs instead of text fields
    private void issueBook() {
        if (selectedUserId == -1 || selectedBookId == -1) {
            JOptionPane.showMessageDialog(this, "Please search and select both User and Book first.", "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.findById(selectedUserId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Selected user not found.", "User Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean stockOk = bookDAO.decreaseAvailableCopies(selectedBookId);
        if (!stockOk) {
            JOptionPane.showMessageDialog(this, "Selected book not available.", "No Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int days = (int) dueDateDaysSpinner.getValue();
        LocalDate today = LocalDate.now();
        LocalDate dueDateLocal = today.plusDays(days);

        BookIssue issue = new BookIssue();
        issue.setUserId(selectedUserId);
        issue.setBookId(selectedBookId);
        issue.setIssueDate(Timestamp.valueOf(today.atStartOfDay()));
        issue.setDueDate(Timestamp.valueOf(dueDateLocal.atStartOfDay()));
        issue.setStatus("ISSUED");

        boolean ok = issueDAO.issueBook(issue);
        if (ok) {
            JOptionPane.showMessageDialog(this, "✅ Book issued successfully to " + user.getUsername() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPage(currentPage);  // Refresh current page
            // Reset selection
            resetIssueForm();
        } else {
            bookDAO.increaseAvailableCopies(selectedBookId);
            JOptionPane.showMessageDialog(this, "❌ Failed to issue book. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ✅ UPDATED: Return uses Issue ID from first column
    private void returnSelectedIssue() {
        int row = activeIssuesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an issue to return.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = activeIssuesTable.convertRowIndexToModel(row);
        int issueId = (Integer) activeIssuesModel.getValueAt(modelRow, 0);
        
        // Get book ID by re-fetching the issue
        BookIssue issue = issueDAO.findById(issueId);
        if (issue == null) {
            JOptionPane.showMessageDialog(this, "Issue not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Return issue #%d?\nBook: %s", issueId, activeIssuesModel.getValueAt(modelRow, 2)),
                "Confirm Return",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean okIssue = issueDAO.returnBook(issueId);
            boolean okStock = bookDAO.increaseAvailableCopies(issue.getBookId());

            if (okIssue && okStock) {
                JOptionPane.showMessageDialog(this, "✅ Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPage(currentPage);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to return book. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void resetIssueForm() {

        suppressUserSearch = true;
        suppressBookSearch = true;

        selectedUserId = -1;
        selectedBookId = -1;

        userSearchField.setText("");
        bookSearchField.setText("");

        selectedUserLabel.setText("👤 No user selected");
        selectedUserLabel.setForeground(new Color(117, 117, 117));

        selectedBookLabel.setText("📖 No book selected");
        selectedBookLabel.setForeground(new Color(117, 117, 117));

        dueDateDaysSpinner.setValue(14);

        // 🔓 RE-ENABLE AUTO SEARCH AFTER RESET
        SwingUtilities.invokeLater(() -> {
            suppressUserSearch = false;
            suppressBookSearch = false;
        });
    }


    public void refreshData() {
        loadPage(currentPage);
    }
}
