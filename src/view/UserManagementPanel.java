package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.User;
import model.UserDAO;

public class UserManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton refreshBtn, addBtn, editBtn, deleteBtn;
    private UserDAO userDAO;

    public UserManagementPanel() {
        this.userDAO = new UserDAO();
        initUI();
        loadUsers();
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

        // Table
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Username", "Email", "Role"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // non-editable directly
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
                case 1: case 2: // Username, Email → LEFT
                    table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
                    break;
                default: // ID, Role → CENTER
                    table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // ✅ Professional header styling
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(248, 249, 250));
        table.getTableHeader().setForeground(new java.awt.Color(33, 33, 33));
        table.getTableHeader().setReorderingAllowed(false);

        // Optimal column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(180); // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(220); // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Role

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        scrollPane.setPreferredSize(new Dimension(800, 450));
        add(scrollPane, BorderLayout.CENTER);

        // Professional buttons
        refreshBtn = new JButton("🔄 Refresh");
        addBtn = new JButton("➕ Add User");
        editBtn = new JButton("✏️ Edit User");
        deleteBtn = new JButton("🗑️ Delete User");

        refreshBtn.addActionListener(e -> loadUsers());
        addBtn.addActionListener(e -> addUser());
        editBtn.addActionListener(e -> editSelectedUser());
        deleteBtn.addActionListener(e -> deleteSelectedUser());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRole() != null ? u.getRole() : "—"
            });
        }
    }

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
                loadUsers();
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
        panel.add(new JLabel(username)); // not editable
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
                loadUsers();
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
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        loadUsers();
    }

}
