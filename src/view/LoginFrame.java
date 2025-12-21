package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import controller.LoginController;
import model.User;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JButton loginBtn, signupBtn;
    private LoginController controller;

    public LoginFrame() {
        controller = new LoginController(this);
        initUI();
    }

    private void initUI() {
        setTitle("e-Library - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 380);
        setLocationRelativeTo(null);
        setResizable(false);

        // Root panel with background color
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xF5F7FA));
        setContentPane(root);

        // Left brand panel
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(0x1E88E5));
        leftPanel.setPreferredSize(new Dimension(170, 0));
        leftPanel.setLayout(new GridBagLayout());

        JLabel appTitle = new JLabel("e-Library");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel appSubtitle = new JLabel("Smart Library System");
        appSubtitle.setForeground(new Color(230, 240, 255));
        appSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        Box box = Box.createVerticalBox();
        box.add(appTitle);
        box.add(Box.createVerticalStrut(8));
        box.add(appSubtitle);

        leftPanel.add(box);
        root.add(leftPanel, BorderLayout.WEST);

        // Right login panel
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        rightPanel.setBackground(Color.WHITE);
        root.add(rightPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        // Title
        JLabel titleLabel = new JLabel("Sign in", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0x333333));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        rightPanel.add(titleLabel, gbc);
        row++;

        JLabel subtitleLabel = new JLabel("Welcome back! Please login to continue.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(0x777777));
        gbc.gridy = row;
        rightPanel.add(subtitleLabel, gbc);
        row++;

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Username"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(16);
        rightPanel.add(usernameField, gbc);
        row++;

        // Password
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(16);
        rightPanel.add(passwordField, gbc);
        row++;

        // Role
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Role"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"ADMIN", "STAFF", "GUEST"});
        rightPanel.add(roleCombo, gbc);
        row++;

        // Login button
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginBtn = new JButton("Login");
     // IMPORTANT: press Enter = click Login
        getRootPane().setDefaultButton(loginBtn);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(new Color(0x1E88E5));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        loginBtn.addActionListener(e -> {
			try {
		        controller.login();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
        rightPanel.add(loginBtn, gbc);
        row++;

        // Signup link
        gbc.gridy = row;
        JPanel signupPanel = new JPanel();
        signupPanel.setOpaque(false);
        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        signupBtn = new JButton("Sign up");
        signupBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        signupBtn.setBorderPainted(false);
        signupBtn.setContentAreaFilled(false);
        signupBtn.setForeground(new Color(0x1E88E5));
        signupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupBtn.addActionListener(e -> controller.showSignup());
        signupPanel.add(noAccountLabel);
        signupPanel.add(signupBtn);
        rightPanel.add(signupPanel, gbc);
        row++;

        // Footer: app info
        gbc.gridy = row;
        JLabel footer = new JLabel(
                "<html><center>e-Library Smart Library System<br/>© 2025 Raj Singh. All Rights Reserved.</center></html>",
                JLabel.CENTER
        );
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(0x999999));
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        rightPanel.add(footer, gbc);
    }

    // Getters used by controller
    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getRole() { return (String) roleCombo.getSelectedItem(); }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void openDashboard(User user) throws SQLException {
        new DashboardFrame(user).setVisible(true);
        dispose();
    }
}
