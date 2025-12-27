package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import controller.SignupController;
import util.CommonMethods;

public class SignupFrame extends JFrame {

    private JTextField firstNameField, lastNameField, usernameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleCombo;
    private JButton registerBtn, cancelBtn;
    private SignupController controller;

    public SignupFrame() {
        controller = new SignupController(this);
        initUI();
    }

    private void initUI() {
        setTitle("eLibrary - Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 440);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xF5F7FA));
        setContentPane(root);

        // Left branding panel (reused style)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(0x1E88E5));
        leftPanel.setPreferredSize(new Dimension(180, 0));

        JLabel appTitle = new JLabel("eLibrary");
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

        // Right form panel
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        rightPanel.setBackground(Color.WHITE);
        root.add(rightPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;

        JLabel titleLabel = new JLabel("Create account", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0x333333));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        rightPanel.add(titleLabel, gbc);
        row++;

        JLabel subtitleLabel = new JLabel("Fill in your details to sign up.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(0x777777));
        gbc.gridy = row;
        rightPanel.add(subtitleLabel, gbc);
        row++;
        
     // firstName
        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("First Name *"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(18);
        rightPanel.add(firstNameField, gbc);
        row++;
        
     // lastName
        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Last Name *"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(18);
        rightPanel.add(lastNameField, gbc);
        row++;
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Username *"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(18);
        rightPanel.add(usernameField, gbc);
        row++;

        // Email
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Email *"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(18);
        rightPanel.add(emailField, gbc);
        row++;

        // Password
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Password *"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(18);
        rightPanel.add(passwordField, gbc);
        row++;

        // Confirm password
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Confirm Password *"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(18);
        rightPanel.add(confirmPasswordField, gbc);
        row++;

        // Role
        gbc.gridy = row; gbc.gridx = 0;
        rightPanel.add(new JLabel("Role"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"GUEST", "STAFF"});
        rightPanel.add(roleCombo, gbc);
        row++;

        // Register + Cancel buttons
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);

        registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setBackground(new Color(0x1E88E5));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        registerBtn.addActionListener(e -> controller.register());

        cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(registerBtn);
        buttons.add(cancelBtn);
        rightPanel.add(buttons, gbc);
        row++;

        // Footer
        gbc.gridy = row;
        JLabel footer = new JLabel(
                "<html><center>e-Library Smart Library System<br/>Design and developed by Raj Singh</center></html>",
                JLabel.CENTER
        );
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(0x999999));
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        rightPanel.add(footer, gbc);
    }

    // Getters for controller
    public String getUsername() { return usernameField.getText().trim(); }
    public String getEmail() { return emailField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirmPassword() { return new String(confirmPasswordField.getPassword()); }
    public String getRole() { return (String) roleCombo.getSelectedItem(); }

    public void showMessage(String msg) {
    	CommonMethods.showMessage(this, msg);
    }

	public String getFirstname() {
		return firstNameField.getText().trim();
	}
	public String getLastname() {
		// TODO Auto-generated method stub
		return lastNameField.getText().trim();
	}
}
