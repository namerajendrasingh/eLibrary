package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.BookDAO;
import model.BookIssueDAO;
import model.UserDAO;

public class DashboardStatsPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BookIssueDAO issueDAO = new BookIssueDAO();
    private final QuadConsumer<Integer, Integer, Integer, Integer> onDataRefresh;

    private JLabel totalBooksLabel;
    private JLabel availableBooksLabel;
    private JLabel totalUsersLabel;
    private JLabel activeIssuesLabel;

    public DashboardStatsPanel(QuadConsumer<Integer, Integer, Integer, Integer> onDataRefresh) {
        this.onDataRefresh = onDataRefresh;
        initUI();
        loadStats();
    }

    // ✅ NEW: Public refresh method for DashboardFrame
    public void refreshStats() {
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 8, 4));
        Font f = new Font("Segoe UI", Font.BOLD, 16);

        totalBooksLabel = new JLabel("Total Books: 0");
        availableBooksLabel = new JLabel("Available Copies: 0");
        totalUsersLabel = new JLabel("Total Users: 0");
        activeIssuesLabel = new JLabel("Active Issues: 0");

        totalBooksLabel.setFont(f);
        availableBooksLabel.setFont(f);
        totalUsersLabel.setFont(f);
        activeIssuesLabel.setFont(f);

        // Professional styling
        totalBooksLabel.setForeground(new Color(0x2196F3));
        availableBooksLabel.setForeground(new Color(0x4CAF50));
        totalUsersLabel.setForeground(new Color(0xFF9800));
        activeIssuesLabel.setForeground(new Color(0xF44336));

        statsGrid.add(totalBooksLabel);
        statsGrid.add(availableBooksLabel);
        statsGrid.add(totalUsersLabel);
        statsGrid.add(activeIssuesLabel);

        add(statsGrid, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void refresh() {
        int totalBooks = safeCount(bookDAO::countAllBooks);
        int available = safeCount(bookDAO::countAvailableBooks);
        int totalUsers = safeCount(userDAO::countAllUsers);
        int activeIssues = safeCount(issueDAO::countActiveIssues);

        // Update labels
        totalBooksLabel.setText("Total Books: " + totalBooks);
        availableBooksLabel.setText("Available Copies: " + available);
        totalUsersLabel.setText("Total Users: " + totalUsers);
        activeIssuesLabel.setText("Active Issues: " + activeIssues);

        // Notify charts with fresh data
        if (onDataRefresh != null) {
            onDataRefresh.accept(totalBooks, available, totalUsers, activeIssues);
        }
    }

    private void loadStats() {
        refresh();  // Reuse same logic
    }

    private int safeCount(Supplier<Integer> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}

