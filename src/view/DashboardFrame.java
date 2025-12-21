package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import model.BookDAO;
import model.BookIssueDAO;
import model.DashboardDAO;
import model.User;
import model.UserDAO;

public class DashboardFrame extends JFrame {

    private final User user;
    private final UserDAO userDAO;
    private final BookDAO bookDAO;
    private final BookIssueDAO issueDAO;
    private final DashboardDAO dashboardDAO;

    private DashboardTabsPanel tabsPanel;
    private DashboardStatsPanel statsPanel;
    private int totalBooks, totalIssued, totalMembers, totalOverdue;

    public DashboardFrame(User user) throws SQLException {
        this.user = user;
        this.userDAO = new UserDAO();
        this.issueDAO = new BookIssueDAO();
        this.bookDAO = new BookDAO();
        this.dashboardDAO = new DashboardDAO();
        initUI();
        
        // ✅ Install inactivity watcher
        util.InactivityWatcher.install(() -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "You have been logged out due to inactivity.",
                    "Session Timeout", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                new LoginFrame().setVisible(true);
            });
        });
    }

    private void initUI() throws SQLException {
        // ✅ Initial stats load
        loadDashboardStats();

        setTitle("e-Library Dashboard - " + user.getRole() + " (" + user.getUsername() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ✅ Modularized components with FULL utility support
        tabsPanel = new DashboardTabsPanel(user);
        
        statsPanel = new DashboardStatsPanel(
            (totalBooks, available, totalUsers, activeIssues) -> {
                this.totalBooks = totalBooks;
                this.totalIssued = activeIssues;
                this.totalMembers = totalUsers;
                try {
                    this.totalOverdue = dashboardDAO.countOverdueBooks();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                // Refresh all tabs
                if (tabsPanel != null) {
                    tabsPanel.refreshAllData();
                }
            }
        );

        // ✅ FIXED: Pass ALL callbacks including refresh
        DashboardSidebar sidebar = new DashboardSidebar(
            user,
            this::logout,
            this::showProfileDialog,
            this::showSettingsDialog,
            title -> tabsPanel.selectTabByTitle(title),
            this::refreshDashboard  // ✅ GLOBAL REFRESH
        );

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        center.add(statsPanel, BorderLayout.NORTH);
        center.add(tabsPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
    }

    // ✅ GLOBAL REFRESH - Called by Sidebar
    private void refreshDashboard() {
        try {
            loadDashboardStats();
            if (statsPanel != null) {
                statsPanel.refreshStats();
            }
            if (tabsPanel != null) {
                tabsPanel.refreshAllData();
            }
            JOptionPane.showMessageDialog(this, "✅ Dashboard refreshed successfully!", 
                "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Refresh failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ✅ Load initial dashboard stats
    private void loadDashboardStats() throws SQLException {
        totalBooks = bookDAO.countAllBooks();
        totalMembers = userDAO.countAllUsers();
        totalIssued = issueDAO.countActiveIssues();
        totalOverdue = dashboardDAO.countOverdueBooks();
    }

    //Logout Method
    private void logout() {
        int res = JOptionPane.showConfirmDialog(this, "Do you really want to logout?",
            "Logout Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // ===== Profile dialog =====
    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "👤 User Profile", true);
        dialog.setSize(450, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(UIManager.getColor("Panel.background"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        // Username (read-only)
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("👤 Username:"), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(user.getUsername(), 18);
        usernameField.setEditable(false);
        usernameField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        panel.add(usernameField, gbc);
        row++;

        // Email (editable)
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("📧 Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(user.getEmail(), 18);
        panel.add(emailField, gbc);
        row++;

        // Role (read-only)
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("🎭 Role:"), gbc);
        gbc.gridx = 1;
        JTextField roleField = new JTextField(user.getRole(), 12);
        roleField.setEditable(false);
        roleField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        panel.add(roleField, gbc);
        row++;

        // New Password
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("🔒 New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(18);
        panel.add(passwordField, gbc);
        row++;

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("🔐 Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmField = new JPasswordField(18);
        panel.add(confirmField, gbc);
        row++;

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JButton saveBtn = new JButton("💾 Save Changes");
        JButton cancelBtn = new JButton("❌ Cancel");
        
        saveBtn.addActionListener(e -> {
            String newEmail = emailField.getText().trim();
            String newPass = new String(passwordField.getPassword()).trim();
            String confirm = new String(confirmField.getPassword()).trim();

            if (newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "❌ Email cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.isEmpty() && !newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "❌ Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            user.setEmail(newEmail);
            boolean okEmail = userDAO.updateUser(user);
            boolean okPwd = true;
            if (!newPass.isEmpty()) {
                okPwd = userDAO.updatePassword(user.getId(), newPass);
            }

            if (okEmail && okPwd) {
                JOptionPane.showMessageDialog(dialog, "✅ Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshDashboard(); // Refresh after profile update
            } else {
                JOptionPane.showMessageDialog(dialog, "❌ Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(buttonPanel, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ===== Settings dialog =====
    private void showSettingsDialog() {
        JDialog dialog = new JDialog(this, "⚙️ Application Settings", true);
        dialog.setSize(450, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("🎨 Theme:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> themeCombo = new JComboBox<>(new String[]{"Light", "Dark"});
        themeCombo.setPreferredSize(new Dimension(150, 28));
        panel.add(themeCombo, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JButton applyBtn = new JButton("✅ Apply Theme");
        JButton closeBtn = new JButton("❌ Close");
        
        applyBtn.addActionListener(e -> {
            String selected = (String) themeCombo.getSelectedItem();
            boolean dark = "Dark".equals(selected);
            applyTheme(dark);
        });

        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(applyBtn);
        buttonPanel.add(closeBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // Apply Theme
    private void applyTheme(boolean dark) {
        try {
            if (dark) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            }
            SwingUtilities.updateComponentTreeUI(this);
            this.pack();
            JOptionPane.showMessageDialog(this, "✅ Theme applied successfully!", "Theme Changed", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Failed to apply theme: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
