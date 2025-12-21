package view;

import java.awt.BorderLayout;
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

public class ReportsStatsPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BookIssueDAO issueDAO = new BookIssueDAO();
    private final QuadConsumer<Integer, Integer, Integer, Integer> onDataRefresh;

    private JLabel totalBooksLabel;
    private JLabel availableBooksLabel;
    private JLabel totalUsersLabel;
    private JLabel activeIssuesLabel;

    public ReportsStatsPanel(QuadConsumer<Integer, Integer, Integer, Integer> onDataRefresh) {
        this.onDataRefresh = onDataRefresh;
        initUI();
        loadStats();
    }

    // ✅ PUBLIC: Refresh method for ReportsPanel
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

        statsGrid.add(totalBooksLabel);
        statsGrid.add(availableBooksLabel);
        statsGrid.add(totalUsersLabel);
        statsGrid.add(activeIssuesLabel);

        add(statsGrid, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshStats());  // ✅ Use public method
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

        // Notify charts
        if (onDataRefresh != null) {
            onDataRefresh.accept(totalBooks, available, totalUsers, activeIssues);
        }
    }

    private void loadStats() {
        refresh();
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
