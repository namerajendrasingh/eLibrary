package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import model.User;
import model.UserDAO;

public class UserManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton refreshBtn, addBtn, editBtn, deleteBtn, prevBtn, nextBtn;
    private JLabel pageInfoLabel;
    private JTextField searchField;
    private JComboBox<String> roleFilter;
    private UserDAO userDAO;

    // ✅ PAGINATION STATE
    private int currentPage = 0;
    private int pageSize = 50;
    private int totalRecords = 0;

    public UserManagementPanel() {
        this.userDAO = new UserDAO();
        initUI();
        loadPage(0);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("👥 User Management");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        titleLabel.setForeground(new java.awt.Color(51, 51, 51));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ✅ FILTER PANEL
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Table model
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Username", "Email", "Role"},
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
                case 1, 2 -> table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer); // Username, Email → LEFT
                default -> table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);   // ID, Role → CENTER
            }
        }

        // Professional header styling
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        table.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));

        // Optimal column widths
        int[] widths = {70, 180, 220, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        scrollPane.setPreferredSize(new Dimension(800, 350));
        add(scrollPane, BorderLayout.CENTER);

        // ✅ PAGINATION + BUTTONS PANEL
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(createPaginationPanel(), BorderLayout.WEST);
        southPanel.add(createButtonPanel(), BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Global search
        JLabel searchLabel = new JLabel("🔍 Search: ");
        searchField = new JTextField(18);
        searchField.addActionListener(e -> applyFilters());
        
        // Debounce search (300ms)
        Timer searchTimer = new Timer(300, e -> applyFilters());
        searchTimer.setRepeats(false);
        searchField.addActionListener(e -> {
            searchTimer.stop();
            searchTimer.start();
        });

        // Role filter
        JLabel roleLabel = new JLabel("Role: ");
        String[] roles = {"All", "ADMIN", "STAFF", "GUEST", "MEMBER"};
        roleFilter = new JComboBox<>(roles);
        roleFilter.addActionListener(e -> applyFilters());

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(roleLabel);
        filterPanel.add(roleFilter);

        return filterPanel;
    }

    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        
        prevBtn = new JButton("⬅️ Previous");
        nextBtn = new JButton("➡️ Next");
        pageInfoLabel = new JLabel("Page 1 (1-50 of 0)");
        
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        prevBtn.addActionListener(e -> previousPage());
        nextBtn.addActionListener(e -> nextPage());
        
        paginationPanel.add(prevBtn);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(nextBtn);
        
        return paginationPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        
        refreshBtn = new JButton("🔄 Refresh");
        addBtn = new JButton("➕ Add User");
        editBtn = new JButton("✏️ Edit User");
        deleteBtn = new JButton("🗑️ Delete User");

        refreshBtn.addActionListener(e -> loadCurrentPage());
        addBtn.addActionListener(e -> addUser());
        editBtn.addActionListener(e -> editSelectedUser());
        deleteBtn.addActionListener(e -> deleteSelectedUser());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        
        return buttonPanel;
    }

    private void setupColumnComparators() {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            sorter.setComparator(i, createColumnComparator(i));
        }
    }

    private Comparator<Object> createColumnComparator(int columnIndex) {
        return (o1, o2) -> {
            return switch (columnIndex) {
                case 0 -> { // ID - Numeric
                    int n1 = o1 instanceof Number ? ((Number) o1).intValue() : 0;
                    int n2 = o2 instanceof Number ? ((Number) o2).intValue() : 0;
                    yield Integer.compare(n1, n2);
                }
                case 1, 2, 3 -> { // Username, Email, Role - String
                    String s1 = o1 != null ? o1.toString() : "";
                    String s2 = o2 != null ? o2.toString() : "";
                    yield s1.compareToIgnoreCase(s2);
                }
                default -> 0;
            };
        };
    }
    
    /**
     * ✅ FIXED: Async non-blocking loadPage (NO UI FREEZE + NO LEAKS)
     */
    private void loadPage(int page) {
        currentPage = page;
        
        // ✅ BACKGROUND THREAD - No UI blocking!
        new Thread(() -> {
            try {
                List<User> users = userDAO.getUsersWithPagination(page * pageSize, pageSize);
                int total = userDAO.getTotalUserCount();
                
                // ✅ UI THREAD - Safe table update
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (User u : users) {
                        tableModel.addRow(new Object[]{
                                u.getId(),
                                u.getUsername(),
                                u.getEmail() != null ? u.getEmail() : "—",
                                u.getRole() != null ? u.getRole() : "—"
                        });
                    }
                    totalRecords = total;
                    updatePaginationControls();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "❌ Failed to load users: " + e.getMessage(), 
                        "Load Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "UserLoader-" + page).start();  // Named thread
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

        // Global search (all columns)
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText));
        }

        // Role filter (column 3)
        String role = (String) roleFilter.getSelectedItem();
        if (!"All".equals(role)) {
            filters.add(RowFilter.regexFilter("(?i)" + role, 3));
        }

        if (!filters.isEmpty()) {
            filter = RowFilter.andFilter(filters);
        }

        sorter.setRowFilter(filter);
    }

    // ✅ EXISTING METHODS (unchanged)
    private void addUser() {
        JTextField usernameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "STAFF", "GUEST"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        panel.add(new JLabel("👤 Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("📧 Email:"));
        panel.add(emailField);
        panel.add(new JLabel("🔒 Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("🎭 Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "➕ Add New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User user = new User(username, password, email, role);
            boolean ok = userDAO.register(user);
            if (ok) {
                JOptionPane.showMessageDialog(this, "✅ User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCurrentPage();  // ✅ Reload current page
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to add user. Check logs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "❌ Please select a user to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int userId = (Integer) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);
        String email = (String) tableModel.getValueAt(modelRow, 2);
        String role = (String) tableModel.getValueAt(modelRow, 3);

        JTextField emailField = new JTextField(email, 15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "STAFF", "GUEST"});
        roleCombo.setSelectedItem(role);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        panel.add(new JLabel("👤 Username:"));
        panel.add(new JLabel(username));
        panel.add(new JLabel("📧 Email:"));
        panel.add(emailField);
        panel.add(new JLabel("🎭 Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "✏️ Edit User: " + username,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newEmail = emailField.getText().trim();
            String newRole = (String) roleCombo.getSelectedItem();
            if (newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Email cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User user = new User();
            user.setId(userId);
            user.setEmail(newEmail);
            user.setRole(newRole);
            boolean ok = userDAO.updateUser(user);
            if (ok) {
                JOptionPane.showMessageDialog(this, "✅ User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCurrentPage();  // ✅ Reload current page
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "❌ Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int userId = (Integer) tableModel.getValueAt(modelRow, 0);
        String username = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("🗑️ Delete user '%s' (ID: %d)?\nThis action cannot be undone.",
                        username, userId),
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = userDAO.deleteUser(userId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "✅ User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCurrentPage();  // ✅ Reload current page
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        loadCurrentPage();
    }
}
